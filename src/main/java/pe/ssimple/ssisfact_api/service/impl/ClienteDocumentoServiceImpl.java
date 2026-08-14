package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Decolecta.ReniecDniResponse;
import pe.ssimple.ssisfact_api.dto.Decolecta.SunatRucResponse;
import pe.ssimple.ssisfact_api.dto.Documento.ClienteDocumentoResponse;
import pe.ssimple.ssisfact_api.dto.Documento.RegistrarManualRequest;
import pe.ssimple.ssisfact_api.exception.DocumentoValidationException;
import pe.ssimple.ssisfact_api.repository.ClienteDocumentoRepository;
import pe.ssimple.ssisfact_api.service.ClienteDocumentoService;
import pe.ssimple.ssisfact_api.service.DecolectaClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteDocumentoServiceImpl implements ClienteDocumentoService {

    private static final String TIPO_DNI = "01";
    private static final String TIPO_RUC = "06";

    private final ClienteDocumentoRepository clienteDocumentoRepository;
    private final DecolectaClient decolectaClient;

    @Override
    public Optional<ClienteDocumentoResponse> consultarDocumento(String codTipoDoc, String nroDocumento) {

        // 1. BD local primero — si ya está cacheado, ni se toca la API externa.
        Optional<ClienteDocumentoResponse> local = clienteDocumentoRepository.buscarPorDocumento(codTipoDoc, nroDocumento);
        if (local.isPresent()) {
            return local;
        }

        // 2. No está en caché: consultar Decolecta según el tipo de documento.
        if (TIPO_DNI.equals(codTipoDoc)) {
            return decolectaClient.consultarDni(nroDocumento).map(dni -> guardarDesdeReniec(nroDocumento, dni));
        }

        if (TIPO_RUC.equals(codTipoDoc)) {
            return decolectaClient.consultarRuc(nroDocumento).map(ruc -> guardarDesdeSunat(nroDocumento, ruc));
        }

        throw new DocumentoValidationException("El tipo de documento debe ser 01 (DNI) o 06 (RUC)");
    }

    @Override
    public ClienteDocumentoResponse registrarManual(RegistrarManualRequest request) {

        if (TIPO_DNI.equals(request.getCodTipoDoc())) {
            if (isBlank(request.getNombres()) || isBlank(request.getApellidoPaterno())) {
                throw new DocumentoValidationException("Para un DNI, nombres y apellido paterno son obligatorios");
            }
            return clienteDocumentoRepository.guardar(
                    request.getCodTipoDoc(), request.getNroDocumento(),
                    request.getNombres().trim(), request.getApellidoPaterno().trim(),
                    request.getApellidoMaterno() != null ? request.getApellidoMaterno().trim() : null,
                    null, null, null, null, null, "MANUAL");
        }

        if (TIPO_RUC.equals(request.getCodTipoDoc())) {
            if (isBlank(request.getRazonSocial())) {
                throw new DocumentoValidationException("Para un RUC, la razón social es obligatoria");
            }
            return clienteDocumentoRepository.guardar(
                    request.getCodTipoDoc(), request.getNroDocumento(),
                    null, null, null,
                    request.getRazonSocial().trim(), null, null,
                    request.getDireccion() != null ? request.getDireccion().trim() : null, null, "MANUAL");
        }

        throw new DocumentoValidationException("El tipo de documento debe ser 01 (DNI) o 06 (RUC)");
    }

    private ClienteDocumentoResponse guardarDesdeReniec(String nroDocumento, ReniecDniResponse dni) {
        return clienteDocumentoRepository.guardar(
                TIPO_DNI, nroDocumento,
                dni.getFirstName(), dni.getFirstLastName(), dni.getSecondLastName(),
                null, null, null, null, null, "RENIEC");
    }

    private ClienteDocumentoResponse guardarDesdeSunat(String nroDocumento, SunatRucResponse ruc) {
        return clienteDocumentoRepository.guardar(
                TIPO_RUC, nroDocumento,
                null, null, null,
                ruc.getRazonSocial(), ruc.getEstado(), ruc.getCondicion(), ruc.getDireccion(), ruc.getUbigeo(), "SUNAT");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
