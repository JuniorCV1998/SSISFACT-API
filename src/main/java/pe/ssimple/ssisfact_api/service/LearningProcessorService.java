package pe.ssimple.ssisfact_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantLearningQueueItem;
import pe.ssimple.ssisfact_api.repository.AssistantLearningQueueRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Procesa la cola de aprendizaje del asistente "Fact" cada hora, en batch acotado
// y fuera del hilo del scheduler (@Async) para no bloquear nada más — ver
// AssistantMemoryServiceImpl.learnFromConversation, que es quien encola cada turno.
//
// v1: agrupa por empresa y cuenta repeticiones de (mensajeUsuario -> respuestaFinal);
// cuando un mismo patrón se repite 3+ veces para una empresa, lo promueve a
// assistant_memory como PREFERENCIA. Análisis más sofisticado queda para después.
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningProcessorService {

    private static final int BATCH_SIZE = 200;
    private static final int UMBRAL_REPETICIONES = 3;

    private final AssistantLearningQueueRepository assistantLearningQueueRepository;
    private final AssistantMemoryService assistantMemoryService;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 * * * *")
    @Async
    public void procesarAprendizaje() {

        List<AssistantLearningQueueItem> pendientes = assistantLearningQueueRepository.listarPendientes(BATCH_SIZE);
        if (pendientes.isEmpty()) {
            return;
        }

        Map<Long, Map<String, Integer>> conteosPorEmpresa = new HashMap<>();

        for (AssistantLearningQueueItem evento : pendientes) {
            try {
                String patron = extraerPatron(evento.getPayload());
                if (patron != null && !patron.isBlank()) {
                    conteosPorEmpresa
                            .computeIfAbsent(evento.getEmpresaId(), id -> new HashMap<>())
                            .merge(patron, 1, Integer::sum);
                }
            } catch (Exception e) {
                log.warn("No se pudo procesar evento de aprendizaje {}: {}", evento.getId(), e.getMessage());
            } finally {
                assistantLearningQueueRepository.marcarProcesado(evento.getId());
            }
        }

        conteosPorEmpresa.forEach((empresaId, conteos) ->
                conteos.forEach((patron, veces) -> {
                    if (veces >= UMBRAL_REPETICIONES) {
                        assistantMemoryService.savePreference(empresaId, patron, "detectado " + veces + " veces");
                    }
                }));

        log.debug("LearningProcessorService: {} eventos procesados, {} empresas con patrones", pendientes.size(), conteosPorEmpresa.size());
    }

    private String extraerPatron(String payloadJson) throws Exception {
        JsonNode node = objectMapper.readTree(payloadJson);
        JsonNode mensajeNode = node.get("mensajeUsuario");
        if (mensajeNode == null || mensajeNode.isNull()) return null;
        return mensajeNode.asText().trim().toLowerCase();
    }
}
