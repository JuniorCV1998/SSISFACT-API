package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientesAtendidosKpi {
    // Clientes distintos que compraron hoy (agrupa por cliente_id; si varias
    // ventas anónimas usan el cliente genérico "Cliente Varios", cuentan como 1).
    private int cantidad;
    // Delta absoluto vs ayer (puede ser negativo).
    private int variacionAbsoluta;
}
