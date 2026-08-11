package pe.ssimple.ssisfact_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatBotRequest;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatBotResponse;
import pe.ssimple.ssisfact_api.exception.SunatAuthenticationException;
import pe.ssimple.ssisfact_api.exception.SunatManualReviewException;
import pe.ssimple.ssisfact_api.exception.SunatUnavailableException;

/**
 * Único punto de acceso HTTP al bot de automatización SUNAT (localhost:3000).
 * Nunca loguea el body de las requests: contiene username/password/RUC.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunatBotClient {

    private static final String GENERIC_UNAVAILABLE_MESSAGE =
            "No fue posible consultar SUNAT en este momento. Inténtalo nuevamente más tarde.";

    private final RestTemplate restTemplate;

    @Value("${sunat.bot.base-url}")
    private String baseUrl;

    public SunatBotResponse fetchNotifications(SunatBotRequest request) {
        return post("/api/platforms/sunat/actions/irABuzon", request);
    }

    public SunatBotResponse fetchMessages(SunatBotRequest request) {
        return post("/api/platforms/sunat/actions/irABuzonMensajes", request);
    }

    public SunatBotResponse login(SunatBotRequest request) {
        return post("/api/platforms/sunat/login", request);
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return false;
        }
    }

    private SunatBotResponse post(String path, SunatBotRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SunatBotRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<SunatBotResponse> response =
                    restTemplate.postForEntity(baseUrl + path, entity, SunatBotResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Bot SUNAT respondió 401 en {}: {}", path, safeStatus(e));
            throw new SunatAuthenticationException(
                    "No fue posible ingresar a SUNAT. Verifica que tu RUC, usuario y Clave SOL sean correctos e inténtalo nuevamente.");
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Bot SUNAT respondió 409 en {}: {}", path, safeStatus(e));
            throw new SunatManualReviewException(
                    "SUNAT está solicitando una acción manual. Ingresa a SUNAT para revisar o actualizar la información solicitada.");
        } catch (HttpStatusCodeException e) {
            log.error("Bot SUNAT respondió {} en {}: {}", e.getStatusCode().value(), path, safeStatus(e));
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        } catch (ResourceAccessException e) {
            log.error("Bot SUNAT no disponible/timeout en {}: {}", path, e.getMessage());
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    /** Extrae solo el campo "status" del body de error del bot, sin loguear el body completo. */
    private String safeStatus(HttpStatusCodeException e) {
        try {
            SunatBotResponse body = e.getResponseBodyAs(SunatBotResponse.class);
            return body != null ? body.getStatus() : "SIN_BODY";
        } catch (RestClientException parseError) {
            return "BODY_NO_PARSEABLE";
        }
    }
}
