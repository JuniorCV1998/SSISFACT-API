package pe.ssimple.ssisfact_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappVincularResponse;
import pe.ssimple.ssisfact_api.exception.WhatsappIntegrationException;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Único punto de acceso HTTP a Evolution API. Nunca loguea números de teléfono
 * completos ni el contenido de los mensajes/documentos enviados.
 *
 * Las formas exactas de respuesta de /instance/create, /instance/fetchInstances
 * y /message/sendMedia no estaban en el contrato original (venía de otro proyecto);
 * se parsean de forma defensiva con JsonNode y se terminan de ajustar con la
 * verificación en vivo contra la instancia real del usuario.
 */
@Slf4j
@Component
public class EvolutionApiClient {

    private static final String GENERIC_UNAVAILABLE_MESSAGE =
            "No fue posible completar la operación de WhatsApp en este momento. Inténtalo nuevamente más tarde.";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public EvolutionApiClient(
            @Qualifier("evolutionRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${evolution.base-url}") String baseUrl,
            @Value("${evolution.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public WhatsappVincularResponse crearInstancia(String instanceName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("instanceName", instanceName);
        body.put("qrcode", true);
        body.put("integration", "WHATSAPP-BAILEYS");

        JsonNode root = post("/instance/create", body);
        JsonNode qrcode = root.path("qrcode");
        String base64 = qrcode.path("base64").asText(null);
        String pairingCode = qrcode.path("pairingCode").asText(null);

        if (base64 == null) {
            log.error("Evolution API: respuesta de /instance/create sin qrcode.base64");
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
        return new WhatsappVincularResponse(base64, pairingCode);
    }

    public void eliminarInstancia(String instanceName) {
        try {
            restTemplate.exchange(baseUrl + "/instance/delete/" + instanceName,
                    HttpMethod.DELETE, new HttpEntity<>(headers()), Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            // ya no existía: no es error, es el estado deseado.
        } catch (RestClientException e) {
            log.error("Evolution API: fallo al eliminar instancia: {}", e.getMessage());
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    public String obtenerEstadoConexion(String instanceName) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/instance/connectionState/" + instanceName,
                    HttpMethod.GET, new HttpEntity<>(headers()), String.class);
            JsonNode root = parse(response.getBody());
            String state = root.path("instance").path("state").asText(null);
            return state != null ? state : "close";
        } catch (HttpClientErrorException.NotFound e) {
            return "close";
        } catch (RestClientException e) {
            log.error("Evolution API: fallo al consultar estado de conexión: {}", e.getMessage());
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    public String obtenerNumeroVinculado(String instanceName) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/instance/fetchInstances?instanceName=" + instanceName,
                    HttpMethod.GET, new HttpEntity<>(headers()), String.class);
            JsonNode root = parse(response.getBody());
            JsonNode primero = root.isArray() && !root.isEmpty() ? root.get(0) : root;

            String ownerJid = primero.path("ownerJid").asText(null);
            if (ownerJid == null) {
                ownerJid = primero.path("instance").path("ownerJid").asText(null);
            }
            if (ownerJid == null || ownerJid.isBlank()) {
                return null;
            }
            int arroba = ownerJid.indexOf('@');
            return arroba > 0 ? ownerJid.substring(0, arroba) : ownerJid;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            log.error("Evolution API: fallo al consultar número vinculado: {}", e.getMessage());
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    public void enviarTexto(String instanceName, String numero, String texto) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("number", normalizarNumeroPeru(numero));
        body.put("text", texto);

        post("/message/sendText/" + instanceName, body);
    }

    public void enviarDocumento(String instanceName, String numero, byte[] pdf, String nombreArchivo, String caption) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("number", normalizarNumeroPeru(numero));
        body.put("mediatype", "document");
        body.put("mimetype", "application/pdf");
        body.put("fileName", nombreArchivo);
        body.put("caption", caption);
        body.put("media", Base64.getEncoder().encodeToString(pdf));

        post("/message/sendMedia/" + instanceName, body);
    }

    // ---------- helpers ----------

    private JsonNode post(String path, Map<String, Object> body) {
        HttpHeaders headers = headers();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(baseUrl + path, new HttpEntity<>(body, headers), String.class);
            return parse(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Evolution API respondió {} en {}", e.getStatusCode().value(), path);
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        } catch (ResourceAccessException e) {
            log.error("Evolution API no disponible/timeout en {}: {}", path, e.getMessage());
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    private JsonNode parse(String body) {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            log.error("Evolution API: respuesta no parseable como JSON");
            throw new WhatsappIntegrationException(GENERIC_UNAVAILABLE_MESSAGE);
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        return headers;
    }

    /** Normaliza celulares guardados en formato local (9 dígitos, Perú) a formato internacional. */
    private String normalizarNumeroPeru(String numero) {
        String limpio = numero.replaceAll("[^0-9]", "");
        return limpio.length() == 9 ? "51" + limpio : limpio;
    }
}
