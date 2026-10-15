package pe.ssimple.ssisfact_api.dto.Reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendientesCobroResponse {
    private BigDecimal monto;
    private int cantidad;
}
