package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// Payload del evento SSE que se empuja al frontend cuando un pago se registra
// sobre una venta PENDIENTE (hoy: confirmación automática de Yape/Plin vía webhook,
// o asociación manual desde la bandeja) — para que la pantalla de detalle/listado
// se actualice sola, sin que el usuario tenga que recargar.
@Data
@AllArgsConstructor
public class VentaPagoEventPayload {
    private Long ventaId;
    private String estado;      // PAGADA o PENDIENTE (si el pago fue parcial)
    private String metodo;
    private BigDecimal monto;
    private BigDecimal saldo;   // lo que falta por pagar después de este pago
}
