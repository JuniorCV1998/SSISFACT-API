package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Assistant.AssistantLearningQueueItem;

import java.util.List;

public interface AssistantLearningQueueRepository {
    void encolarEvento(Long empresaId, String tipoEvento, String payloadJson);
    List<AssistantLearningQueueItem> listarPendientes(int limit);
    void marcarProcesado(Long id);
}
