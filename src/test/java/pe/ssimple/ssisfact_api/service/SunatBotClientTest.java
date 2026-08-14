package pe.ssimple.ssisfact_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatBotRequest;
import pe.ssimple.ssisfact_api.exception.SunatAuthenticationException;
import pe.ssimple.ssisfact_api.exception.SunatManualReviewException;
import pe.ssimple.ssisfact_api.exception.SunatUnavailableException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Verifica que SunatBotClient traduce cada respuesta del bot al estado
 * funcional correcto. login sigue siendo síncrono; irABuzon/irABuzonMensajes
 * usan job+polling (se fuerza pollIntervalMs/maxWaitMs bajos vía reflexión
 * para que los tests no esperen segundos reales).
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
        setField(client, "pollIntervalMs", 1L);
        setField(client, "maxWaitMs", 2000L);
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

    // ---------- login: síncrono ----------

    @Test
    void loginShouldMapUnauthorizedToAuthenticationException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/login"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"status\":\"CREDENCIALES_INVALIDAS\"}"));

        assertThrows(SunatAuthenticationException.class, () -> client.login(anyRequest()));
    }

    @Test
    void loginShouldMapConnectionFailureToUnavailableException() {
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

    // ---------- irABuzon/irABuzonMensajes: job + polling ----------

    @Test
    void shouldReturnResultWhenJobFinishesSuccessfully() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-1\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-1"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-1\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-1"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-1\",\"status\":\"done\",\"result\":"
                                + "{\"success\":true,\"action\":\"irABuzon\",\"data\":{\"total\":0,\"totalPaginas\":1,\"paginas\":[]}}}"));

        var response = client.fetchNotifications(anyRequest());

        assertEquals(true, response.isSuccess());
        assertEquals(0, response.getData().getTotal());
    }

    @Test
    void shouldMapCredencialesInvalidasResultToAuthenticationException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzonMensajes/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-2\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-2"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-2\",\"status\":\"done\",\"result\":"
                                + "{\"success\":false,\"action\":\"irABuzonMensajes\",\"status\":\"CREDENCIALES_INVALIDAS\"}}"));

        assertThrows(SunatAuthenticationException.class, () -> client.fetchMessages(anyRequest()));
    }

    @Test
    void shouldMapRequiereRevisionManualResultToManualReviewException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-3\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-3"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-3\",\"status\":\"done\",\"result\":"
                                + "{\"success\":false,\"action\":\"irABuzon\",\"status\":\"REQUIERE_REVISION_MANUAL\"}}"));

        assertThrows(SunatManualReviewException.class, () -> client.fetchNotifications(anyRequest()));
    }

    @Test
    void shouldMapUnexpectedErrorResultToUnavailableException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-4\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-4"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-4\",\"status\":\"done\",\"result\":"
                                + "{\"success\":false,\"action\":\"irABuzon\",\"status\":\"ERROR_INESPERADO\"}}"));

        assertThrows(SunatUnavailableException.class, () -> client.fetchNotifications(anyRequest()));
    }

    @Test
    void shouldMapJobNotFoundToUnavailableException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-5\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-5"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(SunatUnavailableException.class, () -> client.fetchNotifications(anyRequest()));
    }

    @Test
    void shouldMapStartJobServerErrorToUnavailableException() {
        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        assertThrows(SunatUnavailableException.class, () -> client.fetchNotifications(anyRequest()));
    }

    @Test
    void shouldGiveUpAfterMaxWaitIfJobNeverFinishes() throws Exception {
        setField(client, "maxWaitMs", 5L);
        setField(client, "pollIntervalMs", 1L);

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/actions/irABuzon/jobs"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-6\",\"status\":\"running\"}"));

        server.expect(requestTo(BASE_URL + "/api/platforms/sunat/jobs/job-6"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"job-6\",\"status\":\"running\"}"));

        assertThrows(SunatUnavailableException.class, () -> client.fetchNotifications(anyRequest()));
    }
}
