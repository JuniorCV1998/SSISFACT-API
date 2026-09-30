package pe.ssimple.ssisfact_api.dto.Assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantLearningQueueItem {
    private Long id;
    private Long empresaId;
    private String tipoEvento;
    private String payload;
    private LocalDateTime fechaCreacion;
}
