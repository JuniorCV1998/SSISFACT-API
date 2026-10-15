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
    ComprobanteNumeroResult generarNumeroComprobante(Long empresaId, Long sucursalId, String tipo);
    Long insertarComprobante(Long empresaId, Long ventaId, String tipo, String serie, Integer numero,
                              String clienteNombre, String clienteDocumento,
                              BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total);
    void insertarPago(Long ventaId, String metodo, BigDecimal monto, BigDecimal montoRecibido, String referencia);
    void actualizarTotalesVenta(Long ventaId, BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total, String estado);
    // --- Pago tardío sobre una venta PENDIENTE ya creada (ej. confirmación de Yape) ---
    void actualizarEstadoVenta(Long ventaId, String estado);
    BigDecimal obtenerTotalVenta(Long ventaId);
    // Ventas PENDIENTE de la empresa cuyo saldo (total - pagos ya registrados) coincide
    // con "monto" dentro de las últimas "ventanaHoras". Usado para el match automático
    // de notificaciones de Yape — ver YapeServiceImpl.
    List<Long> buscarVentasPendientesPorSaldo(Long empresaId, BigDecimal monto, int ventanaHoras);
    // Igual que buscarVentasPendientesPorSaldo, pero para pagos digitales divididos
    // (ej. parte por Yape, parte por Plin sobre la misma venta): ventas PENDIENTE cuyo
    // saldo es MAYOR a "monto" — candidatas a recibir este monto como un abono parcial,
    // no como el pago completo. Solo se usa como respaldo cuando no hubo match exacto —
    // ver PagoDigitalServiceImpl.
    List<Long> buscarVentasPendientesPorSaldoParcial(Long empresaId, BigDecimal monto, int ventanaHoras);

    // --- Consulta ---
    List<VentaListItemResponse> listarVentas(Long empresaId, Long sucursalId, Long clienteId, Long cajaId, String estado,
                                              LocalDate fechaDesde, LocalDate fechaHasta, int page, int size);
    // Resuelve el id de venta a partir de su comprobante (serie+número), tenant-scoped.
    // Retorna null si no existe — no lanza excepción.
    Long buscarVentaIdPorComprobante(Long empresaId, String serie, Integer numero);
    // Código legible del comprobante (ej. "NP01-000047") — el que el usuario realmente
    // ve en pantalla. Se usa para los mensajes de notificaciones en vez del id interno
    // de la venta, que el front nunca muestra. Null si la venta no tiene comprobante.
    String obtenerCodigoComprobante(Long ventaId);
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
