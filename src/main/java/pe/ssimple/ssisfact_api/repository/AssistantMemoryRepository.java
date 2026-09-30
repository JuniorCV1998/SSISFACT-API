package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Assistant.AssistantMemoriaResponse;

import java.util.List;

public interface AssistantMemoryRepository {
    void upsertMemoria(Long empresaId, String tipo, String clave, String valor);
    List<AssistantMemoriaResponse> listarTopMemorias(Long empresaId, int limit);
    List<AssistantMemoriaResponse> listarMemorias(Long empresaId);
    int eliminarMemoria(Long id, Long empresaId);
}
