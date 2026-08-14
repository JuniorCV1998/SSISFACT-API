package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminListResponse;

import java.time.LocalDate;

public interface AdminEmpresaService {
    EmpresaAdminListResponse listarEmpresas(String busqueda, int estado, int page, int size);
    EmpresaAdminDetalleResponse obtenerEmpresa(Long empresaId);
    AdminSaveResponse actualizarEstadoEmpresa(Long empresaId, Integer estado);
    AdminSaveResponse actualizarPlanEmpresa(Long empresaId, String plan, Integer maxSucursal, Integer maxUsuarios, LocalDate fechaVencimiento);
    AdminSaveResponse asignarRolUsuario(Long usuarioId, Long empresaId, String rol);
}
