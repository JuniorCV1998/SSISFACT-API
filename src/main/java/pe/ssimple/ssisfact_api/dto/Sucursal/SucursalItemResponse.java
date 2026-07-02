package pe.ssimple.ssisfact_api.dto.Sucursal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SucursalItemResponse {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String direccion;
    private String telefono;
    private Integer estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @JsonIgnore
    private Integer totalRegistros;
}
