package pe.ssimple.ssisfact_api.dto.Assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {
    private String respuesta;
    private List<String> toolsUsados;
}
