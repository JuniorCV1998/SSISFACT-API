package pe.ssimple.ssisfact_api.dto.Venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VentaRequest {

    @NotNull(message = "es obligatoria")
    private Long sucursalId;

    // Opcional: si no se envía, se usa el cliente genérico "CLIENTE VARIOS" de la empresa
    private Long clienteId;

    @NotNull(message = "es obligatorio")
    private String tipoDocumento; // NOTA_PEDIDO (uso normal hoy), BOLETA o GUIA (reservados para SUNAT)

    @NotEmpty(message = "debe tener al menos un producto")
    @Valid
    private List<VentaItemRequest> items;

    // Opcional: descuento global (monto fijo, no porcentaje) aplicado sobre el
    // subtotal de toda la venta, además de los descuentos por ítem si los hay.
    @DecimalMin(value = "0.0", message = "no puede ser negativo")
    private BigDecimal descuento;

    // Opcional: una venta puede quedar PENDIENTE sin pagos registrados aún (venta al crédito)
    @Valid
    private List<PagoRequest> pagos;
}
