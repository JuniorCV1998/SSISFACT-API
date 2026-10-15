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

    // Sucursal activa seleccionada en el front (topbar). Solo se usa como fallback
    // cuando el usuario NO tiene sucursal fija en el JWT (ADMIN/SUPERADMIN, que
    // operan sobre todas las sucursales) — para un usuario con sucursal fija
    // (CAJERO/ALMACEN) este valor se ignora siempre, nunca se confía en el cliente
    // para eso. Se valida server-side que pertenezca a la empresa del usuario.
    private Long sucursalId;
}
