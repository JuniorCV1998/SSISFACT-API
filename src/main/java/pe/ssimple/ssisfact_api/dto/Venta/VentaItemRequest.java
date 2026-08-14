package pe.ssimple.ssisfact_api.dto.Venta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VentaItemRequest {

    @NotNull(message = "es obligatorio")
    private Long productoId;

    @NotNull(message = "es obligatoria")
    @DecimalMin(value = "0.01", message = "debe ser mayor a 0")
    private BigDecimal cantidad;

    // Opcional: si no se envía, se toma el precio actual del producto
    @DecimalMin(value = "0.0", message = "no puede ser negativo")
    private BigDecimal precioUnitario;

    // Opcional: descuento (monto fijo, no porcentaje) aplicado a esta línea.
    // No puede superar precioUnitario * cantidad — se valida en el service.
    @DecimalMin(value = "0.0", message = "no puede ser negativo")
    private BigDecimal descuento;
}
