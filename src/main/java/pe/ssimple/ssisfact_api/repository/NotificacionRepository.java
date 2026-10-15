package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionResponse;

import java.util.List;

public interface NotificacionRepository {

    Long insertar(Long empresaId, Long usuarioId, String tipo, String titulo, String mensaje,
                   String entidadTipo, Long entidadId);

    List<NotificacionResponse> listar(Long empresaId, Long usuarioId, Boolean soloNoLeidas, int page, int size);

    int contarNoLeidas(Long empresaId, Long usuarioId);

    int marcarLeida(Long id, Long empresaId, Long usuarioId);

    void marcarTodasLeidas(Long empresaId, Long usuarioId);
}
