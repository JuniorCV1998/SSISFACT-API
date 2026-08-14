package pe.ssimple.ssisfact_api.dto.Caja;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoVendidoResumen {
    private Long productoId;
    private String productoNombre;
    private BigDecimal cantidadVendida;
    private BigDecimal totalVendido;
}
