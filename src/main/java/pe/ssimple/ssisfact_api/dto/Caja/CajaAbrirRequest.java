package pe.ssimple.ssisfact_api.dto.Caja;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CajaAbrirRequest {

    @NotNull(message = "es obligatoria")
    private Long sucursalId;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0.0", message = "no puede ser negativo")
    private BigDecimal montoInicial;
}
