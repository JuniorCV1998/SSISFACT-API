package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatMessageDto {
    private String id;
    private String asunto;
    private String mensaje;
    private String remitente;
    @JsonProperty("fechaPublicacion")
    private String fechaPublicacion;
    private boolean leido;
    @JsonProperty("tieneAdjunto")
    private boolean tieneAdjunto;
}
