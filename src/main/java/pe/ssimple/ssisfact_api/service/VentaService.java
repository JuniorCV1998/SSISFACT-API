package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Venta.PagoResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface VentaService {
    VentaResponse registrarVenta(VentaRequest request, Long empresaId, Long usuarioId);
    VentaListResponse listarVentas(Long empresaId, Long sucursalId, Long clienteId, Long cajaId, String estado,
                                    LocalDate fechaDesde, LocalDate fechaHasta, int page, int size);
    VentaDetalleResponse obtenerVentaDetalle(Long ventaId, Long empresaId);
    VentaResponse anularVenta(Long ventaId, Long empresaId);
    // Agrega un pago a una venta PENDIENTE ya creada (fiado) y, si con eso se cubre el
    // total, la pasa a PAGADA. Usado tanto por la confirmación automática/manual de
    // notificaciones de Yape (ver YapeService) como por cualquier otro pago tardío.
    PagoResponse registrarPagoVenta(Long ventaId, Long empresaId, String metodo, BigDecimal monto, String referencia);
}
