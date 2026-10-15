package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.impl.VentaSseService;

// Canal en vivo (Server-Sent Events) para que el frontend se entere al instante
// cuando una venta PENDIENTE pasa a PAGADA (hoy: confirmación automática de
// Yape/Plin, ver VentaSseService) sin tener que recargar la pantalla ni hacer polling.
//
// Nota para el front: EventSource nativo del navegador no permite mandar el header
// Authorization, así que este endpoint se consume con fetch + ReadableStream (o una
// librería como @microsoft/fetch-event-source) mandando el Bearer token como en
// cualquier otro request — no con `new EventSource(url)` directo.
@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaEventsController {

    private final VentaSseService ventaSseService;

    @GetMapping(value = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter suscribirse(@AuthenticationPrincipal CustomUserDetails user) {
        return ventaSseService.suscribir(user.getEmpresaId());
    }
}
