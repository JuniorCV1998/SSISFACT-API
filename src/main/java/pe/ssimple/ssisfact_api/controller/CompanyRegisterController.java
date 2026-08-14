package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.RucPublicoResponse;
import pe.ssimple.ssisfact_api.service.ClienteDocumentoService;
import pe.ssimple.ssisfact_api.service.CompanyRegisterService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class CompanyRegisterController {

    private final CompanyRegisterService companyRegisterService;
    private final ClienteDocumentoService clienteDocumentoService;

    // Público (sin login) — se usa antes de tener cuenta, cuando el usuario
    // recién está tipeando su RUC en el formulario de alta de empresa.
    // Reutiliza el mismo caché/consulta a Decolecta que /documento/06/{ruc}.
    @GetMapping("/consultarRuc/{ruc}")
    public ResponseEntity<ApiResponse<RucPublicoResponse>> consultarRuc(@PathVariable String ruc) {

        return clienteDocumentoService.consultarDocumento("06", ruc)
                .map(data -> {
                    RucPublicoResponse response = new RucPublicoResponse();
                    response.setCodTipoDoc(data.getCodTipoDoc());
                    response.setNroDocumento(data.getNroDocumento());
                    response.setRazonSocial(data.getRazonSocial());
                    return ResponseEntity.ok(ApiResponse.success("RUC encontrado", response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró información para ese RUC")));
    }

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