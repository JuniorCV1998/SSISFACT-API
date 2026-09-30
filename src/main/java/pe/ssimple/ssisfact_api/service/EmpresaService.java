package pe.ssimple.ssisfact_api.service;

import org.springframework.web.multipart.MultipartFile;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;

public interface EmpresaService {
    EmpresaActualResponse obtenerActual(Long empresaId);
    AdminSaveResponse actualizar(Long empresaId, EmpresaActualizarRequest request);
    AdminSaveResponse actualizarLogo(Long empresaId, MultipartFile file);
}
