package pe.ssimple.ssisfact_api.dto.Cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

@Data
public class ClienteRequest {

    private Long idCliente;

    // Se completa en el controller a partir del usuario autenticado, no se recibe del cliente
    private Long empresaId;

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no puede superar los 20 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @SafeText
    private String numeroDocumento;

    @NotBlank(message = "es obligatorio")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String nombre;

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @SafeText
    private String telefono;

    @Email(message = "debe ser un correo válido")
    @Size(max = 100, message = "no puede superar los 100 caracteres")
    private String email;

    @Size(max = 255, message = "no puede superar los 255 caracteres")
    @SafeText
    private String direccion;
}
