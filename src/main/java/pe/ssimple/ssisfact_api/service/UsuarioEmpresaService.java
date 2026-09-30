package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;

import java.util.List;

public interface UsuarioEmpresaService {
    List<UsuarioAdminResponse> listarUsuariosEmpresa(Long empresaId);
    AdminSaveResponse asignarSucursal(Long usuarioId, Long empresaId, Long sucursalId);
    AdminSaveResponse crearUsuario(Long empresaId, String nombre, String email, String documento,
                                    String password, String rol, Long sucursalId);
    AdminSaveResponse asignarRol(Long usuarioId, Long empresaId, String rol);
    AdminSaveResponse activarUsuario(Long usuarioId, Long empresaId);
    // actorUsuarioId: quien ejecuta la acción, para bloquear la auto-desactivación.
    AdminSaveResponse desactivarUsuario(Long usuarioId, Long empresaId, Long actorUsuarioId);
}
