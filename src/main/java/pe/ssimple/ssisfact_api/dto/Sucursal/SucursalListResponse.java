package pe.ssimple.ssisfact_api.dto.Sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalListResponse {
    private List<SucursalItemResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
