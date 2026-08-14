package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;

import java.util.List;

// Gestión de trabajadores dentro de la PROPIA empresa (JdbcTemplate + SP), distinto
// del UsuarioRepository (JPA) usado para autenticación/registro.
public interface UsuarioEmpresaRepository {
    List<UsuarioAdminResponse> listarUsuariosEmpresa(Long empresaId);
    AdminSaveResponse asignarSucursal(Long usuarioId, Long empresaId, Long sucursalId);
    AdminSaveResponse crearUsuario(Long empresaId, String nombre, String email, String documento,
                                    String contrasenaHash, String rol, Long sucursalId);
    AdminSaveResponse asignarRol(Long usuarioId, Long empresaId, String rol);
}
