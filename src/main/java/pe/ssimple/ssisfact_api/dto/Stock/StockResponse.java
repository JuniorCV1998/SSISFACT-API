package pe.ssimple.ssisfact_api.dto.Stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.SpResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse implements SpResponse {

    private String estado;

    @JsonIgnore // ya viaja como "message" en el ApiResponse, evita duplicarlo en data
    private String mensaje;

    private Long id;
}
