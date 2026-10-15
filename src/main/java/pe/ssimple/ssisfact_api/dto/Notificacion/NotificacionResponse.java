package pe.ssimple.ssisfact_api.dto.Notificacion;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacionResponse {
    private Long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String entidadTipo;
    private Long entidadId;
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLeida;

    // Solo se completa en listarNotificaciones (COUNT(*) OVER()) — no forma parte del dato en sí.
    private int totalRegistros;
}
