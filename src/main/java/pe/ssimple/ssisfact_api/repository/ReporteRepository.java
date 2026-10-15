package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.ClienteTopResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReporteRepository {

    // {total_vendido, cantidad_ventas} de ventas PAGADA en [desde, hastaExclusivo)
    Map<String, Object> obtenerVentasRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo);

    // {monto, cantidad} de ventas PENDIENTE en [desde, hastaExclusivo)
    Map<String, Object> obtenerPendientesCobroRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo);

    List<ProductoMasVendidoResponse> obtenerProductosMasVendidosRango(
            Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo, int limite);

    List<MetodoPagoResponse> obtenerMetodosPagoRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo);

    List<VentaSucursalResponse> obtenerVentasPorSucursalRango(Long empresaId, LocalDate desde, LocalDate hastaExclusivo);

    List<ClienteTopResponse> obtenerTopClientesRango(
            Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo, int limite);
}
