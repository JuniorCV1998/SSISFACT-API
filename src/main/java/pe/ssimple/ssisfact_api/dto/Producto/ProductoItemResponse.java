package pe.ssimple.ssisfact_api.dto.Producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ProductoItemResponse {
    private Long id;
    private Long empresaId;
    private Long categoriaId;
    private String categoriaNombre;
    private String codigo;
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private Integer stockMinimo;
    private Integer afectoImpuesto;
    private Integer stockTotal;
    private Integer stockSucursal;
    private String imagenUrl;
    private Integer estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @JsonIgnore
    private Integer totalRegistros;
}
