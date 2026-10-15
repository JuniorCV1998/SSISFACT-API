package pe.ssimple.ssisfact_api.dto.PagoDigital;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

import java.math.BigDecimal;

// Body que envía la app de automatización del celular (MacroDroid/Tasker) al detectar
// la notificación de Yape o Plin. "remitente" y "textoOriginal" son opcionales porque
// la extracción por regex del texto de la notificación puede fallar en capturarlos.
@Data
public class RecibirNotificacionPagoDigitalRequest {

    // "YAPE" o "PLIN INTERBANK" — se valida contra el mismo catálogo que PagoDigitalServiceImpl
    // usa para el resto del flujo (ver METODOS_SOPORTADOS).
    @NotBlank(message = "es obligatorio")
    private String metodo;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0.01", message = "debe ser mayor a 0")
    private BigDecimal monto;

    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String remitente;

    @Size(max = 500, message = "no puede superar los 500 caracteres")
    @SafeText
    private String textoOriginal;
}
