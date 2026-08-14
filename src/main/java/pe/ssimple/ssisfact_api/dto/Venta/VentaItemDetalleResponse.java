package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaItemDetalleResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal descuento;
}
