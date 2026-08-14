package pe.ssimple.ssisfact_api.dto.Caja;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CajaListResponse {
    private List<CajaItemResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
