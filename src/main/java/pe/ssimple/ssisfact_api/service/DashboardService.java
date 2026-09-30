package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Dashboard.DashboardResumenResponse;

public interface DashboardService {
    DashboardResumenResponse obtenerResumen(Long empresaId, Long sucursalId, boolean esAdmin);
}
