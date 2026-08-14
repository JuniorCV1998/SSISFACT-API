package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ssimple.ssisfact_api.dto.Venta.ClienteVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.ComprobanteNumeroResult;
import pe.ssimple.ssisfact_api.dto.Venta.IniciarVentaResult;
import pe.ssimple.ssisfact_api.dto.Venta.InventarioLockInfo;
import pe.ssimple.ssisfact_api.dto.Venta.PagoRequest;
import pe.ssimple.ssisfact_api.dto.Venta.ProductoVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListItemResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaResponse;
import pe.ssimple.ssisfact_api.exception.VentaValidationException;
import pe.ssimple.ssisfact_api.repository.VentaRepository;
import pe.ssimple.ssisfact_api.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private static final Set<String> TIPOS_DOCUMENTO = Set.of("BOLETA", "GUIA");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "YAPE", "PLIN", "TRANSFERENCIA", "TARJETA");
    private static final int MAX_PAGOS_POR_VENTA = 3;

    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request, Long empresaId, Long usuarioId) {

        String tipoDocumento = request.getTipoDocumento().trim().toUpperCase();
        if (!TIPOS_DOCUMENTO.contains(tipoDocumento)) {
            throw new VentaValidationException("El tipo de documento debe ser BOLETA o GUIA");
        }

        // 1. Cabecera: valida caja abierta y resuelve el cliente (o el genérico)
        IniciarVentaResult inicio = ventaRepository.iniciarVenta(
                empresaId, request.getSucursalId(), request.getClienteId(), usuarioId);

        if (!"OK".equals(inicio.getEstado())) {
            throw new VentaValidationException(inicio.getMensaje());
        }

        Long ventaId = inicio.getId();

        // 2. Ítems: valida producto y stock, descuenta inventario, registra movimiento
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoItems = BigDecimal.ZERO;

        for (VentaItemRequest itemReq : request.getItems()) {

            ProductoVentaInfo producto = ventaRepository.obtenerProductoParaVenta(itemReq.getProductoId(), empresaId);
            if (producto == null) {
                throw new VentaValidationException(
                        "El producto " + itemReq.getProductoId() + " no existe o no pertenece a la empresa");
            }

            BigDecimal cantidad = itemReq.getCantidad();
            BigDecimal precioUnitario = itemReq.getPrecioUnitario() != null
                    ? itemReq.getPrecioUnitario()
                    : producto.getPrecio();

            InventarioLockInfo inventario = ventaRepository.bloquearInventario(producto.getId(), request.getSucursalId());
            int disponible = inventario == null ? 0 : inventario.getStock();

            if (inventario == null || BigDecimal.valueOf(disponible).compareTo(cantidad) < 0) {
                throw new VentaValidationException(
                        "Stock insuficiente para \"" + producto.getNombre() + "\". Disponible: " + disponible
                                + ", solicitado: " + cantidad);
            }

            // subtotalItem es siempre el precio BRUTO de la línea (precio x cantidad),
            // sin descuento — el descuento se resta aparte al calcular el total.
            BigDecimal subtotalItem = precioUnitario.multiply(cantidad);
            BigDecimal descuentoItem = itemReq.getDescuento() != null ? itemReq.getDescuento() : BigDecimal.ZERO;

            if (descuentoItem.compareTo(subtotalItem) > 0) {
                throw new VentaValidationException(
                        "El descuento del producto \"" + producto.getNombre() + "\" (" + descuentoItem
                                + ") no puede ser mayor a su subtotal (" + subtotalItem + ")");
            }

            ventaRepository.insertarDetalleVenta(ventaId, producto.getId(), cantidad, precioUnitario, subtotalItem, descuentoItem);
            ventaRepository.actualizarStock(inventario.getInventarioId(), BigDecimal.valueOf(disponible).subtract(cantidad));
            ventaRepository.insertarMovimientoStock(
                    producto.getId(), request.getSucursalId(), "VENTA", cantidad, "Venta #" + ventaId, ventaId);

            subtotal = subtotal.add(subtotalItem);
            descuentoItems = descuentoItems.add(descuentoItem);
        }

        // 3. Totales — sin IGV por ahora: es un documento interno, sin valor tributario.
        // descuento = suma de descuentos por ítem + descuento global de la venta.
        BigDecimal descuentoGlobal = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal descuento = descuentoItems.add(descuentoGlobal);
        BigDecimal impuestos = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuento).add(impuestos);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new VentaValidationException(
                    "El descuento total (" + descuento + ") no puede ser mayor al subtotal de la venta (" + subtotal + ")");
        }

        // 4. Comprobante (numeración de la serie principal de la empresa para ese tipo)
        ComprobanteNumeroResult numeroComprobante = ventaRepository.generarNumeroComprobante(empresaId, tipoDocumento);
        if (!"OK".equals(numeroComprobante.getEstado())) {
            throw new VentaValidationException(numeroComprobante.getMensaje());
        }

        ClienteVentaInfo clienteInfo = ventaRepository.obtenerClienteInfo(inicio.getClienteId());

        ventaRepository.insertarComprobante(
                empresaId, ventaId, tipoDocumento, numeroComprobante.getSerie(), numeroComprobante.getNumero(),
                clienteInfo != null ? clienteInfo.getNombre() : null,
                clienteInfo != null ? clienteInfo.getNumeroDocumento() : null,
                subtotal, descuento, impuestos, total);

        // 5. Pagos (opcional — una venta puede quedar PENDIENTE sin pagos, al crédito)
        List<PagoRequest> pagos = request.getPagos();
        BigDecimal totalPagado = BigDecimal.ZERO;

        if (pagos != null && !pagos.isEmpty()) {

            if (pagos.size() > MAX_PAGOS_POR_VENTA) {
                throw new VentaValidationException("Una venta admite como máximo " + MAX_PAGOS_POR_VENTA + " pagos");
            }

            for (PagoRequest pago : pagos) {
                String metodo = pago.getMetodo().trim().toUpperCase();
                if (!METODOS_PAGO.contains(metodo)) {
                    throw new VentaValidationException("Método de pago inválido: " + pago.getMetodo());
                }
                totalPagado = totalPagado.add(pago.getMonto());
            }

            if (totalPagado.compareTo(total) > 0) {
                throw new VentaValidationException(
                        "La suma de los pagos (" + totalPagado + ") supera el total de la venta (" + total + ")");
            }

            for (PagoRequest pago : pagos) {
                ventaRepository.insertarPago(ventaId, pago.getMetodo().trim().toUpperCase(), pago.getMonto(), pago.getReferencia());
            }
        }

        String estadoVenta = totalPagado.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0 ? "PAGADA" : "PENDIENTE";
        ventaRepository.actualizarTotalesVenta(ventaId, subtotal, descuento, impuestos, total, estadoVenta);

        log.info("[registrarVenta] venta registrada — id={} empresaId={} sucursalId={} total={} estado={}",
                ventaId, empresaId, request.getSucursalId(), total, estadoVenta);

        return new VentaResponse(ventaId, estadoVenta, tipoDocumento,
                numeroComprobante.getSerie(), numeroComprobante.getNumero(), subtotal, descuento, impuestos, total);
    }

    @Override
    public VentaListResponse listarVentas(Long empresaId, Long sucursalId, Long clienteId, Long cajaId, String estado,
                                           LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) {

        List<VentaListItemResponse> items = ventaRepository.listarVentas(
                empresaId, sucursalId, clienteId, cajaId, estado, fechaDesde, fechaHasta, page, size);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new VentaListResponse(items, total, page, size);
    }

    @Override
    public VentaDetalleResponse obtenerVentaDetalle(Long ventaId, Long empresaId) {

        VentaDetalleResponse cabecera = ventaRepository.obtenerVentaCabecera(ventaId, empresaId);
        if (cabecera == null) {
            throw new VentaValidationException("La venta no existe");
        }

        cabecera.setItems(ventaRepository.listarItemsVenta(ventaId));
        cabecera.setPagos(ventaRepository.listarPagosVenta(ventaId));

        return cabecera;
    }

    @Override
    @Transactional
    public VentaResponse anularVenta(Long ventaId, Long empresaId) {

        String estadoActual = ventaRepository.obtenerEstadoVenta(ventaId, empresaId);
        if (estadoActual == null) {
            throw new VentaValidationException("La venta no existe");
        }
        if ("ANULADA".equals(estadoActual)) {
            throw new VentaValidationException("La venta ya está anulada");
        }

        String estadoCaja = ventaRepository.obtenerEstadoCajaVenta(ventaId);
        if ("CERRADA".equals(estadoCaja)) {
            throw new VentaValidationException(
                    "No se puede anular la venta: la caja donde se registró ya está cerrada");
        }

        Long sucursalId = ventaRepository.obtenerSucursalVenta(ventaId);

        for (VentaItemDetalleResponse item : ventaRepository.listarProductoCantidadParaAnular(ventaId)) {

            InventarioLockInfo inventario = ventaRepository.bloquearInventario(item.getProductoId(), sucursalId);
            if (inventario != null) {
                ventaRepository.actualizarStock(
                        inventario.getInventarioId(),
                        BigDecimal.valueOf(inventario.getStock()).add(item.getCantidad()));
            }

            ventaRepository.insertarMovimientoStock(
                    item.getProductoId(), sucursalId, "AJUSTE", item.getCantidad(),
                    "Anulación venta #" + ventaId, ventaId);
        }

        ventaRepository.marcarVentaAnulada(ventaId);

        log.info("[anularVenta] venta anulada — id={} empresaId={}", ventaId, empresaId);

        return new VentaResponse(ventaId, "ANULADA", null, null, null, null, null, null, null);
    }
}
