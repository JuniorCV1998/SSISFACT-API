package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Sunat.SunatCredentialsInfo;

import java.util.Optional;

public interface SunatCredencialesRepository {
    Optional<SunatCredentialsInfo> obtenerCredenciales(Long empresaId);
    void actualizarCredenciales(Long empresaId, String usernameSunat, String passwordSunatEncriptado);
}
