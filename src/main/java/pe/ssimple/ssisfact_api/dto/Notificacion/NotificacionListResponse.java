package pe.ssimple.ssisfact_api.dto.Notificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionListResponse {
    private List<NotificacionResponse> items;
    private int totalRegistros;
    private int noLeidas;
    private int page;
    private int size;
}
