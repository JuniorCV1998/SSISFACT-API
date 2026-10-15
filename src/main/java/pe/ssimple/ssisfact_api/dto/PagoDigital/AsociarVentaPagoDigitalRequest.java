package pe.ssimple.ssisfact_api.dto.PagoDigital;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsociarVentaPagoDigitalRequest {

    @NotNull(message = "es obligatorio")
    private Long ventaId;
}
