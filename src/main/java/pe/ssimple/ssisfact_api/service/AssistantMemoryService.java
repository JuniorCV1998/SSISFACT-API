package pe.ssimple.ssisfact_api.service;

public interface AssistantMemoryService {

    // Texto formateado listo para insertar en el system prompt de Claude.
    // Cacheado 5 min por empresa; se invalida en cada save/correction de esa empresa.
    String getMemoryContext(Long empresaId);

    void saveAlias(Long empresaId, String textoUsuario, String productoResuelto);

    void savePreference(Long empresaId, String clave, String valor);

    void saveCorrection(Long empresaId, String textoOriginal, String correccion);

    void markClienteFrecuente(Long empresaId, String clienteRef);

    // Encola el turno para que LearningProcessorService extraiga patrones más tarde
    // (análisis pesado fuera del hilo de la request).
    void learnFromConversation(Long empresaId, Long usuarioId, String mensajeUsuario, String respuestaFinal);
}
