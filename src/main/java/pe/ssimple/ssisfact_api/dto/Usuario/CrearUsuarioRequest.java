package pe.ssimple.ssisfact_api.dto.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CrearUsuarioRequest {

    @NotBlank(message = "es obligatorio")
    private String nombre;

    @NotBlank(message = "es obligatorio")
    @Email(message = "no tiene un formato válido")
    private String email;

    @NotBlank(message = "es obligatorio")
    private String documento;

    @NotBlank(message = "es obligatoria")
    @Pattern(regexp = "^\\d{6}$", message = "debe tener exactamente 6 dígitos numéricos")
    private String password;

    @NotBlank(message = "es obligatorio")
    private String rol; // CAJERO, SUPERVISOR, ALMACEN o AUDITOR

    // Opcional: si no se manda, el trabajador queda sin sucursal asignada
    // (bloqueado de abrir caja/vender/manejar stock hasta que se le asigne una).
    private Long sucursalId;
}
