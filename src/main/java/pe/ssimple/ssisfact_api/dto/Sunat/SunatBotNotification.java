package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SunatBotNotification {
    private String id;
    private String asunto;
    private String fechaPublicacion;
    private String categoria;
    private boolean leido;
    private boolean destacado;
    private boolean urgente;
    private boolean tieneAdjunto;
}
