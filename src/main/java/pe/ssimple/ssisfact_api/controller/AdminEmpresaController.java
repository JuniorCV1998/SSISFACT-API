package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.AsignarRolRequest;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminListResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EstadoEmpresaRequest;
import pe.ssimple.ssisfact_api.dto.Admin.PlanEmpresaRequest;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.service.AdminEmpresaService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/admin/empresas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminEmpresaController {

    private final AdminEmpresaService adminEmpresaService;

    @GetMapping
    public ResponseEntity<ApiResponse<EmpresaAdminListResponse>> listarEmpresas(
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "-1") int estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        EmpresaAdminListResponse result = adminEmpresaService.listarEmpresas(busqueda, estado, page, size);

        return ResponseEntity.ok(ApiResponse.success("Empresas obtenidas correctamente", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaAdminDetalleResponse>> obtenerEmpresa(@PathVariable Long id) {

        EmpresaAdminDetalleResponse result = adminEmpresaService.obtenerEmpresa(id);

        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("La empresa no existe"));
        }

        return ResponseEntity.ok(ApiResponse.success("Empresa obtenida correctamente", result));
    }

    @PostMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoEmpresaRequest request) {

        return ResponseBuilder.build(adminEmpresaService.actualizarEstadoEmpresa(id, request.getEstado()), "OK");
    }

    @PostMapping("/{id}/plan")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> actualizarPlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanEmpresaRequest request) {

        AdminSaveResponse response = adminEmpresaService.actualizarPlanEmpresa(
                id, request.getPlan(), request.getMaxSucursal(), request.getMaxUsuarios(), request.getFechaVencimiento());

        return ResponseBuilder.build(response, "OK");
    }

    @PostMapping("/{id}/usuarios/{usuarioId}/rol")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> asignarRol(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @Valid @RequestBody AsignarRolRequest request) {

        return ResponseBuilder.build(adminEmpresaService.asignarRolUsuario(usuarioId, id, request.getRol()), "OK");
    }
}
