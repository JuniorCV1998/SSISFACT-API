package pe.ssimple.ssisfact_api.dto.Assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantCorrectionRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 500, message = "no puede superar los 500 caracteres")
    private String contexto;

    @NotBlank(message = "es obligatoria")
    @Size(max = 500, message = "no puede superar los 500 caracteres")
    private String correccion;
}
