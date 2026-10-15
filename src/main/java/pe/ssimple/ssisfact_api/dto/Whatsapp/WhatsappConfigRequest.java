package pe.ssimple.ssisfact_api.dto.Whatsapp;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

// Actualización parcial: los campos que no vienen (null) conservan su valor guardado.
// Vacío en mensajeComprobante restaura el texto por defecto.
@Data
public class WhatsappConfigRequest {

    private boolean activo;

    @Size(max = 500, message = "no puede superar 500 caracteres")
    @SafeText
    private String mensaje;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "debe tener formato HH:mm")
    private String horaEnvio;

    @Size(max = 255, message = "no puede superar 255 caracteres")
    @SafeText
    private String mensajeComprobante;
}
