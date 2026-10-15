package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Rol.RolAsignableResponse;

import java.util.List;

public interface RolRepository {
    // Roles de tipo 'USER' (CAJERO, SUPERVISOR, ALMACEN, AUDITOR): los únicos
    // que un admin de empresa puede asignar a sus trabajadores.
    List<RolAsignableResponse> listarAsignables();

    // Roles que el SUPERADMIN de la plataforma puede asignar a un usuario de una
    // empresa: tipo 'ADMIN' (ADMIN, SUNAT) y tipo 'USER'. Excluye 'SYSTEM' (SUPERADMIN).
    List<RolAsignableResponse> listarAsignablesSuperadmin();
}
