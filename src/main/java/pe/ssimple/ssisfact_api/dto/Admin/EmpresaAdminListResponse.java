package pe.ssimple.ssisfact_api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaAdminListResponse {
    private List<EmpresaAdminItemResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
