package pe.ssimple.ssisfact_api.dto.Stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RetiroStockRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotBlank(message = "es obligatorio")
    @Pattern(
        regexp = "VENTA|MERMA|AJUSTE",
        message = "debe ser VENTA, MERMA o AJUSTE"
    )
    private String tipo;

    @Size(max = 100, message = "El motivo no puede superar los 100 caracteres")
    private String motivo;

    private Long referenciaId;
}
