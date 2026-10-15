package pe.ssimple.ssisfact_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.RecibirNotificacionPagoDigitalRequest;
import pe.ssimple.ssisfact_api.service.PagoDigitalService;
import pe.ssimple.ssisfact_api.service.PagoDigitalWebhookRateLimiterService;

// Público (sin JWT) — lo llama una app de automatización (MacroDroid/Tasker) en el
// celular del negocio al detectar la notificación de Yape o Plin, no un usuario
// logueado. La autenticación es el token propio de la empresa (header
// X-PagoDigital-Token), validado a mano en PagoDigitalService ya que no hay
// Authorization Bearer en este flujo.
@RestController
@RequestMapping("/webhooks/pagos-digitales")
@RequiredArgsConstructor
public class PagoDigitalWebhookController {

    private final PagoDigitalService pagoDigitalService;
    private final PagoDigitalWebhookRateLimiterService rateLimiterService;

    @PostMapping("/notificacion")
    public ResponseEntity<ApiResponse<Void>> recibirNotificacion(
            @RequestHeader(value = "X-PagoDigital-Token", required = false) String token,
            @Valid @RequestBody RecibirNotificacionPagoDigitalRequest request,
            HttpServletRequest httpRequest) {

        rateLimiterService.checkLimit(ipCliente(httpRequest));

        pagoDigitalService.recibirNotificacion(
                token, request.getMetodo(), request.getMonto(), request.getRemitente(), request.getTextoOriginal());

        return ResponseEntity.ok(ApiResponse.success("Notificación recibida", null));
    }

    // Mismo criterio que CompanyRegisterController.ipCliente: detrás del proxy
    // (Railway) la última entrada de X-Forwarded-For es la IP real.
    private String ipCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] ips = xff.split(",");
            return ips[ips.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
