package pe.ssimple.ssisfact_api.dto.Venta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IniciarVentaResult implements SpResponse {
    private String estado;
    private String mensaje;
    private Long id;
    private Long cajaId;
    private Long clienteId;
}
