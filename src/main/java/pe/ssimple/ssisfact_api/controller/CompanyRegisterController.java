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
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class CompanyRegisterController {

    private final CompanyRegisterService companyRegisterService;

    @PostMapping("/validateCompany")
    public ResponseEntity<ApiResponse<CompanyRegisterResponse>> registrarEmpresa(
            @Valid @RequestBody CompanyRegisterRequest request) {

        return ResponseBuilder.build(companyRegisterService.registrarEmpresa(request), "OK");
    }

    @PostMapping("/confirmCompany")
    public ResponseEntity<ApiResponse<CompanyRegisterResponse>> confirmarEmpresa(
            @Valid @RequestBody CompanyConfirmRequest request) {

        return ResponseBuilder.build(companyRegisterService.confirmarEmpresa(request), "OK", "CONFIRMADO");
    }
}