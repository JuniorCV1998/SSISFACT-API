package pe.ssimple.ssisfact_api.dto.Assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantChatRequest {

    @NotBlank(message = "es obligatorio")
    @Size(max = 2000, message = "no puede superar los 2000 caracteres")
    private String mensaje;

    // Módulo activo en el frontend (ej. "ventas", "inventario") — solo contexto
    // descriptivo para el system prompt, no se usa para autorización.
    private String moduloActivo;
}
