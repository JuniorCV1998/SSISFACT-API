package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoResponse {
    // EFECTIVO, YAPE, PLIN, TRANSFERENCIA, TARJETA
    private String metodo;
    private BigDecimal total;
    private int porcentaje;
}
