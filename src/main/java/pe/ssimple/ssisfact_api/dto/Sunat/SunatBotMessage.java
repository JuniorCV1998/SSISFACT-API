package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SunatBotMessage {
    private String id;
    private String asunto;
    private String mensaje;
    private String remitente;
    private String fechaPublicacion;
    private boolean leido;
    private boolean tieneAdjunto;
}
