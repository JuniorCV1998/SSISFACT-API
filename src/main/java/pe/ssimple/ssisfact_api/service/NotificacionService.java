package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionListResponse;

public interface NotificacionService {

    // usuarioId null = notificación para toda la empresa (cualquier usuario la ve).
    void crear(Long empresaId, Long usuarioId, String tipo, String titulo, String mensaje,
               String entidadTipo, Long entidadId);

    NotificacionListResponse listar(Long empresaId, Long usuarioId, Boolean soloNoLeidas, int page, int size);

    int contarNoLeidas(Long empresaId, Long usuarioId);

    void marcarLeida(Long id, Long empresaId, Long usuarioId);

    void marcarTodasLeidas(Long empresaId, Long usuarioId);
}
