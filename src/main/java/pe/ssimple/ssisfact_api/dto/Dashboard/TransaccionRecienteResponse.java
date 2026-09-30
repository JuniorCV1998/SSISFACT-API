package pe.ssimple.ssisfact_api.dto.Dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransaccionRecienteResponse {
    private Long ventaId;
    // Puede venir null si la venta aún no tiene comprobante generado (PENDIENTE).
    private String comprobanteTipo;
    private String comprobanteCodigo;
    private String clienteNombre;
    private BigDecimal total;
    // PENDIENTE, PAGADA, ANULADA
    private String estado;
    private LocalDateTime fecha;
}
