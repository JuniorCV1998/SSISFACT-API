package pe.ssimple.ssisfact_api.dto.Caja;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CajaCerrarResponse implements SpResponse {
    private String estado;

    @JsonIgnore // ya viaja como "message" en el ApiResponse, evita duplicarlo en data
    private String mensaje;

    private Long id;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;

    // Arqueo físico: solo EFECTIVO (lo que debe haber en el cajón).
    private BigDecimal montoEsperado;
    private BigDecimal diferencia;

    // Todo el dinero ingresado en la caja, sin importar el método (EFECTIVO + YAPE + PLIN + ...).
    private BigDecimal totalIngresado;
    private List<PagoMetodoResumen> detallePagos;
}
