package pe.ssimple.ssisfact_api.dto.Assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponse {
    private List<CarritoItemResponse> items;
    private BigDecimal totalEstimado;
}
