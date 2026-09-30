package pe.ssimple.ssisfact_api.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoMasVendidoResponse {
    private Long productoId;
    private String nombre;
    private BigDecimal unidadesVendidas;
    private BigDecimal totalVendido;
    // 0-100, relativo al producto más vendido del listado (el #1 siempre vale 100).
    // Pensado para pintar directamente el ancho de la barra de progreso.
    private int porcentajeRelativo;
}
