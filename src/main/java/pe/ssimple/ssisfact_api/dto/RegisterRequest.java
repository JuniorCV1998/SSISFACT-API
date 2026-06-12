package pe.ssimple.ssisfact_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El documento debe tener exactamente 6 dígitos numéricos")
    private String documento;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^\\d{6}$", message = "La contraseña debe tener exactamente 6 dígitos numéricos")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 1, max = 100, message = "El apellido debe tener entre 1 y 100 caracteres")
    private String apellido;
}