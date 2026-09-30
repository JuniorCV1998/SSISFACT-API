package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductosEnStockKpi {
    private int totalUnidades;
    private int stockBajo;
}
