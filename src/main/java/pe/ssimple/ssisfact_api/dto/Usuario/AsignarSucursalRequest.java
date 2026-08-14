package pe.ssimple.ssisfact_api.dto.Usuario;

import lombok.Data;

@Data
public class AsignarSucursalRequest {

    // Sin @NotNull a propósito: null = quitar la sucursal asignada (usuario queda
    // sin acceso a ninguna, hasta que el admin le asigne una de nuevo).
    private Long sucursalId;
}
