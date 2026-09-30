package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantLogItem;
import pe.ssimple.ssisfact_api.repository.AssistantConversationLogRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssistantConversationLogRepositoryImpl implements AssistantConversationLogRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void registrarLog(Long empresaId, Long usuarioId, String rol, String mensaje,
                              String toolName, String toolArgsJson, String toolResultJson) {

        jdbcTemplate.update(
                "INSERT INTO assistant_conversation_log " +
                        "(empresa_id, usuario_id, rol, mensaje, tool_name, tool_args, tool_result) " +
                        "VALUES (?,?,?,?,?,?,?)",
                empresaId, usuarioId, rol, mensaje, toolName, toolArgsJson, toolResultJson);
    }

    @Override
    public List<AssistantLogItem> listarRecientes(Long empresaId, int limit) {

        return jdbcTemplate.query(
                "SELECT * FROM assistant_conversation_log WHERE empresa_id = ? " +
                        "ORDER BY fecha_creacion DESC LIMIT ?",
                AssistantConversationLogRepositoryImpl::mapear,
                empresaId, limit);
    }

    private static AssistantLogItem mapear(ResultSet rs, int rowNum) throws SQLException {
        AssistantLogItem item = new AssistantLogItem();
        item.setId(rs.getLong("id"));
        item.setEmpresaId(rs.getLong("empresa_id"));
        item.setUsuarioId(rs.getLong("usuario_id"));
        item.setRol(rs.getString("rol"));
        item.setMensaje(rs.getString("mensaje"));
        item.setToolName(rs.getString("tool_name"));
        item.setToolArgs(rs.getString("tool_args"));
        item.setToolResult(rs.getString("tool_result"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
        return item;
    }
}
