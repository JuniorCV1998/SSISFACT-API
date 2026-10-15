package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.ReporteResumenResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.ReporteService;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // sucursalId es ignorado para no-admin: siempre se les muestra SU sucursal
    // asignada, sin importar lo que manden. Un admin sin sucursalId ve el
    // consolidado de toda la empresa; con sucursalId, ve solo esa sucursal.
    // Mismo criterio que /dashboard/resumen.
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<ReporteResumenResponse>> resumen(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long sucursalId) {

        boolean esAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        Long sucursalEfectiva = esAdmin ? sucursalId : user.getSucursalId();

        ReporteResumenResponse result = reporteService.obtenerResumen(
                user.getEmpresaId(), sucursalEfectiva, esAdmin, fechaDesde, fechaHasta);

        return ResponseEntity.ok(ApiResponse.success("Reporte obtenido correctamente", result));
    }
}
