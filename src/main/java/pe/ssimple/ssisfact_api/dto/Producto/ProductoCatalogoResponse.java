package pe.ssimple.ssisfact_api.dto.Producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoCatalogoResponse {
    private Long id;
    private String categoriaNombre;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer afectoImpuesto;
    private Integer stockTotal;
    private String imagenUrl;

    @JsonIgnore
    private Integer totalRegistros;
}
