package pe.ssimple.ssisfact_api.dto.Assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

@Data
public class AssistantConfigRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 30, message = "no puede superar 30 caracteres")
    @SafeText
    private String nombreAsistente;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "CERCANO|FORMAL|DIRECTO", message = "debe ser CERCANO, FORMAL o DIRECTO")
    private String tono;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "FEMENINA|MASCULINA", message = "debe ser FEMENINA o MASCULINA")
    private String vozGenero;
}
