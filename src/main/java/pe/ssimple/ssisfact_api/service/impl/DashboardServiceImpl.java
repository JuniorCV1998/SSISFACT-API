package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Dashboard.ClientesAtendidosKpi;
import pe.ssimple.ssisfact_api.dto.Dashboard.DashboardResumenResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductosEnStockKpi;
import pe.ssimple.ssisfact_api.dto.Dashboard.TicketPromedioKpi;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaDiaResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaHoyKpi;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;
import pe.ssimple.ssisfact_api.repository.DashboardRepository;
import pe.ssimple.ssisfact_api.service.DashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String[] DIAS_ES = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};
    private static final int TOP_PRODUCTOS = 5;
    private static final int TRANSACCIONES_RECIENTES = 8;

    private final DashboardRepository dashboardRepository;

    @Override
    public DashboardResumenResponse obtenerResumen(Long empresaId, Long sucursalId, boolean esAdmin) {

        DashboardResumenResponse response = new DashboardResumenResponse();

        response.setVentasHoy(construirVentasHoy(empresaId, sucursalId));
        response.setClientesAtendidos(construirClientesAtendidos(empresaId, sucursalId));
        response.setTicketPromedio(construirTicketPromedio(empresaId, sucursalId));
        response.setProductosEnStock(construirProductosEnStock(empresaId, sucursalId));
        response.setVentasSemana(construirVentasSemana(empresaId, sucursalId));
        response.setProductosMasVendidos(construirProductosMasVendidos(empresaId, sucursalId));
        response.setMetodosPago(construirMetodosPago(empresaId, sucursalId));
        response.setTransaccionesRecientes(
                dashboardRepository.obtenerTransaccionesRecientes(empresaId, sucursalId, TRANSACCIONES_RECIENTES));

        // Comparativa entre sucursales: solo aporta valor para el admin viendo el
        // consolidado (sin filtrar), y solo si hay más de una sucursal activa.
        if (esAdmin && sucursalId == null) {
            List<VentaSucursalResponse> porSucursal = dashboardRepository.obtenerVentasPorSucursalHoy(empresaId);
            if (porSucursal.size() > 1) {
                response.setVentasPorSucursal(porSucursal);
            }
        }

        return response;
    }

    private VentaHoyKpi construirVentasHoy(Long empresaId, Long sucursalId) {

        Map<String, Object> raw = dashboardRepository.obtenerVentasHoyVsAyer(empresaId, sucursalId);

        BigDecimal totalHoy = toBigDecimal(raw.get("total_hoy"));
        BigDecimal totalAyer = toBigDecimal(raw.get("total_ayer"));

        return new VentaHoyKpi(totalHoy, toInt(raw.get("cantidad_hoy")), calcularVariacionPorcentual(totalHoy, totalAyer));
    }

    private ClientesAtendidosKpi construirClientesAtendidos(Long empresaId, Long sucursalId) {

        Map<String, Object> raw = dashboardRepository.obtenerClientesHoyVsAyer(empresaId, sucursalId);

        int hoy = toInt(raw.get("clientes_hoy"));
        int ayer = toInt(raw.get("clientes_ayer"));

        return new ClientesAtendidosKpi(hoy, hoy - ayer);
    }

    private TicketPromedioKpi construirTicketPromedio(Long empresaId, Long sucursalId) {

        Map<String, Object> raw = dashboardRepository.obtenerVentasHoyVsAyer(empresaId, sucursalId);

        BigDecimal totalHoy = toBigDecimal(raw.get("total_hoy"));
        BigDecimal totalAyer = toBigDecimal(raw.get("total_ayer"));
        int cantidadHoy = toInt(raw.get("cantidad_hoy"));
        int cantidadAyer = toInt(raw.get("cantidad_ayer"));

        BigDecimal ticketHoy = cantidadHoy > 0
                ? totalHoy.divide(BigDecimal.valueOf(cantidadHoy), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal ticketAyer = cantidadAyer > 0
                ? totalAyer.divide(BigDecimal.valueOf(cantidadAyer), 2, RoundingMode.HALF_UP)
                : null;

        return new TicketPromedioKpi(ticketHoy, calcularVariacionPorcentual(ticketHoy, ticketAyer));
    }

    private ProductosEnStockKpi construirProductosEnStock(Long empresaId, Long sucursalId) {

        Map<String, Object> raw = dashboardRepository.obtenerProductosEnStock(empresaId, sucursalId);

        return new ProductosEnStockKpi(toInt(raw.get("total_unidades")), toInt(raw.get("stock_bajo")));
    }

    private List<VentaDiaResponse> construirVentasSemana(Long empresaId, Long sucursalId) {

        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate siguienteLunes = lunes.plusDays(7);

        List<Map<String, Object>> filas = dashboardRepository.obtenerVentasPorDia(empresaId, sucursalId, lunes, siguienteLunes);

        Map<LocalDate, BigDecimal> totalesPorDia = new HashMap<>();
        for (Map<String, Object> fila : filas) {
            LocalDate dia = ((java.sql.Date) fila.get("dia")).toLocalDate();
            totalesPorDia.put(dia, toBigDecimal(fila.get("total")));
        }

        List<VentaDiaResponse> semana = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = lunes.plusDays(i);
            BigDecimal total = totalesPorDia.getOrDefault(fecha, BigDecimal.ZERO);
            semana.add(new VentaDiaResponse(DIAS_ES[i], fecha, total));
        }

        return semana;
    }

    private List<ProductoMasVendidoResponse> construirProductosMasVendidos(Long empresaId, Long sucursalId) {

        List<ProductoMasVendidoResponse> top =
                dashboardRepository.obtenerProductosMasVendidosHoy(empresaId, sucursalId, TOP_PRODUCTOS);

        if (top.isEmpty()) {
            return top;
        }

        BigDecimal maxUnidades = top.get(0).getUnidadesVendidas();
        for (ProductoMasVendidoResponse producto : top) {
            int porcentaje = maxUnidades.compareTo(BigDecimal.ZERO) > 0
                    ? producto.getUnidadesVendidas()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(maxUnidades, 0, RoundingMode.HALF_UP)
                            .intValue()
                    : 0;
            producto.setPorcentajeRelativo(porcentaje);
        }

        return top;
    }

    private List<MetodoPagoResponse> construirMetodosPago(Long empresaId, Long sucursalId) {

        List<MetodoPagoResponse> metodos = dashboardRepository.obtenerMetodosPagoHoy(empresaId, sucursalId);

        if (metodos.isEmpty()) {
            return metodos;
        }

        BigDecimal totalGeneral = metodos.stream()
                .map(MetodoPagoResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (MetodoPagoResponse metodo : metodos) {
            int porcentaje = totalGeneral.compareTo(BigDecimal.ZERO) > 0
                    ? metodo.getTotal()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeneral, 0, RoundingMode.HALF_UP)
                            .intValue()
                    : 0;
            metodo.setPorcentaje(porcentaje);
        }

        return metodos;
    }

    // ayer=0 (o null) no da una base válida de comparación.
    private BigDecimal calcularVariacionPorcentual(BigDecimal hoy, BigDecimal ayer) {
        if (ayer == null || ayer.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return hoy.subtract(ayer)
                .divide(ayer, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        return ((Number) value).intValue();
    }
}
