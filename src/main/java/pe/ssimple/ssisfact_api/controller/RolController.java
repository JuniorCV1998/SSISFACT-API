package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Rol.RolAsignableResponse;
import pe.ssimple.ssisfact_api.service.RolService;

import java.util.List;

@RestController
@RequestMapping("/rol")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolService rolService;

    // Roles que un admin de empresa puede asignar a sus trabajadores al crear
    // o reasignar (tipo 'USER'): nunca incluye ADMIN/SUNAT/SUPERADMIN.
    @GetMapping("/asignables")
    public ResponseEntity<ApiResponse<List<RolAsignableResponse>>> listarAsignables() {

        List<RolAsignableResponse> result = rolService.listarAsignables();

        return ResponseEntity.ok(ApiResponse.success("Roles obtenidos correctamente", result));
    }
}
