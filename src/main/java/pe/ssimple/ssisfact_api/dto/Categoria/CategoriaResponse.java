package pe.ssimple.ssisfact_api.dto.Categoria;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoriaResponse {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String descripcion;
    private Integer estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
