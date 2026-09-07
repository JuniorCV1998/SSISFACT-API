package pe.ssimple.ssisfact_api.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaProductoResponse {
    private String nombre;

    // Código de barras si el producto tiene uno; si no, el código interno.
    private String codigo;

    private BigDecimal precio;
}
