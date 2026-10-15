package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionListResponse;
import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionResponse;
import pe.ssimple.ssisfact_api.exception.NotificacionValidationException;
import pe.ssimple.ssisfact_api.repository.NotificacionRepository;
import pe.ssimple.ssisfact_api.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final VentaSseService ventaSseService;

    @Override
    public void crear(Long empresaId, Long usuarioId, String tipo, String titulo, String mensaje,
                       String entidadTipo, Long entidadId) {

        Long id = notificacionRepository.insertar(empresaId, usuarioId, tipo, titulo, mensaje, entidadTipo, entidadId);

        log.info("[Notificacion] creada — id={} tipo={} empresaId={} usuarioId={} entidadTipo={} entidadId={}",
                id, tipo, empresaId, usuarioId, entidadTipo, entidadId);

        // Solo se empuja en vivo si es para toda la empresa (usuario_id NULL): el
        // canal SSE agrupa por empresa, no por usuario, así que una notificación
        // dirigida a una persona puntual no debe salir por ahí o la verían todos.
        if (usuarioId == null) {
            NotificacionResponse payload = new NotificacionResponse();
            payload.setId(id);
            payload.setTipo(tipo);
            payload.setTitulo(titulo);
            payload.setMensaje(mensaje);
            payload.setEntidadTipo(entidadTipo);
            payload.setEntidadId(entidadId);
            payload.setLeida(false);
            payload.setFechaCreacion(LocalDateTime.now());
            ventaSseService.publicarNotificacion(empresaId, payload);
        }
    }

    @Override
    public NotificacionListResponse listar(Long empresaId, Long usuarioId, Boolean soloNoLeidas, int page, int size) {

        List<NotificacionResponse> items = notificacionRepository.listar(empresaId, usuarioId, soloNoLeidas, page, size);
        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();
        int noLeidas = notificacionRepository.contarNoLeidas(empresaId, usuarioId);

        return new NotificacionListResponse(items, total, noLeidas, page, size);
    }

    @Override
    public int contarNoLeidas(Long empresaId, Long usuarioId) {
        return notificacionRepository.contarNoLeidas(empresaId, usuarioId);
    }

    @Override
    public void marcarLeida(Long id, Long empresaId, Long usuarioId) {

        int filasActualizadas = notificacionRepository.marcarLeida(id, empresaId, usuarioId);
        if (filasActualizadas == 0) {
            throw new NotificacionValidationException("La notificación no existe, no te pertenece o ya estaba leída");
        }
    }

    @Override
    public void marcarTodasLeidas(Long empresaId, Long usuarioId) {
        notificacionRepository.marcarTodasLeidas(empresaId, usuarioId);
    }
}
