package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaListResponse {
    private List<VentaListItemResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
