package pe.ssimple.ssisfact_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ssimple.ssisfact_api.dto.Sunat.*;
import pe.ssimple.ssisfact_api.exception.SunatAuthenticationException;
import pe.ssimple.ssisfact_api.exception.SunatCredentialsNotConfiguredException;
import pe.ssimple.ssisfact_api.exception.SunatManualReviewException;
import pe.ssimple.ssisfact_api.exception.SunatUnavailableException;
import pe.ssimple.ssisfact_api.repository.SunatCredencialesRepository;
import pe.ssimple.ssisfact_api.repository.SunatMensajeRepository;
import pe.ssimple.ssisfact_api.repository.SunatNotificacionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SunatServiceTest {

    @Mock
    private SunatCredencialesRepository credencialesRepository;

    @Mock
    private SunatNotificacionRepository notificacionRepository;

    @Mock
    private SunatMensajeRepository mensajeRepository;

    @Mock
    private SunatCache sunatCache;

    @Mock
    private SunatBotClient sunatBotClient;

    @Mock
    private SunatCredentialCrypto sunatCredentialCrypto;

    @InjectMocks
    private SunatService sunatService;

    private static SunatCredentialsInfo configuredCredentials() {
        return new SunatCredentialsInfo("10410481605", "GEORICAT", "encrypted");
    }

    private static SunatBotResponse botResponseWithOneNotification() {
        SunatBotNotification notification = new SunatBotNotification();
        notification.setId("980406926");
        notification.setAsunto("Resolución");
        notification.setFechaPublicacion("21/04/2026 22:14:52");
        notification.setCategoria("14");
        notification.setLeido(true);
        notification.setDestacado(false);
        notification.setUrgente(false);
        notification.setTieneAdjunto(true);

        SunatBotPage page = new SunatBotPage();
        page.setPagina(1);
        page.setNotificaciones(List.of(notification));

        SunatBotData data = new SunatBotData();
        data.setTotal(1);
        data.setTotalPaginas(1);
        data.setPaginas(List.of(page));

        SunatBotResponse response = new SunatBotResponse();
        response.setSuccess(true);
        response.setAction("irABuzon");
        response.setData(data);
        return response;
    }

    private static SunatPageResult<SunatNotificationDto> emptyNotificationPage() {
        return new SunatPageResult<>(List.of(), 0);
    }

    private static SunatPageResult<SunatMessageDto> emptyMessagePage() {
        return new SunatPageResult<>(List.of(), 0);
    }

    // ---------- Notificaciones: cache-guard de sincronización ----------

    @Test
    void shouldNotCallBotWhenLastSyncIsStillValid() {
        when(sunatCache.get("sunat:sync:notificaciones:15")).thenReturn(Optional.of(Instant.now()));
        when(notificacionRepository.listar(15L, 0, 25))
                .thenReturn(new SunatPageResult<>(List.of(new SunatNotificationDto("1", "Asunto", "01/01/2026 10:00:00", "14", null, true, false, false, true)), 1));

        SunatApiResponse<SunatNotificationListResponse> response = sunatService.getNotificaciones(15L, 0, 25, false);

        assertTrue(response.isSuccess());
        assertEquals("CACHE", response.getSource());
        assertEquals(1, response.getData().getTotal());
        verifyNoInteractions(sunatBotClient, credencialesRepository);
    }

    @Test
    void shouldSyncAndPersistWhenGuardExpired() {
        when(sunatCache.get("sunat:sync:notificaciones:20")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(20L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchNotifications(any(SunatBotRequest.class))).thenReturn(botResponseWithOneNotification());
        when(notificacionRepository.listar(eq(20L), eq(0), eq(25)))
                .thenReturn(new SunatPageResult<>(List.of(new SunatNotificationDto("980406926", "Resolución", "21/04/2026 22:14:52", "14", null, true, false, false, true)), 1));

        SunatApiResponse<SunatNotificationListResponse> response = sunatService.getNotificaciones(20L, 0, 25, false);

        assertTrue(response.isSuccess());
        assertEquals("SUNAT", response.getSource());
        assertEquals(1, response.getData().getTotal());

        ArgumentCaptor<SunatNotificationDto> captor = ArgumentCaptor.forClass(SunatNotificationDto.class);
        verify(notificacionRepository, times(1)).upsert(eq(20L), eq("10410481605"), captor.capture());
        assertEquals("980406926", captor.getValue().getId());
        verify(sunatCache, times(1)).put(eq("sunat:sync:notificaciones:20"), any(Instant.class));
    }

    @Test
    void shouldPaginateOverAccumulatedHistoryEvenIfBotReturnedFewer() {
        // El guard de sync está vigente: no llama al bot, solo pagina la BD (histórico ya acumulado).
        when(sunatCache.get("sunat:sync:notificaciones:16")).thenReturn(Optional.of(Instant.now()));
        when(notificacionRepository.listar(16L, 1, 25)).thenReturn(new SunatPageResult<>(List.of(), 68));

        SunatApiResponse<SunatNotificationListResponse> response = sunatService.getNotificaciones(16L, 1, 25, false);

        assertEquals(68, response.getData().getTotal());
        assertEquals(3, response.getData().getTotalPaginas()); // ceil(68/25)
        assertEquals(1, response.getData().getPagina());
        assertEquals(25, response.getData().getTamanioPagina());
        verifyNoInteractions(sunatBotClient);
    }

    @Test
    void shouldClampPageSizeToDefaultsAndMax() {
        when(sunatCache.get(anyString())).thenReturn(Optional.of(Instant.now()));
        when(notificacionRepository.listar(eq(17L), eq(0), eq(25))).thenReturn(emptyNotificationPage());
        sunatService.getNotificaciones(17L, -1, 0, false);
        verify(notificacionRepository).listar(17L, 0, 25);

        when(notificacionRepository.listar(eq(17L), eq(0), eq(100))).thenReturn(emptyNotificationPage());
        sunatService.getNotificaciones(17L, 0, 9999, false);
        verify(notificacionRepository).listar(17L, 0, 100);
    }

    @Test
    void shouldForceRefreshEvenWhenGuardIsValid() {
        when(credencialesRepository.obtenerCredenciales(22L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchNotifications(any())).thenReturn(botResponseWithOneNotification());
        when(notificacionRepository.listar(22L, 0, 25)).thenReturn(emptyNotificationPage());

        sunatService.getNotificaciones(22L, 0, 25, true);

        verify(sunatCache, never()).get(anyString());
        verify(sunatBotClient, times(1)).fetchNotifications(any());
    }

    // ---------- Notificaciones: errores del bot ----------

    @Test
    void shouldPropagateAuthenticationFailure() {
        when(sunatCache.get("sunat:sync:notificaciones:30")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(30L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchNotifications(any()))
                .thenThrow(new SunatAuthenticationException("Credenciales inválidas"));

        assertThrows(SunatAuthenticationException.class, () -> sunatService.getNotificaciones(30L, 0, 25, false));
        verifyNoInteractions(notificacionRepository);
    }

    @Test
    void shouldPropagateManualReviewAsFunctionalError() {
        when(sunatCache.get("sunat:sync:notificaciones:40")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(40L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchNotifications(any()))
                .thenThrow(new SunatManualReviewException("Revisión manual"));

        assertThrows(SunatManualReviewException.class, () -> sunatService.getNotificaciones(40L, 0, 25, false));
    }

    @Test
    void shouldPropagateServiceUnavailable() {
        when(sunatCache.get("sunat:sync:notificaciones:41")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(41L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchNotifications(any()))
                .thenThrow(new SunatUnavailableException("No disponible"));

        assertThrows(SunatUnavailableException.class, () -> sunatService.getNotificaciones(41L, 0, 25, false));
    }

    @Test
    void shouldFailFastWhenCredentialsAreNotConfigured() {
        when(sunatCache.get("sunat:sync:notificaciones:42")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(42L))
                .thenReturn(Optional.of(new SunatCredentialsInfo("10410481605", null, null)));

        assertThrows(SunatCredentialsNotConfiguredException.class, () -> sunatService.getNotificaciones(42L, 0, 25, false));
        verifyNoInteractions(sunatBotClient);
    }

    // ---------- Mensajes ----------

    @Test
    void shouldNotCallBotForMessagesWhenGuardIsValid() {
        when(sunatCache.get("sunat:sync:mensajes:50")).thenReturn(Optional.of(Instant.now()));
        when(mensajeRepository.listar(50L, 0, 25)).thenReturn(emptyMessagePage());

        SunatApiResponse<SunatMessageListResponse> response = sunatService.getMensajes(50L, 0, 25, false);

        assertTrue(response.isSuccess());
        assertEquals("CACHE", response.getSource());
        verifyNoInteractions(sunatBotClient);
    }

    @Test
    void shouldSyncAndPersistMessagesWhenGuardExpired() {
        when(sunatCache.get("sunat:sync:mensajes:60")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(60L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");

        SunatBotMessage message = new SunatBotMessage();
        message.setId("111");
        message.setAsunto("Asunto mensaje");
        message.setMensaje("Contenido");
        message.setRemitente("SUNAT");
        message.setFechaPublicacion("01/01/2026 00:00:00");
        message.setLeido(false);
        message.setTieneAdjunto(false);

        SunatBotPage page = new SunatBotPage();
        page.setPagina(1);
        page.setMensajes(List.of(message));

        SunatBotData data = new SunatBotData();
        data.setTotal(1);
        data.setTotalPaginas(1);
        data.setPaginas(List.of(page));

        SunatBotResponse botResponse = new SunatBotResponse();
        botResponse.setSuccess(true);
        botResponse.setData(data);

        when(sunatBotClient.fetchMessages(any())).thenReturn(botResponse);
        when(mensajeRepository.listar(60L, 0, 25))
                .thenReturn(new SunatPageResult<>(List.of(new SunatMessageDto("111", "Asunto mensaje", "Contenido", "SUNAT", "01/01/2026 00:00:00", false, false)), 1));

        SunatApiResponse<SunatMessageListResponse> response = sunatService.getMensajes(60L, 0, 25, false);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().getTotal());
        verify(mensajeRepository, times(1)).upsert(eq(60L), eq("10410481605"), any(SunatMessageDto.class));
        verify(sunatCache, times(1)).put(eq("sunat:sync:mensajes:60"), any(Instant.class));
    }

    @Test
    void shouldPropagateManualReviewForMessages() {
        when(sunatCache.get("sunat:sync:mensajes:61")).thenReturn(Optional.empty());
        when(credencialesRepository.obtenerCredenciales(61L)).thenReturn(Optional.of(configuredCredentials()));
        when(sunatCredentialCrypto.decrypt("encrypted")).thenReturn("oxitold23");
        when(sunatBotClient.fetchMessages(any())).thenThrow(new SunatManualReviewException("Revisión manual"));

        assertThrows(SunatManualReviewException.class, () -> sunatService.getMensajes(61L, 0, 25, false));
    }

    // ---------- Credenciales ----------

    @Test
    void shouldValidateAgainstBotBeforePersistingCredentials() {
        when(credencialesRepository.obtenerCredenciales(70L))
                .thenReturn(Optional.of(new SunatCredentialsInfo("10410481605", null, null)));
        when(sunatBotClient.login(any())).thenReturn(new SunatBotResponse());
        when(sunatCredentialCrypto.encrypt("oxitold23")).thenReturn("cipher-text");

        sunatService.guardarCredenciales(70L, "GEORICAT", "oxitold23");

        ArgumentCaptor<SunatBotRequest> captor = ArgumentCaptor.forClass(SunatBotRequest.class);
        verify(sunatBotClient).login(captor.capture());
        assertEquals("oxitold23", captor.getValue().getCredentials().getPassword());

        verify(credencialesRepository).actualizarCredenciales(70L, "GEORICAT", "cipher-text");
    }

    @Test
    void shouldNotPersistCredentialsWhenBotRejectsLogin() {
        when(credencialesRepository.obtenerCredenciales(71L))
                .thenReturn(Optional.of(new SunatCredentialsInfo("10410481605", null, null)));
        when(sunatBotClient.login(any())).thenThrow(new SunatAuthenticationException("Credenciales inválidas"));

        assertThrows(SunatAuthenticationException.class, () -> sunatService.guardarCredenciales(71L, "GEORICAT", "malo"));

        verify(credencialesRepository, never()).actualizarCredenciales(any(), any(), any());
    }

    // ---------- Seguridad ----------

    @Test
    void credentialsInfoToStringShouldNeverExposePassword() {
        SunatCredentialsInfo info = new SunatCredentialsInfo("10410481605", "GEORICAT", "super-secret-cipher");
        assertFalse(info.toString().contains("super-secret-cipher"));
    }
}
