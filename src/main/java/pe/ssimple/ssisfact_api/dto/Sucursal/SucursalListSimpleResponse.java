package pe.ssimple.ssisfact_api.dto.Sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalListSimpleResponse {
    private Integer maxSucursales;
    private List<SucursalSimpleResponse> items;
}
