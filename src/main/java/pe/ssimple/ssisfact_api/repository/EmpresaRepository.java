package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;

public interface EmpresaRepository {
    EmpresaActualResponse obtenerActual(Long empresaId);
    AdminSaveResponse actualizar(Long empresaId, EmpresaActualizarRequest request);
    String obtenerLogoUrlActual(Long empresaId);
    void actualizarLogo(Long empresaId, String logoUrl);
    // 0=eliminada, 1=activa, 2=suspendida. null si la empresa no existe.
    Integer obtenerEstado(Long empresaId);
    void actualizarConfigAsistente(Long empresaId, String nombreAsistente, String tono, String vozGenero);
}
