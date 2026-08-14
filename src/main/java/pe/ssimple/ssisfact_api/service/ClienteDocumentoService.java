package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Documento.ClienteDocumentoResponse;
import pe.ssimple.ssisfact_api.dto.Documento.RegistrarManualRequest;

import java.util.Optional;

public interface ClienteDocumentoService {

    // BD local primero; si no existe, consulta Decolecta (DNI o RUC según
    // codTipoDoc), guarda el resultado en caché y lo devuelve. Optional.empty()
    // si no se encontró ni en BD ni en la API.
    Optional<ClienteDocumentoResponse> consultarDocumento(String codTipoDoc, String nroDocumento);

    ClienteDocumentoResponse registrarManual(RegistrarManualRequest request);
}
