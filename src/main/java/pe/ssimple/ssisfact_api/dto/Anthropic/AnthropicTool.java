package pe.ssimple.ssisfact_api.dto.Anthropic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnthropicTool {
    private String name;
    private String description;
    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;
    // Se marca solo en el último tool del catálogo para cachear todo el arreglo
    // de tools como un único prefijo (ver AssistantToolCatalog.tools()).
    @JsonProperty("cache_control")
    private AnthropicCacheControl cacheControl;

    public AnthropicTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
