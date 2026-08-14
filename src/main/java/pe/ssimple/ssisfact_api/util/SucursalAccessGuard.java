package pe.ssimple.ssisfact_api.util;

import org.springframework.stereotype.Component;
import pe.ssimple.ssisfact_api.exception.AccesoSucursalDenegadoException;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;

@Component
public class SucursalAccessGuard {

    // ADMIN queda exento: puede operar (abrir caja, vender, manejar stock) en
    // cualquier sucursal de su empresa. El resto de roles queda limitado a la
    // sucursal que el admin le haya asignado en usuarios.sucursal_id.
    public void validar(CustomUserDetails user, Long sucursalIdSolicitada) {

        boolean esAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) {
            return;
        }

        Long sucursalIdUsuario = user.getSucursalId();

        if (sucursalIdUsuario == null) {
            throw new AccesoSucursalDenegadoException(
                    "No tienes una sucursal asignada. Contacta a tu administrador.");
        }

        if (!sucursalIdUsuario.equals(sucursalIdSolicitada)) {
            throw new AccesoSucursalDenegadoException("No tienes acceso a esta sucursal.");
        }
    }
}
