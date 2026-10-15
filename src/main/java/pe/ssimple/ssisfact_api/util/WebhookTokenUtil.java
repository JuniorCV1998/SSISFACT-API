package pe.ssimple.ssisfact_api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// Generación y verificación de tokens de webhook por empresa (ej. Yape). El token en
// claro solo se le muestra al ADMIN una vez, al generarlo; en BD se guarda únicamente
// su hash SHA-256 (hex), para que una fuga de la base no permita inyectar pagos falsos.
public final class WebhookTokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private WebhookTokenUtil() {
    }

    public static String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
