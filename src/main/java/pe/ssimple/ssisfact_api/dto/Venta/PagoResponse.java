package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoResponse {
    private Long id;
    private String metodo;
    private BigDecimal monto;
    private String referencia;
    private LocalDateTime fecha;
}
