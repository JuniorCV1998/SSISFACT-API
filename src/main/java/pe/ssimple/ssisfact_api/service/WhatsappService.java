package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigRequest;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappEstadoResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappVincularResponse;

public interface WhatsappService {
    WhatsappVincularResponse vincular(Long empresaId);
    WhatsappEstadoResponse consultarEstado(Long empresaId);
    void desvincular(Long empresaId);
    WhatsappConfigResponse actualizarConfig(Long empresaId, WhatsappConfigRequest request);
    WhatsappConfigResponse obtenerConfig(Long empresaId);
    void enviarVenta(Long empresaId, Long ventaId, String numeroOverride);
}
