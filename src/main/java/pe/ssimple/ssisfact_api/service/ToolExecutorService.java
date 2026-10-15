package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicContentBlock;

import java.util.Map;

public interface ToolExecutorService {

    // Ejecuta una tool pedida por Claude y retorna siempre un bloque tool_result
    // (nunca lanza) — empresaId siempre viene de "user" (JWT), cualquier valor
    // equivalente que venga en "input" (JSON del modelo) se ignora. sucursalId es
    // la sucursal EFECTIVA ya resuelta por ClaudeServiceImpl (la fija del usuario
    // si la tiene, o la seleccionada en el front validada contra su empresa si no).
    AnthropicContentBlock ejecutar(String toolName, Map<String, Object> input, String toolUseId, CustomUserDetails user, Long sucursalId);
}
