package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Assistant.AssistantChatResponse;

public interface ClaudeService {

    AssistantChatResponse chat(CustomUserDetails user, String mensaje, String moduloActivo);
}
