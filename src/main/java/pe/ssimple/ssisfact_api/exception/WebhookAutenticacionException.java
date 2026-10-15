package pe.ssimple.ssisfact_api.exception;

// Token de webhook ausente, inválido o con el webhook deshabilitado para esa empresa.
public class WebhookAutenticacionException extends RuntimeException {
    public WebhookAutenticacionException(String message) {
        super(message);
    }
}
