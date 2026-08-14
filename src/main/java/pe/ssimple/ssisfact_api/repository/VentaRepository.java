package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Venta.ClienteVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.ComprobanteNumeroResult;
import pe.ssimple.ssisfact_api.dto.Venta.IniciarVentaResult;
import pe.ssimple.ssisfact_api.dto.Venta.InventarioLockInfo;
import pe.ssimple.ssisfact_api.dto.Venta.PagoResponse;
import pe.ssimple.ssisfact_api.dto.Venta.ProductoVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListItemResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VentaRepository {

    // --- Registro de venta (orquestado desde VentaServiceImpl en una única transacción) ---
    IniciarVentaResult iniciarVenta(Long empresaId, Long sucursalId, Long clienteId, Long usuarioId);
    ClienteVentaInfo obtenerClienteInfo(Long clienteId);
    ProductoVentaInfo obtenerProductoParaVenta(Long productoId, Long empresaId);
    InventarioLockInfo bloquearInventario(Long productoId, Long sucursalId);
    void insertarDetalleVenta(Long ventaId, Long productoId, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal descuento);
    void actualizarStock(Long inventarioId, BigDecimal nuevoStock);
    void insertarMovimientoStock(Long productoId, Long sucursalId, String tipo, BigDecimal cantidad, String motivo, Long referenciaId);
    ComprobanteNumeroResult generarNumeroComprobante(Long empresaId, String tipo);
    Long insertarComprobante(Long empresaId, Long ventaId, String tipo, String serie, Integer numero,
                              String clienteNombre, String clienteDocumento,
                              BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total);
    void insertarPago(Long ventaId, String metodo, BigDecimal monto, String referencia);
    void actualizarTotalesVenta(Long ventaId, BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total, String estado);

    // --- Consulta ---
    List<VentaListItemResponse> listarVentas(Long empresaId, Long sucursalId, Long clienteId, Long cajaId, String estado,
                                              LocalDate fechaDesde, LocalDate fechaHasta, int page, int size);
    VentaDetalleResponse obtenerVentaCabecera(Long ventaId, Long empresaId);
    List<VentaItemDetalleResponse> listarItemsVenta(Long ventaId);
    List<PagoResponse> listarPagosVenta(Long ventaId);

    // --- Anulación ---
    String obtenerEstadoVenta(Long ventaId, Long empresaId);
    String obtenerEstadoCajaVenta(Long ventaId);
    Long obtenerSucursalVenta(Long ventaId);
    List<VentaItemDetalleResponse> listarProductoCantidadParaAnular(Long ventaId);
    void marcarVentaAnulada(Long ventaId);
}
