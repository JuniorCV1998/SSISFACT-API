package pe.ssimple.ssisfact_api.dto.PagoDigital;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionPagoDigitalResponse {
    private Long id;
    private String metodo; // YAPE, PLIN
    private BigDecimal monto;
    private String remitente;
    private String textoOriginal;
    private LocalDateTime fechaRecibido;
    private String estado; // CONFIRMADA_AUTO, CONFIRMADA_MANUAL, SIN_MATCH, DESCARTADA
    private Long ventaId;
    private LocalDateTime fechaAsociacion;
    // Solo viene seteado cuando el repositorio devuelve una lista paginada (listarBandeja).
    private Integer totalRegistros;
}
