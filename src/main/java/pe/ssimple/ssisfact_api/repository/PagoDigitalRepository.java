package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalConfigResponse;

import java.math.BigDecimal;
import java.util.List;

// Webhook de pagos digitales (Yape, Plin) por notificación del celular. Una sola
// configuración/token/bandeja por empresa sirve para cualquier método — ver
// PagoDigitalServiceImpl.
public interface PagoDigitalRepository {

    // --- Configuración por empresa (configuracion_empresa) ---
    void habilitarToken(Long empresaId, String tokenHash);
    void deshabilitar(Long empresaId);
    PagoDigitalConfigResponse obtenerConfig(Long empresaId);
    // Resuelve la empresa dueña de un token de webhook ya hasheado, solo si está
    // habilitado. Retorna null si no hay match o el webhook está deshabilitado.
    Long buscarEmpresaPorTokenHash(String tokenHash);

    // --- Bandeja de notificaciones (pago_digital_notificaciones) ---
    Long insertarNotificacion(Long empresaId, String metodo, BigDecimal monto, String remitente, String textoOriginal);
    void confirmarNotificacion(Long notificacionId, Long ventaId, String estado);
    void marcarDescartada(Long notificacionId);
    NotificacionPagoDigitalResponse obtenerNotificacion(Long notificacionId, Long empresaId);
    List<NotificacionPagoDigitalResponse> listarBandeja(Long empresaId, String estado, int page, int size);
}
