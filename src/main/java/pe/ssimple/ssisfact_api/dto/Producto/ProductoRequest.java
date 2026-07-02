package pe.ssimple.ssisfact_api.dto.Producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

import java.math.BigDecimal;

@Data
public class ProductoRequest {

    private Long idProducto;

    // Se completa en el controller a partir del usuario autenticado, no se recibe del cliente
    private Long empresaId;

    @NotBlank(message = "es obligatorio")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String categoriaNombre;

    /* @NotBlank(message = "es obligatorio") */
    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @SafeText
    private String codigo;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @SafeText
    private String codigoBarras;

    @NotBlank(message = "es obligatorio")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String nombre;

    @SafeText
    private String descripcion;

    @NotNull(message = "es obligatorio")
    @DecimalMin(value = "0.00", message = "no puede ser negativo")
    private BigDecimal precio;

    @DecimalMin(value = "0.00", message = "no puede ser negativo")
    private BigDecimal costo;

    @Min(value = 0, message = "no puede ser negativo")
    private Integer stockMinimo;

    private Integer afectoImpuesto;

    @Size(max = 255, message = "no puede superar los 255 caracteres")
    private String imagenUrl;

    // Stock inicial (opcional): si se envía sucursalId, cantidadInicial y motivoIngreso son obligatorios
    private Long sucursalId;

    @Min(value = 1, message = "debe ser mayor a 0")
    private Integer cantidadInicial;

    @Size(max = 255, message = "no puede superar los 255 caracteres")
    @SafeText
    private String motivoIngreso;

    private Long compraId;
}
