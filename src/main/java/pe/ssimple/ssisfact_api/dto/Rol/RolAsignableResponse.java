package pe.ssimple.ssisfact_api.dto.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolAsignableResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    // "ADMIN" (ADMIN, SUNAT) o "USER" (CAJERO, SUPERVISOR, ALMACEN, AUDITOR)
    private String tipo;
}
