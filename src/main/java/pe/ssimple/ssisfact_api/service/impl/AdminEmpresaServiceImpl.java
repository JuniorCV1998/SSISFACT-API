package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminItemResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminListResponse;
import pe.ssimple.ssisfact_api.repository.AdminEmpresaRepository;
import pe.ssimple.ssisfact_api.service.AdminEmpresaService;
import pe.ssimple.ssisfact_api.service.SunatService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEmpresaServiceImpl implements AdminEmpresaService {

    private final AdminEmpresaRepository adminEmpresaRepository;
    private final SunatService sunatService;

    @Override
    public EmpresaAdminListResponse listarEmpresas(String busqueda, int estado, int page, int size) {

        List<EmpresaAdminItemResponse> items = adminEmpresaRepository.listarEmpresas(busqueda.trim(), estado, page, size);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new EmpresaAdminListResponse(items, total, page, size);
    }

    @Override
    public EmpresaAdminDetalleResponse obtenerEmpresa(Long empresaId) {

        EmpresaAdminDetalleResponse empresa = adminEmpresaRepository.obtenerEmpresa(empresaId);
        if (empresa == null) {
            return null;
        }

        empresa.setUsuarios(adminEmpresaRepository.listarUsuariosEmpresa(empresaId));
        empresa.setSunatConfigurado(sunatService.tieneCredencialesConfiguradas(empresaId));

        return empresa;
    }

    @Override
    public AdminSaveResponse actualizarEstadoEmpresa(Long empresaId, Integer estado) {
        return adminEmpresaRepository.actualizarEstadoEmpresa(empresaId, estado);
    }

    @Override
    public AdminSaveResponse actualizarPlanEmpresa(Long empresaId, String plan, Integer maxSucursal, Integer maxUsuarios, LocalDate fechaVencimiento) {
        return adminEmpresaRepository.actualizarPlanEmpresa(empresaId, plan.trim().toUpperCase(), maxSucursal, maxUsuarios, fechaVencimiento);
    }

    @Override
    public AdminSaveResponse asignarRolUsuario(Long usuarioId, Long empresaId, String rol) {
        return adminEmpresaRepository.asignarRolUsuario(usuarioId, empresaId, rol.trim().toUpperCase());
    }
}
