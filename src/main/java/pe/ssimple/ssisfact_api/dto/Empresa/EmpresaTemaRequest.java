package pe.ssimple.ssisfact_api.dto.Empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmpresaTemaRequest {

    private static final String MENSAJE_HEX = "debe ser un color hexadecimal, ej. #1A2B3C";

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = MENSAJE_HEX)
    private String colorPrimario;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = MENSAJE_HEX)
    private String colorSecundario;

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = MENSAJE_HEX)
    private String colorAcento;
}
