package pe.ssimple.ssisfact_api.dto.Venta;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnviarVentaWhatsappRequest {

    // Opcional: si no viene, se usa el teléfono registrado del cliente de la venta.
    @Pattern(regexp = "^[0-9+ ]{0,20}$", message = "debe contener solo números")
    private String numero;
}
