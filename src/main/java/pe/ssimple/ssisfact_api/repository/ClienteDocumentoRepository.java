package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Documento.ClienteDocumentoResponse;

import java.util.Optional;

public interface ClienteDocumentoRepository {
    Optional<ClienteDocumentoResponse> buscarPorDocumento(String codTipoDoc, String nroDocumento);

    ClienteDocumentoResponse guardar(String codTipoDoc, String nroDocumento,
                                      String nombres, String apellidoPaterno, String apellidoMaterno,
                                      String razonSocial, String estadoRuc, String condicion,
                                      String direccion, String ubigeo, String origenDatos);
}
