package pe.ssimple.ssisfact_api.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.ValidUsername;

@Data
public class LoginRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 255, message = "El username debe tener entre 3 y 255 caracteres")
    @ValidUsername
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^\\d{6}$", message = "La contraseña debe tener exactamente 6 dígitos numéricos")
    private String password;
}