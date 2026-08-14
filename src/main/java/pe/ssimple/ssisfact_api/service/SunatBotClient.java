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
import pe.ssimple.ssisfact_api.dto.Sunat.SunatJobPollResponse;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatJobStartResponse;
import pe.ssimple.ssisfact_api.exception.SunatAuthenticationException;
import pe.ssimple.ssisfact_api.exception.SunatManualReviewException;
import pe.ssimple.ssisfact_api.exception.SunatUnavailableException;

/**
 * Único punto de acceso HTTP al bot de automatización SUNAT (localhost:3000).
 * Nunca loguea el body de las requests: contiene username/password/RUC.
 *
 * irABuzon/irABuzonMensajes usan el modo job+polling del bot (pueden tardar
 * varios minutos en cuentas con buzones grandes): se dispara el job y se
 * hace polling hasta que termine, dentro de un único método bloqueante —
 * SunatService no necesita saber que por dentro esto ya no es una sola
 * llamada HTTP. login sigue siendo síncrono (validación rápida, sin listar).
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

    @Value("${sunat.bot.job.poll-interval-ms:3000}")
    private long pollIntervalMs;

    @Value("${sunat.bot.job.max-wait-ms:600000}")
    private long maxWaitMs;

    public SunatBotResponse fetchNotifications(SunatBotRequest request) {
        return runJob("irABuzon", request);
    }

    public SunatBotResponse fetchMessages(SunatBotRequest request) {
        return runJob("irABuzonMensajes", request);
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

    // ---------- irABuzon / irABuzonMensajes: modo job + polling ----------

    private SunatBotResponse runJob(String action, SunatBotRequest request) {
        String jobId = startJob(action, request);
        return pollUntilDone(action, jobId);
    }

    private String startJob(String action, SunatBotRequest request) {
        String path = "/api/platforms/sunat/actions/" + action + "/jobs";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SunatBotRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<SunatJobStartResponse> response =
                    restTemplate.postForEntity(baseUrl + path, entity, SunatJobStartResponse.class);
            SunatJobStartResponse body = response.getBody();
            if (body == null || body.getJobId() == null || body.getJobId().isBlank()) {
                log.error("Bot SUNAT: respuesta sin jobId al iniciar {}", action);
                throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
            }
            return body.getJobId();
        } catch (HttpStatusCodeException e) {
            log.error("Bot SUNAT respondió {} al iniciar job {}", e.getStatusCode().value(), action);
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        } catch (ResourceAccessException e) {
            log.error("Bot SUNAT no disponible/timeout al iniciar job {}: {}", action, e.getMessage());
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    private SunatBotResponse pollUntilDone(String action, String jobId) {
        String path = "/api/platforms/sunat/jobs/" + jobId;
        long deadline = System.currentTimeMillis() + maxWaitMs;

        while (System.currentTimeMillis() < deadline) {
            sleep(pollIntervalMs);

            SunatJobPollResponse poll;
            try {
                poll = restTemplate.getForObject(baseUrl + path, SunatJobPollResponse.class);
            } catch (HttpClientErrorException.NotFound e) {
                log.error("Bot SUNAT: job {} ({}) no existe o expiró", jobId, action);
                throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
            } catch (RestClientException e) {
                log.warn("Bot SUNAT: fallo transitorio consultando job {} ({}), reintentando: {}",
                        jobId, action, e.getMessage());
                continue;
            }

            if (poll == null) {
                continue;
            }
            if ("done".equalsIgnoreCase(poll.getStatus())) {
                return interpretResult(action, jobId, poll.getResult());
            }
            // "running" (u otro estado intermedio): seguir esperando.
        }

        log.error("Bot SUNAT: job {} ({}) no terminó dentro de {} ms", jobId, action, maxWaitMs);
        throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
    }

    private SunatBotResponse interpretResult(String action, String jobId, SunatBotResponse result) {
        if (result == null) {
            log.error("Bot SUNAT: job {} ({}) terminó sin resultado", jobId, action);
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        }
        if (result.isSuccess()) {
            return result;
        }

        String status = result.getStatus();
        if ("CREDENCIALES_INVALIDAS".equals(status)) {
            log.warn("Bot SUNAT: job {} ({}) terminó con CREDENCIALES_INVALIDAS", jobId, action);
            throw new SunatAuthenticationException(
                    "No fue posible ingresar a SUNAT. Verifica que tu RUC, usuario y Clave SOL sean correctos e inténtalo nuevamente.");
        }
        if ("REQUIERE_REVISION_MANUAL".equals(status)) {
            log.warn("Bot SUNAT: job {} ({}) terminó con REQUIERE_REVISION_MANUAL", jobId, action);
            throw new SunatManualReviewException(
                    "SUNAT está solicitando una acción manual. Ingresa a SUNAT para revisar o actualizar la información solicitada.");
        }

        log.error("Bot SUNAT: job {} ({}) terminó con error: {}", jobId, action, status);
        throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SunatUnavailableException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    // ---------- login: sigue siendo una llamada síncrona simple ----------

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
