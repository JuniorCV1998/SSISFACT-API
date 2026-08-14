package pe.ssimple.ssisfact_api.dto.Caja;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CajaItemResponse {
    private Long id;
    private Long sucursalId;
    private String sucursalNombre;
    private Long usuarioId;
    private String usuarioNombre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String estado;

    @JsonIgnore
    private Integer totalRegistros;
}
