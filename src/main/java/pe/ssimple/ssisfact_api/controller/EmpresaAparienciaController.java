package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaAparienciaResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.EmpresaService;

@RestController
@RequestMapping("/empresa/apariencia")
@RequiredArgsConstructor
public class EmpresaAparienciaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<ApiResponse<EmpresaAparienciaResponse>> obtenerApariencia(
            @AuthenticationPrincipal CustomUserDetails user) {

        EmpresaAparienciaResponse result = empresaService.obtenerApariencia(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Apariencia obtenida correctamente", result));
    }
}
