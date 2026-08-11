package pe.ssimple.ssisfact_api.dto.Stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetiroStockResponse implements SpResponse {

    private String estado;

    @JsonIgnore
    private String mensaje;

    private Long stock; // stock actual tras el retiro
}
