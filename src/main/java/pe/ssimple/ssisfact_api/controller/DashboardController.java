package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.DashboardResumenResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // sucursalId es ignorado para no-admin: siempre se les muestra SU sucursal
    // asignada, sin importar lo que manden. Un admin sin sucursalId ve el
    // consolidado de toda la empresa; con sucursalId, ve solo esa sucursal.
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResumenResponse>> resumen(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long sucursalId) {

        boolean esAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        Long sucursalEfectiva = esAdmin ? sucursalId : user.getSucursalId();

        DashboardResumenResponse result = dashboardService.obtenerResumen(
                user.getEmpresaId(), sucursalEfectiva, esAdmin);

        return ResponseEntity.ok(ApiResponse.success("Dashboard obtenido correctamente", result));
    }
}
