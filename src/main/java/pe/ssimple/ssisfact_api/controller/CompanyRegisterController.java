package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;
import pe.ssimple.ssisfact_api.service.CompanyRegisterService;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class CompanyRegisterController {

    private final CompanyRegisterService companyRegisterService;

    /**
     * Valida y registra el proceso de creación de empresa.
     */
    @PostMapping("/validateCompany")
    public ResponseEntity<ApiResponse<CompanyRegisterResponse>> registrarEmpresa(
            @Valid @RequestBody CompanyRegisterRequest request) {

        return buildResponse(
                companyRegisterService.registrarEmpresa(request));
    }

    /**
     * Confirma el proceso y crea empresa + usuario administrador.
     */
    @PostMapping("/confirmCompany")
    public ResponseEntity<ApiResponse<CompanyRegisterResponse>> confirmarEmpresa(
            @Valid @RequestBody CompanyConfirmRequest request) {

        return buildResponse(
                companyRegisterService.confirmarEmpresa(request));
    }

    /**
     * Construye respuesta estándar para todos los endpoints
     */
    private ResponseEntity<ApiResponse<CompanyRegisterResponse>>
    buildResponse(CompanyRegisterResponse response) {

        if (!"OK".equals(response.getEstado())
                && !"CONFIRMADO".equals(response.getEstado())) {

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            response.getMensaje(),
                            response));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        response.getMensaje(),
                        response));
    }
}