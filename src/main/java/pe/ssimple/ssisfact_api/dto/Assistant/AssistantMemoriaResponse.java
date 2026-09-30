package pe.ssimple.ssisfact_api.dto.Assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantMemoriaResponse {
    private Long id;
    private String tipo;
    private String clave;
    private String valor;
    private Integer usos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
