package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaDiaResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.ClienteTopResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.PendientesCobroResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.ReporteResumenResponse;
import pe.ssimple.ssisfact_api.exception.VentaValidationException;
import pe.ssimple.ssisfact_api.repository.DashboardRepository;
import pe.ssimple.ssisfact_api.repository.ReporteRepository;
import pe.ssimple.ssisfact_api.service.ReporteService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final String[] DIAS_ES = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};
    private static final int TOP_PRODUCTOS = 5;
    private static final int TOP_CLIENTES = 5;
    private static final int RANGO_MAXIMO_DIAS = 366;

    private final ReporteRepository reporteRepository;
    private final DashboardRepository dashboardRepository;

    @Override
    public ReporteResumenResponse obtenerResumen(Long empresaId, Long sucursalId, boolean esAdmin,
                                                  LocalDate fechaDesde, LocalDate fechaHasta) {

        if (fechaHasta.isBefore(fechaDesde)) {
            throw new VentaValidationException("fechaHasta no puede ser anterior a fechaDesde");
        }
        long diasRango = ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
        if (diasRango > RANGO_MAXIMO_DIAS) {
            throw new VentaValidationException("El rango de fechas no puede superar " + RANGO_MAXIMO_DIAS + " días");
        }

        LocalDate hastaExclusivo = fechaHasta.plusDays(1);

        ReporteResumenResponse response = new ReporteResumenResponse();

        Map<String, Object> ventasActual = reporteRepository.obtenerVentasRango(empresaId, sucursalId, fechaDesde, hastaExclusivo);
        BigDecimal totalVendido = toBigDecimal(ventasActual.get("total_vendido"));
        int cantidadVentas = toInt(ventasActual.get("cantidad_ventas"));

        LocalDate desdeAnterior = fechaDesde.minusDays(diasRango);
        Map<String, Object> ventasAnterior = reporteRepository.obtenerVentasRango(empresaId, sucursalId, desdeAnterior, fechaDesde);
        BigDecimal totalAnterior = toBigDecimal(ventasAnterior.get("total_vendido"));

        response.setTotalVendido(totalVendido);
        response.setCantidadVentas(cantidadVentas);
        response.setTicketPromedio(cantidadVentas > 0
                ? totalVendido.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        response.setVariacionPorcentualVsPeriodoAnterior(calcularVariacionPorcentual(totalVendido, totalAnterior));

        Map<String, Object> pendientes = reporteRepository.obtenerPendientesCobroRango(empresaId, sucursalId, fechaDesde, hastaExclusivo);
        response.setPendientesCobro(new PendientesCobroResponse(toBigDecimal(pendientes.get("monto")), toInt(pendientes.get("cantidad"))));

        response.setVentasPorDia(construirVentasPorDia(empresaId, sucursalId, fechaDesde, hastaExclusivo, (int) diasRango));
        response.setProductosMasVendidos(construirProductosMasVendidos(empresaId, sucursalId, fechaDesde, hastaExclusivo));
        response.setMetodosPago(construirMetodosPago(empresaId, sucursalId, fechaDesde, hastaExclusivo));
        response.setTopClientes(construirTopClientes(empresaId, sucursalId, fechaDesde, hastaExclusivo));

        if (esAdmin && sucursalId == null) {
            List<VentaSucursalResponse> porSucursal = reporteRepository.obtenerVentasPorSucursalRango(empresaId, fechaDesde, hastaExclusivo);
            if (porSucursal.size() > 1) {
                response.setVentasPorSucursal(porSucursal);
            }
        }

        return response;
    }

    private List<VentaDiaResponse> construirVentasPorDia(Long empresaId, Long sucursalId,
                                                           LocalDate desde, LocalDate hastaExclusivo, int totalDias) {

        List<Map<String, Object>> filas = dashboardRepository.obtenerVentasPorDia(empresaId, sucursalId, desde, hastaExclusivo);

        Map<LocalDate, BigDecimal> totalesPorDia = new HashMap<>();
        for (Map<String, Object> fila : filas) {
            LocalDate dia = ((java.sql.Date) fila.get("dia")).toLocalDate();
            totalesPorDia.put(dia, toBigDecimal(fila.get("total")));
        }

        List<VentaDiaResponse> resultado = new ArrayList<>(totalDias);
        for (int i = 0; i < totalDias; i++) {
            LocalDate fecha = desde.plusDays(i);
            BigDecimal total = totalesPorDia.getOrDefault(fecha, BigDecimal.ZERO);
            resultado.add(new VentaDiaResponse(DIAS_ES[fecha.getDayOfWeek().getValue() - 1], fecha, total));
        }

        return resultado;
    }

    private List<ProductoMasVendidoResponse> construirProductosMasVendidos(Long empresaId, Long sucursalId,
                                                                             LocalDate desde, LocalDate hastaExclusivo) {

        List<ProductoMasVendidoResponse> top =
                reporteRepository.obtenerProductosMasVendidosRango(empresaId, sucursalId, desde, hastaExclusivo, TOP_PRODUCTOS);

        if (top.isEmpty()) {
            return top;
        }

        BigDecimal maxUnidades = top.get(0).getUnidadesVendidas();
        for (ProductoMasVendidoResponse producto : top) {
            producto.setPorcentajeRelativo(porcentajeRelativo(producto.getUnidadesVendidas(), maxUnidades).intValue());
        }

        return top;
    }

    private List<MetodoPagoResponse> construirMetodosPago(Long empresaId, Long sucursalId,
                                                            LocalDate desde, LocalDate hastaExclusivo) {

        List<MetodoPagoResponse> metodos = reporteRepository.obtenerMetodosPagoRango(empresaId, sucursalId, desde, hastaExclusivo);

        if (metodos.isEmpty()) {
            return metodos;
        }

        BigDecimal totalGeneral = metodos.stream()
                .map(MetodoPagoResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (MetodoPagoResponse metodo : metodos) {
            metodo.setPorcentaje(porcentajeRelativo(metodo.getTotal(), totalGeneral).intValue());
        }

        return metodos;
    }

    private List<ClienteTopResponse> construirTopClientes(Long empresaId, Long sucursalId,
                                                            LocalDate desde, LocalDate hastaExclusivo) {

        List<ClienteTopResponse> top =
                reporteRepository.obtenerTopClientesRango(empresaId, sucursalId, desde, hastaExclusivo, TOP_CLIENTES);

        if (top.isEmpty()) {
            return top;
        }

        BigDecimal maxTotal = top.get(0).getTotalComprado();
        for (ClienteTopResponse cliente : top) {
            cliente.setPorcentajeRelativo(porcentajeRelativo(cliente.getTotalComprado(), maxTotal).intValue());
        }

        return top;
    }

    private BigDecimal porcentajeRelativo(BigDecimal valor, BigDecimal base) {
        return base.compareTo(BigDecimal.ZERO) > 0
                ? valor.multiply(BigDecimal.valueOf(100)).divide(base, 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    // base=0 (o null) no da una base válida de comparación.
    private BigDecimal calcularVariacionPorcentual(BigDecimal actual, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return actual.subtract(base)
                .divide(base, 4, RoundingMode.HALF_UP)
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
