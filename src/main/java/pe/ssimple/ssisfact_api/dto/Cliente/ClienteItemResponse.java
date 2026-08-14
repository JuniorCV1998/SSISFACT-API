package pe.ssimple.ssisfact_api.dto.Cliente;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteItemResponse {
    private Long id;
    private Long empresaId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private Integer estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @JsonIgnore
    private Integer totalRegistros;
}
