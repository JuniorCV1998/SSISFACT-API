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
import pe.ssimple.ssisfact_api.dto.Venta.PagoResponse;
import pe.ssimple.ssisfact_api.dto.Venta.ProductoVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListItemResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaPagoEventPayload;
import pe.ssimple.ssisfact_api.dto.Venta.VentaRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaResponse;
import pe.ssimple.ssisfact_api.exception.VentaValidationException;
import pe.ssimple.ssisfact_api.repository.VentaRepository;
import pe.ssimple.ssisfact_api.service.NotificacionService;
import pe.ssimple.ssisfact_api.service.VentaService;
import pe.ssimple.ssisfact_api.util.ComprobanteCodigoUtil;
import pe.ssimple.ssisfact_api.util.NumeroALetrasUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    // NOTA_PEDIDO: documento interno, sin valor tributario, mientras no exista
    // facturación electrónica SUNAT. BOLETA/GUIA quedan reservados para cuando se active.
    private static final Set<String> TIPOS_DOCUMENTO = Set.of("BOLETA", "GUIA", "NOTA_PEDIDO");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "YAPE", "PLIN INTERBANK", "TRANSFERENCIA", "TARJETA");
    // Yape y Plin no se registran como pago "ya recibido" al crear la venta: se
    // confirman solos cuando llega la notificación real al webhook y hace match
    // por saldo (PagoDigitalService). Si se permitiera registrarlos aquí, la venta
    // quedaría PAGADA al instante sin que el dinero haya llegado de verdad, y el
    // frontend nunca vería la transición pendiente→pagada que da la confirmación real.
    private static final Set<String> METODOS_PAGO_AUTOMATICO = Set.of("YAPE", "PLIN INTERBANK");
    private static final int MAX_PAGOS_POR_VENTA = 3;

    private final VentaRepository ventaRepository;
    private final VentaSseService ventaSseService;
    private final NotificacionService notificacionService;

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request, Long empresaId, Long usuarioId) {

        String tipoDocumento = request.getTipoDocumento().trim().toUpperCase();
        if (!TIPOS_DOCUMENTO.contains(tipoDocumento)) {
            throw new VentaValidationException("El tipo de documento debe ser BOLETA, GUIA o NOTA_PEDIDO");
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
        ComprobanteNumeroResult numeroComprobante = ventaRepository.generarNumeroComprobante(empresaId, request.getSucursalId(), tipoDocumento);
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
        if ((pagos == null || pagos.isEmpty())
                && request.getMetodoPagoContado() != null && !request.getMetodoPagoContado().isBlank()
                && total.compareTo(BigDecimal.ZERO) > 0) {
            String metodoContado = request.getMetodoPagoContado().trim().toUpperCase();
            if (METODOS_PAGO_AUTOMATICO.contains(metodoContado)) {
                throw new VentaValidationException(
                        "Yape y Plin Interbank se confirman solos al llegar el pago real; deja la venta sin método de pago y "
                                + "quedará PENDIENTE hasta que el sistema lo confirme automáticamente");
            }
            PagoRequest pagoContado = new PagoRequest();
            pagoContado.setMetodo(request.getMetodoPagoContado());
            pagoContado.setMonto(total);
            pagos = List.of(pagoContado);
        }
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
                if (METODOS_PAGO_AUTOMATICO.contains(metodo)) {
                    throw new VentaValidationException(
                            "Yape y Plin Interbank se confirman solos al llegar el pago real; no se registran al crear la venta. "
                                    + "Omite ese pago y la venta quedará PENDIENTE hasta que el sistema lo confirme automáticamente");
                }
                if (pago.getMontoRecibido() != null && pago.getMontoRecibido().compareTo(pago.getMonto()) < 0) {
                    throw new VentaValidationException(
                            "El monto recibido (" + pago.getMontoRecibido() + ") no puede ser menor al monto del pago (" + pago.getMonto() + ")");
                }
                totalPagado = totalPagado.add(pago.getMonto());
            }

            if (totalPagado.compareTo(total) > 0) {
                throw new VentaValidationException(
                        "La suma de los pagos (" + totalPagado + ") supera el total de la venta (" + total + ")");
            }

            for (PagoRequest pago : pagos) {
                ventaRepository.insertarPago(ventaId, pago.getMetodo().trim().toUpperCase(),
                        pago.getMonto(), pago.getMontoRecibido(), pago.getReferencia());
            }
        }

        String estadoVenta = totalPagado.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0 ? "PAGADA" : "PENDIENTE";
        ventaRepository.actualizarTotalesVenta(ventaId, subtotal, descuento, impuestos, total, estadoVenta);

        log.info("[registrarVenta] venta registrada — id={} empresaId={} sucursalId={} total={} estado={}",
                ventaId, empresaId, request.getSucursalId(), total, estadoVenta);

        return new VentaResponse(ventaId, estadoVenta, tipoDocumento,
                numeroComprobante.getSerie(), numeroComprobante.getNumero(),
                ComprobanteCodigoUtil.formatear(numeroComprobante.getSerie(), numeroComprobante.getNumero()),
                subtotal, descuento, impuestos, total);
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

        List<VentaItemDetalleResponse> items = ventaRepository.listarItemsVenta(ventaId);
        cabecera.setItems(items);
        cabecera.setPagos(ventaRepository.listarPagosVenta(ventaId));

        cabecera.setTotalUnidades(items.stream()
                .map(VentaItemDetalleResponse::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        cabecera.setMontoEnLetras(NumeroALetrasUtil.convertir(cabecera.getTotal()));
        cabecera.setDocumentoTitulo(tituloDocumento(cabecera.getComprobanteTipo()));
        cabecera.setComprobanteCodigo(ComprobanteCodigoUtil.formatear(cabecera.getComprobanteSerie(), cabecera.getComprobanteNumero()));

        return cabecera;
    }

    // Título honesto para imprimir según el tipo real de comprobante. BOLETA/FACTURA/etc.
    // solo deberían implicar "electrónica" el día que exista declaración real ante SUNAT;
    // hasta entonces, el único tipo realmente en uso es NOTA_PEDIDO.
    private String tituloDocumento(String comprobanteTipo) {
        if (comprobanteTipo == null) {
            return "NOTA DE PEDIDO";
        }
        return switch (comprobanteTipo) {
            case "BOLETA" -> "BOLETA DE VENTA ELECTRÓNICA";
            case "FACTURA" -> "FACTURA ELECTRÓNICA";
            case "GUIA" -> "GUÍA DE REMISIÓN ELECTRÓNICA";
            case "NOTA_CREDITO" -> "NOTA DE CRÉDITO ELECTRÓNICA";
            case "NOTA_DEBITO" -> "NOTA DE DÉBITO ELECTRÓNICA";
            default -> "NOTA DE PEDIDO";
        };
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

        return new VentaResponse(ventaId, "ANULADA", null, null, null, null, null, null, null, null);
    }

    @Override
    @Transactional
    public PagoResponse registrarPagoVenta(Long ventaId, Long empresaId, String metodo, BigDecimal monto, String referencia) {

        String estadoActual = ventaRepository.obtenerEstadoVenta(ventaId, empresaId);
        if (estadoActual == null) {
            throw new VentaValidationException("La venta no existe");
        }
        if (!"PENDIENTE".equals(estadoActual)) {
            throw new VentaValidationException(
                    "La venta no está pendiente de pago (estado actual: " + estadoActual + ")");
        }

        String metodoNormalizado = metodo == null ? "" : metodo.trim().toUpperCase();
        if (!METODOS_PAGO.contains(metodoNormalizado)) {
            throw new VentaValidationException("Método de pago inválido: " + metodo);
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new VentaValidationException("El monto del pago debe ser mayor a 0");
        }

        List<PagoResponse> pagosExistentes = ventaRepository.listarPagosVenta(ventaId);
        if (pagosExistentes.size() >= MAX_PAGOS_POR_VENTA) {
            throw new VentaValidationException("Una venta admite como máximo " + MAX_PAGOS_POR_VENTA + " pagos");
        }

        BigDecimal total = ventaRepository.obtenerTotalVenta(ventaId);
        BigDecimal totalPagado = pagosExistentes.stream()
                .map(PagoResponse::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = total.subtract(totalPagado);

        if (monto.compareTo(saldo) > 0) {
            throw new VentaValidationException(
                    "El pago (" + monto + ") supera el saldo pendiente de la venta (" + saldo + ")");
        }

        ventaRepository.insertarPago(ventaId, metodoNormalizado, monto, null, referencia);

        BigDecimal nuevoSaldo = saldo.subtract(monto);
        String estadoResultante = totalPagado.add(monto).compareTo(total) >= 0 ? "PAGADA" : "PENDIENTE";
        if ("PAGADA".equals(estadoResultante)) {
            ventaRepository.actualizarEstadoVenta(ventaId, "PAGADA");
        }

        log.info("[registrarPagoVenta] pago agregado — ventaId={} empresaId={} metodo={} monto={}",
                ventaId, empresaId, metodoNormalizado, monto);

        // Avisa en vivo al frontend (SSE): sin esto, la pantalla no se entera de que
        // el pago entró hasta que alguien recarga manualmente (ver VentaSseService).
        ventaSseService.publicarPagoConfirmado(empresaId,
                new VentaPagoEventPayload(ventaId, estadoResultante, metodoNormalizado, monto, nuevoSaldo));

        // Queda también en el centro de notificaciones (campana) — para toda la
        // empresa, no solo quien tenga la pantalla de la venta abierta en ese momento.
        // Se identifica la venta por el código de su comprobante (ej. "NP01-000047"),
        // que es lo que el usuario realmente ve en pantalla — el id interno (ventaId)
        // no aparece en ninguna parte del front, así que mencionarlo no le sirve a nadie.
        String codigoComprobante = ventaRepository.obtenerCodigoComprobante(ventaId);
        String referenciaVenta = codigoComprobante != null ? codigoComprobante : "#" + ventaId;
        String mensaje = "Se registró un pago de S/ " + monto + " por " + metodoNormalizado
                + " en la venta " + referenciaVenta + (nuevoSaldo.compareTo(BigDecimal.ZERO) > 0
                        ? " (saldo pendiente: S/ " + nuevoSaldo + ")" : "");
        notificacionService.crear(empresaId, null, "PAGO_DIGITAL_CONFIRMADO",
                "Pago confirmado", mensaje, "VENTA", ventaId);

        List<PagoResponse> pagos = ventaRepository.listarPagosVenta(ventaId);
        return pagos.get(pagos.size() - 1);
    }
}
