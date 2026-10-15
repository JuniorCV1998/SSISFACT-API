package pe.ssimple.ssisfact_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.exception.RateLimitExceededException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Límite de llamadas/minuto por IP al webhook público de pagos digitales (Yape/Plin).
// Protege contra fuerza bruta del token (probar tokens al azar) y contra spam del
// celular si la app de automatización entra en loop. Mismo enfoque en memoria que
// RegisterRateLimiterService.
@Service
public class PagoDigitalWebhookRateLimiterService {

    private static final long VENTANA_MS = 60_000;

    private final Map<String, Deque<Long>> llamadasPorIp = new ConcurrentHashMap<>();

    @Value("${pagos-digitales.rate-limit.consultas-por-minuto}")
    private int consultasPorMinuto;

    public void checkLimit(String ip) {

        long ahora = System.currentTimeMillis();
        Deque<Long> timestamps = llamadasPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < ahora - VENTANA_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= consultasPorMinuto) {
                throw new RateLimitExceededException(
                        "Demasiadas notificaciones desde esa conexión, espera un minuto e intenta de nuevo");
            }
            timestamps.addLast(ahora);
        }
    }
}
