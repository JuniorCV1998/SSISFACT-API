package pe.ssimple.ssisfact_api.dto.Anthropic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnthropicMessageRequest {
    private String model;
    @JsonProperty("max_tokens")
    private int maxTokens;
    // Array de bloques (no String) para poder marcar cache_control en el system prompt.
    private List<AnthropicContentBlock> system;
    private List<AnthropicMessage> messages;
    private List<AnthropicTool> tools;
}
