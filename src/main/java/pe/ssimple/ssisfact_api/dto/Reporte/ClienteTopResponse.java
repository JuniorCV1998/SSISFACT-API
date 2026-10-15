package pe.ssimple.ssisfact_api.dto.Reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteTopResponse {
    private Long clienteId;
    private String nombre;
    private int cantidadCompras;
    private BigDecimal totalComprado;
    // 0-100, relativo al cliente con más compras del listado (el #1 siempre vale 100).
    private int porcentajeRelativo;
}
