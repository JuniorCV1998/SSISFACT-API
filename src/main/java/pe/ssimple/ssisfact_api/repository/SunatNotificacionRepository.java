package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Sunat.SunatNotificationDto;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatPageResult;

public interface SunatNotificacionRepository {
    void upsert(Long empresaId, String ruc, SunatNotificationDto notificacion);
    SunatPageResult<SunatNotificationDto> listar(Long empresaId, int pagina, int tamanio);
}
