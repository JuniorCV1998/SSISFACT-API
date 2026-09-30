package pe.ssimple.ssisfact_api.dto.Dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaHoyKpi {
    private BigDecimal total;
    private int cantidadComprobantes;
    // % vs ayer. null si ayer no hubo ventas (no hay base de comparación).
    private BigDecimal variacionPorcentual;
}
