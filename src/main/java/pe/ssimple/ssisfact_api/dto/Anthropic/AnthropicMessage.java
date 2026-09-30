package pe.ssimple.ssisfact_api.dto.Anthropic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnthropicMessage {
    private String role; // "user" | "assistant"
    private List<AnthropicContentBlock> content;
}
