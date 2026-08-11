package pe.ssimple.ssisfact_api.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra/descifra las credenciales SUNAT (username/Clave SOL) que se guardan en
 * la tabla empresas, usando AES-256-GCM. La clave nunca debe loguearse ni
 * exponerse; solo vive en memoria a partir de la property sunat.crypto.secret-key.
 */
@Service
public class SunatCredentialCrypto {

    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${sunat.crypto.secret-key}")
    private String secretKeyBase64;

    private SecretKeySpec secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException(
                    "sunat.crypto.secret-key debe decodificar a 16, 24 o 32 bytes (AES-128/192/256)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] output = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(cipherText, 0, output, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible encriptar la credencial SUNAT", e);
        }
    }

    public String decrypt(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        try {
            byte[] input = Base64.getDecoder().decode(storedValue);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(input, 0, iv, 0, iv.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plainText = cipher.doFinal(input, iv.length, input.length - iv.length);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible desencriptar la credencial SUNAT", e);
        }
    }
}
