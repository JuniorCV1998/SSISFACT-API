package pe.ssimple.ssisfact_api.dto.Empresa;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpresaActualizarRequest {

    @NotBlank(message = "es obligatorio")
    private String nombre;

    @NotBlank(message = "es obligatorio")
    private String ruc;

    private String direccion;
    private String telefono;

    @Email(message = "debe ser un email válido")
    private String email;

    private boolean igvHabilitado;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0", message = "no puede ser negativo")
    @DecimalMax(value = "100", message = "no puede superar 100")
    private BigDecimal igvPorcentaje;

    private String ticketMensajePie;
}
