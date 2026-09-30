package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantLearningQueueItem;
import pe.ssimple.ssisfact_api.repository.AssistantLearningQueueRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssistantLearningQueueRepositoryImpl implements AssistantLearningQueueRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void encolarEvento(Long empresaId, String tipoEvento, String payloadJson) {

        jdbcTemplate.update(
                "INSERT INTO assistant_learning_queue (empresa_id, tipo_evento, payload) VALUES (?,?,?)",
                empresaId, tipoEvento, payloadJson);
    }

    @Override
    public List<AssistantLearningQueueItem> listarPendientes(int limit) {

        return jdbcTemplate.query(
                "SELECT id, empresa_id, tipo_evento, payload, fecha_creacion " +
                        "FROM assistant_learning_queue WHERE procesado = 0 " +
                        "ORDER BY fecha_creacion ASC LIMIT ?",
                AssistantLearningQueueRepositoryImpl::mapear,
                limit);
    }

    @Override
    public void marcarProcesado(Long id) {

        jdbcTemplate.update(
                "UPDATE assistant_learning_queue SET procesado = 1, fecha_procesado = NOW() WHERE id = ?",
                id);
    }

    private static AssistantLearningQueueItem mapear(ResultSet rs, int rowNum) throws SQLException {
        AssistantLearningQueueItem item = new AssistantLearningQueueItem();
        item.setId(rs.getLong("id"));
        item.setEmpresaId(rs.getLong("empresa_id"));
        item.setTipoEvento(rs.getString("tipo_evento"));
        item.setPayload(rs.getString("payload"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
        return item;
    }
}
