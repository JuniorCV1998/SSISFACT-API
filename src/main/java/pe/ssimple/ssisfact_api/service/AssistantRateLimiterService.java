package pe.ssimple.ssisfact_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.exception.RateLimitExceededException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Límite de mensajes/minuto por usuario para /assistant/chat, en memoria del proceso
// (mismo idioma ConcurrentHashMap que TokenBlacklistService) — no hay Bucket4j en
// este proyecto y agregarlo solo para este contador sería desproporcionado.
@Service
public class AssistantRateLimiterService {

    private static final long VENTANA_MS = 60_000;

    private final Map<Long, Deque<Long>> mensajesPorUsuario = new ConcurrentHashMap<>();

    @Value("${assistant.rate-limit.messages-per-minute}")
    private int mensajesPorMinuto;

    public void checkLimit(Long usuarioId) {

        long ahora = System.currentTimeMillis();
        Deque<Long> timestamps = mensajesPorUsuario.computeIfAbsent(usuarioId, id -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < ahora - VENTANA_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= mensajesPorMinuto) {
                throw new RateLimitExceededException(
                        "Límite de " + mensajesPorMinuto + " mensajes por minuto alcanzado, espera un momento");
            }
            timestamps.addLast(ahora);
        }
    }
}
