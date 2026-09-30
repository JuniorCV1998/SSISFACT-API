package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.EmpresaService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/empresa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/actual")
    public ResponseEntity<ApiResponse<EmpresaActualResponse>> obtenerActual(
            @AuthenticationPrincipal CustomUserDetails user) {

        EmpresaActualResponse result = empresaService.obtenerActual(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Empresa obtenida correctamente", result));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> actualizar(
            @Valid @RequestBody EmpresaActualizarRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AdminSaveResponse response = empresaService.actualizar(user.getEmpresaId(), request);

        return ResponseBuilder.build(response, "OK");
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AdminSaveResponse>> actualizarLogo(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user) {

        AdminSaveResponse response = empresaService.actualizarLogo(user.getEmpresaId(), file);

        return ResponseBuilder.build(response, "OK");
    }
}
