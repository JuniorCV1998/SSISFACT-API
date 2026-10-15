package pe.ssimple.ssisfact_api.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import pe.ssimple.ssisfact_api.dto.RegisterCompany.DniPublicoResponse;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.RucPublicoResponse;
import pe.ssimple.ssisfact_api.service.ClienteDocumentoService;
import pe.ssimple.ssisfact_api.service.CompanyRegisterService;
import pe.ssimple.ssisfact_api.service.RegisterRateLimiterService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class CompanyRegisterController {

    private final CompanyRegisterService companyRegisterService;
    private final ClienteDocumentoService clienteDocumentoService;
    private final RegisterRateLimiterService registerRateLimiterService;

    // Público (sin login) — se usa antes de tener cuenta, cuando el usuario
    // recién está tipeando su RUC en el formulario de alta de empresa.
    // Reutiliza el mismo caché/consulta a Decolecta que /documento/06/{ruc}.
    @GetMapping("/consultarRuc/{ruc}")
    public ResponseEntity<ApiResponse<RucPublicoResponse>> consultarRuc(
            @PathVariable String ruc, HttpServletRequest request) {

        registerRateLimiterService.checkLimit(ipCliente(request));

        return clienteDocumentoService.consultarDocumento("06", ruc)
                .map(data -> {
                    RucPublicoResponse response = new RucPublicoResponse();
                    response.setCodTipoDoc(data.getCodTipoDoc());
                    response.setNroDocumento(data.getNroDocumento());
                    response.setRazonSocial(data.getRazonSocial());
                    response.setDireccion(data.getDireccion());
                    return ResponseEntity.ok(ApiResponse.success("RUC encontrado", response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró información para ese RUC")));
    }

    // Público (sin login) — para el paso 2 del alta, cuando el administrador
    // ingresa su DNI. Reutiliza el mismo caché/consulta a Decolecta que /documento/01/{dni}.
    @GetMapping("/consultarDni/{dni}")
    public ResponseEntity<ApiResponse<DniPublicoResponse>> consultarDni(
            @PathVariable String dni, HttpServletRequest request) {

        registerRateLimiterService.checkLimit(ipCliente(request));

        return clienteDocumentoService.consultarDocumento("01", dni)
                .map(data -> {
                    DniPublicoResponse response = new DniPublicoResponse();
                    response.setCodTipoDoc(data.getCodTipoDoc());
                    response.setNroDocumento(data.getNroDocumento());
                    response.setNombres(data.getNombres());
                    response.setApellidoPaterno(data.getApellidoPaterno());
                    response.setApellidoMaterno(data.getApellidoMaterno());
                    return ResponseEntity.ok(ApiResponse.success("DNI encontrado", response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró información para ese DNI")));
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

    // Detrás del proxy (Railway) la última entrada de X-Forwarded-For es la IP real
    // que agrega el proxy; la primera la puede falsear el cliente. Sin header, usa la conexión directa.
    private String ipCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] ips = xff.split(",");
            return ips[ips.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}