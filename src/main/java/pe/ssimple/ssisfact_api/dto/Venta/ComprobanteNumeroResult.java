package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteNumeroResult {
    private String estado;
    private String mensaje;
    private String serie;
    private Integer numero;
}
