package pe.ssimple.ssisfact_api.dto.Anthropic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// Un solo tipo cubre los tres bloques de contenido de la Messages API que este
// proyecto usa: "text" (respuesta conversacional), "tool_use" (Claude pide
// ejecutar una tool) y "tool_result" (lo que le devolvemos tras ejecutarla).
// @JsonInclude(NON_NULL) omite los campos que no aplican a cada tipo.
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnthropicContentBlock {

    private String type; // "text" | "tool_use" | "tool_result"

    // type = text
    private String text;

    // type = tool_use
    private String id;   // tool_use_id a repetir en el tool_result correspondiente
    private String name;
    private Map<String, Object> input;

    // type = tool_result
    @JsonProperty("tool_use_id")
    private String toolUseId;
    private String content;
    @JsonProperty("is_error")
    private Boolean isError;

    // Marca este bloque como prefijo cacheable (usado en el bloque de system prompt).
    @JsonProperty("cache_control")
    private AnthropicCacheControl cacheControl;

    public static AnthropicContentBlock text(String text) {
        AnthropicContentBlock block = new AnthropicContentBlock();
        block.setType("text");
        block.setText(text);
        return block;
    }

    public static AnthropicContentBlock textCacheable(String text) {
        AnthropicContentBlock block = text(text);
        block.setCacheControl(AnthropicCacheControl.ephemeral());
        return block;
    }

    public static AnthropicContentBlock toolResult(String toolUseId, String content, boolean isError) {
        AnthropicContentBlock block = new AnthropicContentBlock();
        block.setType("tool_result");
        block.setToolUseId(toolUseId);
        block.setContent(content);
        block.setIsError(isError);
        return block;
    }
}
