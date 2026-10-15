package pe.ssimple.ssisfact_api.dto.PagoDigital;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionPagoDigitalListResponse {
    private List<NotificacionPagoDigitalResponse> items;
    private int totalRegistros;
    private int page;
    private int size;
}
