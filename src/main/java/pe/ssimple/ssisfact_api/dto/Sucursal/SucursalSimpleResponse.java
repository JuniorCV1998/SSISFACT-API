package pe.ssimple.ssisfact_api.dto.Sucursal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalSimpleResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private Integer estado;

    @JsonIgnore
    private Integer maxSucursal; // se extrae en el service para el nivel de lista
}
