package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.AsignarRolRequest;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Usuario.AsignarSucursalRequest;
import pe.ssimple.ssisfact_api.dto.Usuario.CrearUsuarioRequest;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.UsuarioEmpresaService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

import java.util.List;

// Gestión de los trabajadores de la PROPIA empresa del admin logueado — distinto
// de AdminEmpresaController, que es del superadmin de la plataforma sobre CUALQUIER empresa.
@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioEmpresaService usuarioService;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<UsuarioAdminResponse>>> listarUsuarios(
            @AuthenticationPrincipal CustomUserDetails user) {

        List<UsuarioAdminResponse> result = usuarioService.listarUsuariosEmpresa(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos correctamente", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminSaveResponse>> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AdminSaveResponse response = usuarioService.crearUsuario(
                user.getEmpresaId(), request.getNombre().trim(), request.getEmail().trim(),
                request.getDocumento().trim(), request.getPassword(), request.getRol(), request.getSucursalId());

        return ResponseBuilder.build(response, "OK");
    }

    @PostMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> asignarRol(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRolRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AdminSaveResponse response = usuarioService.asignarRol(id, user.getEmpresaId(), request.getRol());

        return ResponseBuilder.build(response, "OK");
    }

    @PostMapping("/{id}/sucursal")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> asignarSucursal(
            @PathVariable Long id,
            @Valid @RequestBody AsignarSucursalRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AdminSaveResponse response = usuarioService.asignarSucursal(id, user.getEmpresaId(), request.getSucursalId());

        return ResponseBuilder.build(response, "OK");
    }
}
