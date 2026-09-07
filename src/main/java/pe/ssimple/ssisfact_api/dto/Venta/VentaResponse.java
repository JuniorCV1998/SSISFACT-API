package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {
    private Long id;
    private String estado;
    private String comprobanteTipo;
    private String comprobanteSerie;
    private Integer comprobanteNumero;
    // Listo para imprimir: "NP01-000001" (serie + correlativo con 6 dígitos).
    private String comprobanteCodigo;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuestos;
    private BigDecimal total;
}
