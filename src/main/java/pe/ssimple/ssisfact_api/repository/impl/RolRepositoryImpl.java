package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Rol.RolAsignableResponse;
import pe.ssimple.ssisfact_api.repository.RolRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolRepositoryImpl implements RolRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<RolAsignableResponse> listarAsignables() {

        return jdbcTemplate.query(
                "SELECT id, nombre, descripcion, tipo FROM roles WHERE tipo = 'USER' AND estado = 1 ORDER BY nombre ASC",
                (rs, rowNum) -> mapear(rs));
    }

    @Override
    public List<RolAsignableResponse> listarAsignablesSuperadmin() {

        return jdbcTemplate.query(
                "SELECT id, nombre, descripcion, tipo FROM roles WHERE tipo IN ('ADMIN', 'USER') AND estado = 1 ORDER BY tipo ASC, nombre ASC",
                (rs, rowNum) -> mapear(rs));
    }

    private RolAsignableResponse mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new RolAsignableResponse(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("tipo"));
    }
}
