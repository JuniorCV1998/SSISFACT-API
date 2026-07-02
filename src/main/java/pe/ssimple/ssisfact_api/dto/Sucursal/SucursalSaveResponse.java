package pe.ssimple.ssisfact_api.dto.Sucursal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalSaveResponse implements SpResponse {
    private String estado;

    @JsonIgnore
    private String mensaje;

    private Long id;
}
