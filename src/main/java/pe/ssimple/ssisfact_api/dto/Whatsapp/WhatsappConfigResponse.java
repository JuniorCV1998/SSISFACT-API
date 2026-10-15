package pe.ssimple.ssisfact_api.dto.Whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappConfigResponse {
    private String mensaje;
    private String horaEnvio;
    private String mensajeComprobante;
    private boolean activo;
    private boolean whatsappVinculado;
}
