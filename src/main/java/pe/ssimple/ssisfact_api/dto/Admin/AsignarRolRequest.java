package pe.ssimple.ssisfact_api.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AsignarRolRequest {

    @NotBlank(message = "es obligatorio")
    private String rol;
}
