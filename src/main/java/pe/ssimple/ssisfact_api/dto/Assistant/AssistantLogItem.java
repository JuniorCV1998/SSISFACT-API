package pe.ssimple.ssisfact_api.dto.Assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantLogItem {
    private Long id;
    private Long empresaId;
    private Long usuarioId;
    private String rol;
    private String mensaje;
    private String toolName;
    private String toolArgs;
    private String toolResult;
    private LocalDateTime fechaCreacion;
}
