package pe.ssimple.ssisfact_api.dto.Venta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

import java.math.BigDecimal;

@Data
public class PagoRequest {

    @NotBlank(message = "es obligatorio")
    private String metodo;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0.01", message = "debe ser mayor a 0")
    private BigDecimal monto;

    // Opcional: cuánto entregó físicamente el cliente (normalmente en EFECTIVO), si
    // es mayor a "monto" implica vuelto. Si no se envía, se asume que no hay vuelto.
    @DecimalMin(value = "0.01", message = "debe ser mayor a 0")
    private BigDecimal montoRecibido;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @SafeText
    private String referencia;
}
