package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Assistant.AssistantLogItem;

import java.util.List;

public interface AssistantConversationLogRepository {
    void registrarLog(Long empresaId, Long usuarioId, String rol, String mensaje,
                       String toolName, String toolArgsJson, String toolResultJson);
    List<AssistantLogItem> listarRecientes(Long empresaId, int limit);
}
