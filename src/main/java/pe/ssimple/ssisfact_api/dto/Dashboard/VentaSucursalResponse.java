package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaSucursalResponse {
    private Long sucursalId;
    private String sucursalNombre;
    private BigDecimal total;
    private int cantidadVentas;
}
