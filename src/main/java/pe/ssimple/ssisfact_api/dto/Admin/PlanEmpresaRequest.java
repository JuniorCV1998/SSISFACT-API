package pe.ssimple.ssisfact_api.dto.Admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

import java.time.LocalDate;

@Data
public class PlanEmpresaRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @SafeText
    private String plan;

    @NotNull(message = "es obligatorio")
    @Min(value = 0, message = "no puede ser negativo")
    private Integer maxSucursal;

    @NotNull(message = "es obligatorio")
    @Min(value = 0, message = "no puede ser negativo")
    private Integer maxUsuarios;

    // Opcional: null = sin vencimiento
    private LocalDate fechaVencimiento;
}
