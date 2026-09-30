package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantMemoriaResponse;
import pe.ssimple.ssisfact_api.repository.AssistantMemoryRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssistantMemoryRepositoryImpl implements AssistantMemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsertMemoria(Long empresaId, String tipo, String clave, String valor) {

        jdbcTemplate.update(
                "INSERT INTO assistant_memory (empresa_id, tipo, clave, valor, usos) VALUES (?,?,?,?,1) " +
                        "ON DUPLICATE KEY UPDATE valor = VALUES(valor), usos = usos + 1, " +
                        "fecha_actualizacion = CURRENT_TIMESTAMP",
                empresaId, tipo, clave, valor);
    }

    @Override
    public List<AssistantMemoriaResponse> listarTopMemorias(Long empresaId, int limit) {

        return jdbcTemplate.query(
                "SELECT * FROM assistant_memory WHERE empresa_id = ? ORDER BY usos DESC LIMIT ?",
                AssistantMemoryRepositoryImpl::mapear,
                empresaId, limit);
    }

    @Override
    public List<AssistantMemoriaResponse> listarMemorias(Long empresaId) {

        return jdbcTemplate.query(
                "SELECT * FROM assistant_memory WHERE empresa_id = ? ORDER BY tipo, usos DESC",
                AssistantMemoryRepositoryImpl::mapear,
                empresaId);
    }

    @Override
    public int eliminarMemoria(Long id, Long empresaId) {

        return jdbcTemplate.update(
                "DELETE FROM assistant_memory WHERE id = ? AND empresa_id = ?",
                id, empresaId);
    }

    private static AssistantMemoriaResponse mapear(ResultSet rs, int rowNum) throws SQLException {
        AssistantMemoriaResponse item = new AssistantMemoriaResponse();
        item.setId(rs.getLong("id"));
        item.setTipo(rs.getString("tipo"));
        item.setClave(rs.getString("clave"));
        item.setValor(rs.getString("valor"));
        item.setUsos(rs.getInt("usos"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
        Timestamp fa = rs.getTimestamp("fecha_actualizacion");
        if (fa != null) item.setFechaActualizacion(fa.toLocalDateTime());
        return item;
    }
}
