package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioLockInfo {
    private Long inventarioId;
    private Integer stock;
}
