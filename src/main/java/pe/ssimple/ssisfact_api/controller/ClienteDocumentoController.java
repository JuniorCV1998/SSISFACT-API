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
import pe.ssimple.ssisfact_api.dto.Documento.ClienteDocumentoResponse;
import pe.ssimple.ssisfact_api.dto.Documento.RegistrarManualRequest;
import pe.ssimple.ssisfact_api.service.ClienteDocumentoService;

@RestController
@RequestMapping("/documento")
@RequiredArgsConstructor
public class ClienteDocumentoController {

    private final ClienteDocumentoService clienteDocumentoService;

    // codTipoDoc: 01 = DNI, 06 = RUC
    @GetMapping("/{codTipoDoc}/{nroDocumento}")
    public ResponseEntity<ApiResponse<ClienteDocumentoResponse>> consultarDocumento(
            @PathVariable String codTipoDoc,
            @PathVariable String nroDocumento) {

        return clienteDocumentoService.consultarDocumento(codTipoDoc, nroDocumento)
                .map(data -> ResponseEntity.ok(ApiResponse.success("Documento encontrado", data)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontró información para ese documento")));
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ClienteDocumentoResponse>> registrarManual(
            @Valid @RequestBody RegistrarManualRequest request) {

        ClienteDocumentoResponse response = clienteDocumentoService.registrarManual(request);

        return ResponseEntity.ok(ApiResponse.success("Documento registrado correctamente", response));
    }
}
