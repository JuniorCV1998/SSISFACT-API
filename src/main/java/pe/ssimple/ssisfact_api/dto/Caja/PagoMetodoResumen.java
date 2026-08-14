package pe.ssimple.ssisfact_api.dto.Caja;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoMetodoResumen {
    private String metodo;
    private BigDecimal total;
    private Integer cantidad;
}
