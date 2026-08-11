package pe.ssimple.ssisfact_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatBotRequest;
import pe.ssimple.ssisfact_api.exception.SunatAuthenticationException;
import pe.ssimple.ssisfact_api.exception.SunatManualReviewException;
import pe.ssimple.ssisfact_api.exception.SunatUnavailableException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpMethod.POST;

/**
 * Verifica que SunatBotClient traduce cada respuesta del bot al estado
 * funcional correcto. RestTemplate lanza excepción para cualquier 4xx/5xx,
 * así que esto cubre el punto ciego que un mock de SunatBotClient no prueba.
 */
class SunatBotClientTest {

    private static final String BASE_URL = "http://localhost:3000";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private SunatBotClient client;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new SunatBotClient(restTemplate);
        setField(client, "baseUrl", BASE_URL);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = SunatBotClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static SunatBotRequest anyRequest() {
        return SunatBotRequest.builder()
                .credentials(SunatBotRequest.Credentials.builder().username("u").password("p").build())
                .build();
    }

    @Test
    void shouldMapUnauthorizedToAuthenticationException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/login"))
                .andExpect(method(POST))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"status\":\"CREDENCIALES_INVALIDAS\"}"));

        assertThrows(SunatAuthenticationException.class, () -> client.login(anyRequest()));
    }

    @Test
    void shouldMapConflictToManualReviewException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon"))
                .andExpect(method(POST))
                .andRespond(withStatus(org.springframework.http.HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"status\":\"REQUIERE_REVISION_MANUAL\"}"));

        assertThrows(SunatManualReviewException.class, () -> client.fetchNotifications(anyRequest()));
    }

    @Test
    void shouldMapServerErrorToUnavailableException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzonMensajes"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        assertThrows(SunatUnavailableException.class, () -> client.fetchMessages(anyRequest()));
    }

    @Test
    void shouldMapConnectionFailureToUnavailableException() {
        RestTemplate brokenRestTemplate = new RestTemplate() {
            @Override
            public <T> org.springframework.http.ResponseEntity<T> postForEntity(
                    String url, Object request, Class<T> responseType, Object... uriVariables) {
                throw new ResourceAccessException("Connection refused");
            }
        };
        SunatBotClient brokenClient = new SunatBotClient(brokenRestTemplate);
        assertThrows(SunatUnavailableException.class, () -> {
            try {
                setField(brokenClient, "baseUrl", BASE_URL);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            brokenClient.login(anyRequest());
        });
    }
}
