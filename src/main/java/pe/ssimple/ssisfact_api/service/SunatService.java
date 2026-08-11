package pe.ssimple.ssisfact_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Sunat.*;
import pe.ssimple.ssisfact_api.exception.SunatCredentialsNotConfiguredException;
import pe.ssimple.ssisfact_api.repository.SunatCredencialesRepository;
import pe.ssimple.ssisfact_api.repository.SunatMensajeRepository;
import pe.ssimple.ssisfact_api.repository.SunatNotificacionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * El bot solo entrega "una tanda" de resultados por invocación (no acepta pedir
 * una página puntual), así que el histórico completo vive en BD (upsert, nunca
 * se borra) y el listado que ve el frontend siempre pagina sobre esa BD.
 * La cache ya no guarda el listado: solo actúa de "guard" para no invocar el
 * bot más de una vez cada {@link #} sunat.cache.ttl-seconds por empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SunatService {

    private static final int PAGE_SIZE_DEFAULT = 25;
    private static final int PAGE_SIZE_MAX = 100;

    private final SunatCredencialesRepository credencialesRepository;
    private final SunatNotificacionRepository notificacionRepository;
    private final SunatMensajeRepository mensajeRepository;
    private final SunatCache sunatCache;
    private final SunatBotClient sunatBotClient;
    private final SunatCredentialCrypto sunatCredentialCrypto;
    private final Map<Long, Object> syncLocks = new ConcurrentHashMap<>();

    public SunatApiResponse<SunatNotificationListResponse> getNotificaciones(
            Long empresaId, int pagina, int tamanio, boolean forceRefresh) {

        SyncResult sync = sincronizarSiHaceFalta(
                empresaId, forceRefresh, "sunat:sync:notificaciones:" + empresaId,
                credentials -> sunatBotClient.fetchNotifications(buildRequest(credentials)),
                (id, ruc, botResponse) -> persistirNotificaciones(id, ruc, botResponse));

        int size = clampSize(tamanio);
        int page = Math.max(pagina, 0);
        SunatPageResult<SunatNotificationDto> pageResult = notificacionRepository.listar(empresaId, page, size);

        SunatNotificationListResponse response = new SunatNotificationListResponse(
                page, size, pageResult.getTotal(), totalPaginas(pageResult.getTotal(), size),
                pageResult.getItems(), sync.ultimaSincronizacion());
        return SunatApiResponse.success(response, sync.source());
    }

    public SunatApiResponse<SunatMessageListResponse> getMensajes(
            Long empresaId, int pagina, int tamanio, boolean forceRefresh) {

        SyncResult sync = sincronizarSiHaceFalta(
                empresaId, forceRefresh, "sunat:sync:mensajes:" + empresaId,
                credentials -> sunatBotClient.fetchMessages(buildRequest(credentials)),
                (id, ruc, botResponse) -> persistirMensajes(id, ruc, botResponse));

        int size = clampSize(tamanio);
        int page = Math.max(pagina, 0);
        SunatPageResult<SunatMessageDto> pageResult = mensajeRepository.listar(empresaId, page, size);

        SunatMessageListResponse response = new SunatMessageListResponse(
                page, size, pageResult.getTotal(), totalPaginas(pageResult.getTotal(), size),
                pageResult.getItems(), sync.ultimaSincronizacion());
        return SunatApiResponse.success(response, sync.source());
    }

    public boolean tieneCredencialesConfiguradas(Long empresaId) {
        return credencialesRepository.obtenerCredenciales(empresaId)
                .map(SunatCredentialsInfo::isConfigured)
                .orElse(false);
    }

    /** Valida (contra el bot) y persiste las credenciales SUNAT de la empresa. */
    public SunatApiResponse<Void> guardarCredenciales(Long empresaId, String username, String password) {
        SunatCredentialsInfo current = credencialesRepository.obtenerCredenciales(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        SunatBotRequest.Credentials credentials = SunatBotRequest.Credentials.builder()
                .username(username)
                .password(password)
                .extra(Map.of("ruc", current.getRuc()))
                .build();

        sunatBotClient.login(SunatBotRequest.builder().credentials(credentials).build());

        credencialesRepository.actualizarCredenciales(empresaId, username, sunatCredentialCrypto.encrypt(password));
        return SunatApiResponse.success(null, "SUNAT");
    }

    /**
     * Si la cache-guard de la empresa está vencida (o forceRefresh=true), llama al
     * bot bajo lock por empresa y persiste lo que devuelva. Siempre retorna el
     * instante del último sync exitoso conocido (aunque esta llamada haya sido
     * un hit de cache).
     */
    private SyncResult sincronizarSiHaceFalta(
            Long empresaId, boolean forceRefresh, String syncKey,
            java.util.function.Function<SunatCredentialsInfo, SunatBotResponse> invocarBot,
            TriConsumer<Long, String, SunatBotResponse> persistir) {

        if (!forceRefresh) {
            Optional<Instant> lastSync = sunatCache.get(syncKey);
            if (lastSync.isPresent()) {
                return new SyncResult("CACHE", lastSync.get());
            }
        }

        synchronized (syncLocks.computeIfAbsent(empresaId, id -> new Object())) {
            if (!forceRefresh) {
                Optional<Instant> lastSyncAgain = sunatCache.get(syncKey);
                if (lastSyncAgain.isPresent()) {
                    return new SyncResult("CACHE", lastSyncAgain.get());
                }
            }

            SunatCredentialsInfo credentials = obtenerCredencialesConfiguradas(empresaId);
            SunatBotResponse botResponse = invocarBot.apply(credentials);
            persistir.accept(empresaId, credentials.getRuc(), botResponse);

            Instant now = Instant.now();
            sunatCache.put(syncKey, now);
            return new SyncResult("SUNAT", now);
        }
    }

    private record SyncResult(String source, Instant ultimaSincronizacion) {
    }

    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private static int clampSize(int size) {
        if (size <= 0) {
            return PAGE_SIZE_DEFAULT;
        }
        return Math.min(size, PAGE_SIZE_MAX);
    }

    private static int totalPaginas(long total, int size) {
        return (int) Math.ceil(total / (double) size);
    }

    private SunatCredentialsInfo obtenerCredencialesConfiguradas(Long empresaId) {
        SunatCredentialsInfo credentials = credencialesRepository.obtenerCredenciales(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        if (!credentials.isConfigured()) {
            throw new SunatCredentialsNotConfiguredException(
                    "Debes configurar tus credenciales SUNAT antes de continuar.");
        }
        return credentials;
    }

    private SunatBotRequest buildRequest(SunatCredentialsInfo credentials) {
        SunatBotRequest.Credentials.CredentialsBuilder credentialsBuilder = SunatBotRequest.Credentials.builder()
                .username(credentials.getUsernameSunat())
                .password(sunatCredentialCrypto.decrypt(credentials.getPasswordSunatEncriptado()));

        if (credentials.getRuc() != null && !credentials.getRuc().isBlank()) {
            credentialsBuilder.extra(Map.of("ruc", credentials.getRuc()));
        }
        return SunatBotRequest.builder().credentials(credentialsBuilder.build()).build();
    }

    private void persistirNotificaciones(Long empresaId, String ruc, SunatBotResponse botResponse) {
        for (SunatNotificationDto dto : extraerNotificaciones(botResponse)) {
            notificacionRepository.upsert(empresaId, ruc, dto);
        }
    }

    private void persistirMensajes(Long empresaId, String ruc, SunatBotResponse botResponse) {
        for (SunatMessageDto dto : extraerMensajes(botResponse)) {
            mensajeRepository.upsert(empresaId, ruc, dto);
        }
    }

    private List<SunatNotificationDto> extraerNotificaciones(SunatBotResponse botResponse) {
        List<SunatNotificationDto> items = new java.util.ArrayList<>();
        if (botResponse != null && botResponse.getData() != null && botResponse.getData().getPaginas() != null) {
            for (SunatBotPage page : botResponse.getData().getPaginas()) {
                if (page.getNotificaciones() == null) {
                    continue;
                }
                for (SunatBotNotification item : page.getNotificaciones()) {
                    items.add(new SunatNotificationDto(
                            item.getId(),
                            item.getAsunto(),
                            item.getFechaPublicacion(),
                            item.getCategoria(),
                            null,
                            item.isLeido(),
                            item.isDestacado(),
                            item.isUrgente(),
                            item.isTieneAdjunto()));
                }
            }
        }
        return items;
    }

    private List<SunatMessageDto> extraerMensajes(SunatBotResponse botResponse) {
        List<SunatMessageDto> items = new java.util.ArrayList<>();
        if (botResponse != null && botResponse.getData() != null && botResponse.getData().getPaginas() != null) {
            for (SunatBotPage page : botResponse.getData().getPaginas()) {
                if (page.getMensajes() == null) {
                    continue;
                }
                for (SunatBotMessage item : page.getMensajes()) {
                    items.add(new SunatMessageDto(
                            item.getId(),
                            item.getAsunto(),
                            item.getMensaje(),
                            item.getRemitente(),
                            item.getFechaPublicacion(),
                            item.isLeido(),
                            item.isTieneAdjunto()));
                }
            }
        }
        return items;
    }
}
