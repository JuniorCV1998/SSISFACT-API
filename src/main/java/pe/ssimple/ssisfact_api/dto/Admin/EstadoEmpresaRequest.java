package pe.ssimple.ssisfact_api.dto.Admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoEmpresaRequest {

    @NotNull(message = "es obligatorio")
    private Integer estado; // 0=eliminada, 1=activa, 2=pendiente/suspendida
}
