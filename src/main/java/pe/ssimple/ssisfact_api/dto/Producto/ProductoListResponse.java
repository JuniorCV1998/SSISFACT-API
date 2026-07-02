package pe.ssimple.ssisfact_api.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoListResponse {
    private List<ProductoItemResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
