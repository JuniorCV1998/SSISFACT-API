package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVentaInfo {
    private Long id;
    private String nombre;
    private BigDecimal precio;
}
