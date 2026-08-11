package pe.ssimple.ssisfact_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SunatCredentialCryptoTest {

    private SunatCredentialCrypto crypto;

    @BeforeEach
    void setUp() throws Exception {
        crypto = new SunatCredentialCrypto();
        Field keyField = SunatCredentialCrypto.class.getDeclaredField("secretKeyBase64");
        keyField.setAccessible(true);
        keyField.set(crypto, Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes()));
        java.lang.reflect.Method init = SunatCredentialCrypto.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(crypto);
    }

    @Test
    void shouldRoundTripEncryptDecrypt() {
        String encrypted = crypto.encrypt("oxitold23");
        assertNotEquals("oxitold23", encrypted);
        assertEquals("oxitold23", crypto.decrypt(encrypted));
    }

    @Test
    void shouldProduceDifferentCiphertextEachTime() {
        String first = crypto.encrypt("oxitold23");
        String second = crypto.encrypt("oxitold23");
        assertNotEquals(first, second, "el IV aleatorio debe producir ciphertexts distintos");
    }

    @Test
    void shouldReturnNullForBlankInput() {
        assertNull(crypto.encrypt(null));
        assertNull(crypto.encrypt(""));
        assertNull(crypto.decrypt(null));
    }
}
