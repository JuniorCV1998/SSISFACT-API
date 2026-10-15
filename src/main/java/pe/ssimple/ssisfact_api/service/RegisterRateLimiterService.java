package pe.ssimple.ssisfact_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.exception.RateLimitExceededException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Límite de consultas/minuto por IP para los endpoints públicos de /register
// (consultarRuc y consultarDni). Mismo enfoque en memoria que AssistantRateLimiterService.
// Cuenta TODAS las consultas, incluidas las que salen de la caché local: el objetivo
// es proteger también la BD y la cuota de Decolecta de un bot que barra documentos.
@Service
public class RegisterRateLimiterService {

    private static final long VENTANA_MS = 60_000;

    private final Map<String, Deque<Long>> consultasPorIp = new ConcurrentHashMap<>();

    @Value("${register.rate-limit.consultas-por-minuto}")
    private int consultasPorMinuto;

    public void checkLimit(String ip) {

        long ahora = System.currentTimeMillis();
        Deque<Long> timestamps = consultasPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < ahora - VENTANA_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= consultasPorMinuto) {
                throw new RateLimitExceededException(
                        "Demasiadas consultas desde tu conexión, espera un minuto e intenta de nuevo");
            }
            timestamps.addLast(ahora);
        }
    }
}
