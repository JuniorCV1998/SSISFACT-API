package pe.ssimple.ssisfact_api.dto.Empresa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpresaActualResponse {
    private Long id;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
    private String logoUrl;
    private String logoTicketUrl;
    private boolean igvHabilitado;
    private BigDecimal igvPorcentaje;
    private String ticketMensajePie;
    private String asistenteNombre;
    private String asistenteTono;
    private String vozGenero;
    private String colorPrimario;
    private String colorSecundario;
    private String colorAcento;
}
