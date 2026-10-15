package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Reporte.ReporteResumenResponse;

import java.time.LocalDate;

public interface ReporteService {

    ReporteResumenResponse obtenerResumen(Long empresaId, Long sucursalId, boolean esAdmin,
                                           LocalDate fechaDesde, LocalDate fechaHasta);
}
