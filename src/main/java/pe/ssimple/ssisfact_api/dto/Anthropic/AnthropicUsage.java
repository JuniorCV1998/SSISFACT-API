package pe.ssimple.ssisfact_api.dto.Anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnthropicUsage {
    @JsonProperty("input_tokens")
    private int inputTokens;
    @JsonProperty("output_tokens")
    private int outputTokens;
}
