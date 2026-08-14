package pe.ssimple.ssisfact_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist de JWT en memoria del proceso (sin BD/Redis) + sesión única por usuario.
 *
 * - Al hacer login, el token activo anterior de un usuario (si existía) se manda
 *   a la blacklist automáticamente: solo el último login por usuario queda válido.
 * - Al hacer logout, el token se agrega explícitamente a la blacklist.
 * - {@link #isBlacklisted(String)} se consulta en JwtAuthenticationFilter antes de
 *   validar firma/expiración del JWT.
 * - Un job programado purga periódicamente las entradas ya expiradas para que el
 *   mapa no crezca indefinidamente.
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // token -> fecha de expiración (para poder purgarlo cuando ya no hace falta retenerlo)
    private final Map<String, Date> blacklist = new ConcurrentHashMap<>();

    // usuarioId -> token activo actual (sesión única por usuario)
    private final Map<Long, String> activeTokenByUser = new ConcurrentHashMap<>();

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /** Registra el token recién emitido como la sesión activa del usuario, invalidando la anterior si existía. */
    public void registerActiveToken(Long usuarioId, String token, Date expiration) {
        String tokenAnterior = activeTokenByUser.put(usuarioId, token);
        if (tokenAnterior != null && !tokenAnterior.equals(token)) {
            blacklist.put(tokenAnterior, expiration);
            log.debug("Sesión anterior del usuario {} invalidada por nuevo login", usuarioId);
        }
    }

    /** Invalida explícitamente un token (logout). */
    public void blacklist(String token, Date expiration) {
        blacklist.put(token, expiration);
        activeTokenByUser.values().remove(token);
    }

    /** Purga cada 30 minutos las entradas cuya expiración ya pasó — evita que la blacklist crezca sin límite. */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void purgarExpirados() {
        Date ahora = new Date();
        int antes = blacklist.size();
        blacklist.values().removeIf(expiracion -> expiracion.before(ahora));
        int eliminados = antes - blacklist.size();
        if (eliminados > 0) {
            log.debug("Blacklist de tokens: {} entradas expiradas purgadas, {} restantes", eliminados, blacklist.size());
        }
    }
}
