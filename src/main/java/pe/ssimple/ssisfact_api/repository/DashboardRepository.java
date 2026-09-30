package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.TransaccionRecienteResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardRepository {
    // {total_hoy, total_ayer, cantidad_hoy, cantidad_ayer}
    Map<String, Object> obtenerVentasHoyVsAyer(Long empresaId, Long sucursalId);

    // {clientes_hoy, clientes_ayer}
    Map<String, Object> obtenerClientesHoyVsAyer(Long empresaId, Long sucursalId);

    // {total_unidades, stock_bajo}
    Map<String, Object> obtenerProductosEnStock(Long empresaId, Long sucursalId);

    // Una fila por día con ventas en el rango; los días sin ventas no aparecen (se completan en el service).
    List<Map<String, Object>> obtenerVentasPorDia(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo);

    List<ProductoMasVendidoResponse> obtenerProductosMasVendidosHoy(Long empresaId, Long sucursalId, int limite);

    List<MetodoPagoResponse> obtenerMetodosPagoHoy(Long empresaId, Long sucursalId);

    List<VentaSucursalResponse> obtenerVentasPorSucursalHoy(Long empresaId);

    List<TransaccionRecienteResponse> obtenerTransaccionesRecientes(Long empresaId, Long sucursalId, int limite);
}
