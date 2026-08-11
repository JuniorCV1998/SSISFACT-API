package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatCredentialsInfo;
import pe.ssimple.ssisfact_api.repository.SunatCredencialesRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SunatCredencialesRepositoryImpl implements SunatCredencialesRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<SunatCredentialsInfo> obtenerCredenciales(Long empresaId) {
        List<SunatCredentialsInfo> result = jdbcTemplate.query(
                "CALL sp_obtener_credenciales_sunat(?)",
                (rs, rowNum) -> new SunatCredentialsInfo(
                        rs.getString("ruc"),
                        rs.getString("username_sunat"),
                        rs.getString("password_sunat")),
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public void actualizarCredenciales(Long empresaId, String usernameSunat, String passwordSunatEncriptado) {
        String estado = jdbcTemplate.queryForObject(
                "CALL sp_actualizar_credenciales_sunat(?,?,?)",
                (rs, rowNum) -> rs.getString("estado"),
                empresaId, usernameSunat, passwordSunatEncriptado);

        if (!"OK".equals(estado)) {
            throw new IllegalArgumentException("Empresa no encontrada");
        }
    }
}
