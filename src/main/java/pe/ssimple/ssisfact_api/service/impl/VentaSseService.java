package pe.ssimple.ssisfact_api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaPagoEventPayload;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// Canal de eventos en vivo por empresa (un solo canal, varios tipos de evento) —
// hoy empuja dos cosas: 1) "pago-confirmado" cuando una venta PENDIENTE recibe un
// pago (webhook de Yape/Plin o asociación manual, ver PagoDigitalServiceImpl) y
// 2) "notificacion-nueva" cada vez que se crea una notificación de la campana (ver
// NotificacionServiceImpl) — esto último para que el front pueda, por ejemplo, leer
// en voz alta un pago confirmado sin esperar al polling de la campana (cada 30s).
// Sin esto, el usuario no se entera de nada hasta que recarga o hace polling a mano.
//
// Un emisor (SseEmitter) por pestaña/cliente conectado, agrupados por empresaId
// (misma idea que los rate limiters por IP: un mapa en memoria, sin infraestructura
// nueva). Como pasa por el devtunnel/un proxy, se manda un "ping" periódico para
// que la conexión no se corte por inactividad.
@Slf4j
@Service
public class VentaSseService {

    private static final long TIMEOUT_MS = 30L * 60 * 1000; // 30 min; el front debe reconectar al cerrarse

    private final Map<Long, List<SseEmitter>> suscriptoresPorEmpresa = new ConcurrentHashMap<>();

    public SseEmitter suscribir(Long empresaId) {

        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        List<SseEmitter> emitters = suscriptoresPorEmpresa.computeIfAbsent(empresaId, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("conectado").data("ok"));
        } catch (IOException ex) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void publicarPagoConfirmado(Long empresaId, VentaPagoEventPayload payload) {

        List<SseEmitter> emitters = suscriptoresPorEmpresa.get(empresaId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("pago-confirmado").data(payload));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            }
        }

        log.info("[VentaSseService] evento pago-confirmado empujado — empresaId={} ventaId={} suscriptores={}",
                empresaId, payload.getVentaId(), emitters.size());
    }

    public void publicarNotificacion(Long empresaId, NotificacionResponse payload) {

        List<SseEmitter> emitters = suscriptoresPorEmpresa.get(empresaId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notificacion-nueva").data(payload));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            }
        }

        log.info("[VentaSseService] evento notificacion-nueva empujado — empresaId={} tipo={} suscriptores={}",
                empresaId, payload.getTipo(), emitters.size());
    }

    // Keep-alive cada 20s: evita que el devtunnel/navegador corte la conexión por
    // inactividad cuando no hay pagos reales entre medio.
    @Scheduled(fixedRate = 20_000)
    public void heartbeat() {

        for (Map.Entry<Long, List<SseEmitter>> entry : suscriptoresPorEmpresa.entrySet()) {
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException | IllegalStateException ex) {
                    entry.getValue().remove(emitter);
                }
            }
        }
    }
}
