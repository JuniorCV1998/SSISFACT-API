package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalListResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalConfigResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalTokenResponse;

import java.math.BigDecimal;

public interface PagoDigitalService {

    PagoDigitalConfigResponse obtenerConfig(Long empresaId);
    PagoDigitalTokenResponse regenerarToken(Long empresaId);
    void deshabilitar(Long empresaId);

    // Llamado desde el webhook público (sin JWT) — "tokenPlano" es el valor en claro
    // que mandó el celular, se hashea internamente antes de buscar la empresa.
    // "metodo" es "YAPE" o "PLIN".
    void recibirNotificacion(String tokenPlano, String metodo, BigDecimal monto, String remitente, String textoOriginal);

    NotificacionPagoDigitalListResponse listarBandeja(Long empresaId, String estado, int page, int size);
    void asociarManual(Long notificacionId, Long empresaId, Long ventaId);
    void descartar(Long notificacionId, Long empresaId);
}
