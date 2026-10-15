package pe.ssimple.ssisfact_api.dto.PagoDigital;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDigitalConfigResponse {
    private boolean habilitado;
    private boolean tieneToken;
    // Ruta del webhook (relativa) para pegar en la app de automatización del celular,
    // junto con la base URL del backend que el front ya conoce. Sirve tanto para
    // notificaciones de Yape como de Plin (el método va en el body de cada POST).
    private String urlWebhook;
}
