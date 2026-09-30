package pe.ssimple.ssisfact_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicMessageRequest;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicMessageResponse;

import java.util.List;
import java.util.Optional;

// Único punto de acceso HTTP a la API de Anthropic (Messages API). A diferencia
// de DecolectaClient, aquí un fallo del proveedor SÍ es una situación excepcional
// (no un simple "no encontrado") — pero igual nunca se lanza hacia arriba: se
// devuelve Optional.empty() y es ClaudeServiceImpl quien decide la respuesta
// suave que recibe el usuario final ("Fact no está disponible en este momento").
@Slf4j
@Component
public class AnthropicClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public AnthropicClient(
            @Qualifier("anthropicRestTemplate") RestTemplate restTemplate,
            @Value("${anthropic.base-url}") String baseUrl,
            @Value("${anthropic.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public Optional<AnthropicMessageResponse> enviarMensaje(AnthropicMessageRequest request) {

        if (apiKey == null || apiKey.isBlank()) {
            log.error("ANTHROPIC_API_KEY no configurada — no se puede llamar a la API de Anthropic");
            return Optional.empty();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<AnthropicMessageResponse> response = restTemplate.exchange(
                    baseUrl + "/messages",
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    AnthropicMessageResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            log.warn("Anthropic respondió {} sin cuerpo utilizable", response.getStatusCode().value());
            return Optional.empty();

        } catch (HttpStatusCodeException e) {
            log.error("Anthropic respondió {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Anthropic no disponible/timeout: {}", e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Error inesperado llamando a Anthropic: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
