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
import org.springframework.web.util.UriComponentsBuilder;
import pe.ssimple.ssisfact_api.dto.Decolecta.ReniecDniResponse;
import pe.ssimple.ssisfact_api.dto.Decolecta.SunatRucResponse;

import java.util.List;
import java.util.Optional;

// Único punto de acceso HTTP a Decolecta (api.decolecta.com). Nunca lanza
// excepción hacia arriba por un documento no encontrado o un fallo del
// proveedor: siempre devuelve Optional.empty() en esos casos — es
// ClienteDocumentoServiceImpl quien decide qué hacer con un "no encontrado".
@Slf4j
@Component
public class DecolectaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public DecolectaClient(
            @Qualifier("decolectaRestTemplate") RestTemplate restTemplate,
            @Value("${decolecta.base-url}") String baseUrl,
            @Value("${decolecta.token}") String token) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public Optional<ReniecDniResponse> consultarDni(String numero) {
        return get("/reniec/dni", numero, ReniecDniResponse.class);
    }

    public Optional<SunatRucResponse> consultarRuc(String numero) {
        return get("/sunat/ruc", numero, SunatRucResponse.class);
    }

    private <T> Optional<T> get(String path, String numero, Class<T> type) {

        String url = UriComponentsBuilder.fromUriString(baseUrl + path)
                .queryParam("numero", numero)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), type);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            return Optional.empty();

        } catch (HttpStatusCodeException e) {
            // Documento no encontrado (404) u otro error del proveedor: no es fatal.
            log.warn("Decolecta respondió {} en {} (numero={})", e.getStatusCode().value(), path, numero);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Decolecta no disponible/timeout en {}: {}", path, e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Error inesperado consultando Decolecta en {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }
}
