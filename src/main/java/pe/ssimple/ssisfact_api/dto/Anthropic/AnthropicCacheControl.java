package pe.ssimple.ssisfact_api.dto.Anthropic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Marca un bloque (system, el último tool del catálogo, etc.) como cacheable por
// Anthropic (TTL de 5 min por defecto). Por debajo del mínimo de tokens cacheables
// del modelo (4096 en Haiku 4.5), la API simplemente lo ignora sin error — no hace
// falta condicionar esto por tamaño.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnthropicCacheControl {
    private String type;

    public static AnthropicCacheControl ephemeral() {
        return new AnthropicCacheControl("ephemeral");
    }
}
