package pe.ssimple.ssisfact_api.dto.Sucursal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

@Data
public class SucursalRequest {

    private Long idSucursal;

    // Se completa en el controller a partir del usuario autenticado
    private Long empresaId;

    @NotBlank(message = "es obligatorio")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String nombre;

    @NotBlank(message = "es obligatoria")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String direccion;

    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @SafeText
    private String telefono;

    private Integer estado;
}
