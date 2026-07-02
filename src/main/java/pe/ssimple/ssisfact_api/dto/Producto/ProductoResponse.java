package pe.ssimple.ssisfact_api.dto.Producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse implements SpResponse {

    private String estado;

    @JsonIgnore // ya viaja como "message" en el ApiResponse, evita duplicarlo en data
    private String mensaje;

    private Long id;
    private StockResponse stock;

    public ProductoResponse(String estado, String mensaje, Long id) {
        this(estado, mensaje, id, null);
    }
}
