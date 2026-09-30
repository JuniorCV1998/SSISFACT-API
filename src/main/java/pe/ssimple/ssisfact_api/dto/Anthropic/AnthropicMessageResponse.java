package pe.ssimple.ssisfact_api.dto.Anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnthropicMessageResponse {
    private String id;
    private String type;
    private String role;
    private List<AnthropicContentBlock> content;
    private String model;
    @JsonProperty("stop_reason")
    private String stopReason; // "end_turn" | "tool_use" | "max_tokens" | ...
    @JsonProperty("stop_sequence")
    private String stopSequence;
    private AnthropicUsage usage;
}
