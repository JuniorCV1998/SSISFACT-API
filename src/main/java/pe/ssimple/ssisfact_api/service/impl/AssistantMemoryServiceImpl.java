package pe.ssimple.ssisfact_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantMemoriaResponse;
import pe.ssimple.ssisfact_api.repository.AssistantLearningQueueRepository;
import pe.ssimple.ssisfact_api.repository.AssistantMemoryRepository;
import pe.ssimple.ssisfact_api.service.AssistantMemoryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Memoria persistente por empresa para el asistente "Fact". El contexto formateado
// (getMemoryContext) se cachea en memoria del proceso por assistant.memory.cache-ttl-seconds
// (mismo idioma ConcurrentHashMap self-cleaning que TokenBlacklistService, sin Spring
// Cache/Bucket4j) porque se lee en cada turno de chat; cualquier escritura invalida
// de inmediato la entrada de esa empresa para que una corrección se vea en el
// siguiente mensaje, sin esperar hasta que expire el TTL.
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantMemoryServiceImpl implements AssistantMemoryService {

    private static final String TIPO_ALIAS = "ALIAS";
    private static final String TIPO_PREFERENCIA = "PREFERENCIA";
    private static final String TIPO_CORRECCION = "CORRECCION";
    private static final String TIPO_CLIENTE_FRECUENTE = "CLIENTE_FRECUENTE";

    private final AssistantMemoryRepository assistantMemoryRepository;
    private final AssistantLearningQueueRepository assistantLearningQueueRepository;
    private final ObjectMapper objectMapper;

    @Value("${assistant.memory.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    @Value("${assistant.memory.max-context-items:20}")
    private int maxContextItems;

    private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public String getMemoryContext(Long empresaId) {

        CacheEntry entry = cache.get(empresaId);
        long ahora = System.currentTimeMillis();
        if (entry != null && entry.expiresAtMs > ahora) {
            return entry.texto;
        }

        List<AssistantMemoriaResponse> memorias = assistantMemoryRepository.listarTopMemorias(empresaId, maxContextItems);
        String texto = formatear(memorias);
        cache.put(empresaId, new CacheEntry(texto, ahora + cacheTtlSeconds * 1000));
        return texto;
    }

    @Override
    public void saveAlias(Long empresaId, String textoUsuario, String productoResuelto) {
        assistantMemoryRepository.upsertMemoria(empresaId, TIPO_ALIAS, normalizar(textoUsuario), productoResuelto);
        cache.remove(empresaId);
    }

    @Override
    public void savePreference(Long empresaId, String clave, String valor) {
        assistantMemoryRepository.upsertMemoria(empresaId, TIPO_PREFERENCIA, normalizar(clave), valor);
        cache.remove(empresaId);
    }

    @Override
    public void saveCorrection(Long empresaId, String textoOriginal, String correccion) {
        assistantMemoryRepository.upsertMemoria(empresaId, TIPO_CORRECCION, normalizar(textoOriginal), correccion);
        cache.remove(empresaId);
    }

    @Override
    public void markClienteFrecuente(Long empresaId, String clienteRef) {
        assistantMemoryRepository.upsertMemoria(empresaId, TIPO_CLIENTE_FRECUENTE, normalizar(clienteRef), clienteRef);
        cache.remove(empresaId);
    }

    @Override
    public void learnFromConversation(Long empresaId, Long usuarioId, String mensajeUsuario, String respuestaFinal) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("usuarioId", usuarioId);
            payload.put("mensajeUsuario", mensajeUsuario);
            payload.put("respuestaFinal", respuestaFinal);
            assistantLearningQueueRepository.encolarEvento(empresaId, "PATRON_DETECTADO", objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("No se pudo encolar el turno para aprendizaje de la empresa {}: {}", empresaId, e.getMessage());
        }
    }

    // Claves cortas y en minúsculas para que "Margarita"/"margarita " colapsen
    // en la misma entrada de assistant_memory en vez de duplicarse.
    private String normalizar(String texto) {
        if (texto == null) return "";
        String limpio = texto.trim().toLowerCase();
        return limpio.length() > 150 ? limpio.substring(0, 150) : limpio;
    }

    private String formatear(List<AssistantMemoriaResponse> memorias) {
        if (memorias.isEmpty()) {
            return "(sin memoria previa registrada para esta empresa)";
        }

        StringBuilder sb = new StringBuilder();
        agregarGrupo(sb, "Alias de productos conocidos", memorias, TIPO_ALIAS);
        agregarGrupo(sb, "Preferencias detectadas", memorias, TIPO_PREFERENCIA);
        agregarGrupo(sb, "Correcciones del usuario (prioridad alta)", memorias, TIPO_CORRECCION);
        agregarGrupo(sb, "Clientes frecuentes", memorias, TIPO_CLIENTE_FRECUENTE);
        return sb.toString();
    }

    private void agregarGrupo(StringBuilder sb, String titulo, List<AssistantMemoriaResponse> memorias, String tipo) {
        List<AssistantMemoriaResponse> delTipo = memorias.stream().filter(m -> tipo.equals(m.getTipo())).toList();
        if (delTipo.isEmpty()) return;

        sb.append(titulo).append(":\n");
        for (AssistantMemoriaResponse m : delTipo) {
            sb.append("- ").append(m.getClave()).append(": ").append(m.getValor()).append("\n");
        }
    }

    private static class CacheEntry {
        final String texto;
        final long expiresAtMs;

        CacheEntry(String texto, long expiresAtMs) {
            this.texto = texto;
            this.expiresAtMs = expiresAtMs;
        }
    }
}
