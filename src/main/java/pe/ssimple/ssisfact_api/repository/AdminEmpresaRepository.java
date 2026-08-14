package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminItemResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;

import java.time.LocalDate;
import java.util.List;

public interface AdminEmpresaRepository {
    List<EmpresaAdminItemResponse> listarEmpresas(String busqueda, int estado, int page, int size);
    EmpresaAdminDetalleResponse obtenerEmpresa(Long empresaId);
    List<UsuarioAdminResponse> listarUsuariosEmpresa(Long empresaId);
    AdminSaveResponse actualizarEstadoEmpresa(Long empresaId, Integer estado);
    AdminSaveResponse actualizarPlanEmpresa(Long empresaId, String plan, Integer maxSucursal, Integer maxUsuarios, LocalDate fechaVencimiento);
    AdminSaveResponse asignarRolUsuario(Long usuarioId, Long empresaId, String rol);
}
