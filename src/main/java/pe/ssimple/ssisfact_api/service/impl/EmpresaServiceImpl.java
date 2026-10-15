package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaAparienciaResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaTemaRequest;
import pe.ssimple.ssisfact_api.exception.ImagenValidationException;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;
import pe.ssimple.ssisfact_api.service.EmpresaService;
import pe.ssimple.ssisfact_api.service.StorageService;
import pe.ssimple.ssisfact_api.util.SqlErrorMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private static final long LOGO_MAX_BYTES = 2L * 1024 * 1024;

    private final EmpresaRepository empresaRepository;
    private final StorageService storageService;

    @Override
    public EmpresaActualResponse obtenerActual(Long empresaId) {
        return empresaRepository.obtenerActual(empresaId);
    }

    @Override
    public EmpresaAparienciaResponse obtenerApariencia(Long empresaId) {
        EmpresaActualResponse empresa = empresaRepository.obtenerActual(empresaId);
        return new EmpresaAparienciaResponse(
                empresa.getNombre(),
                empresa.getLogoUrl(),
                empresa.getLogoTicketUrl(),
                empresa.getColorPrimario(),
                empresa.getColorSecundario(),
                empresa.getColorAcento());
    }

    @Override
    public AdminSaveResponse actualizar(Long empresaId, EmpresaActualizarRequest request) {

        request.setNombre(request.getNombre().trim());
        request.setRuc(request.getRuc().trim());
        request.setDireccion(vacioComoNull(request.getDireccion()));
        request.setTelefono(vacioComoNull(request.getTelefono()));
        request.setEmail(vacioComoNull(request.getEmail()));
        request.setTicketMensajePie(vacioComoNull(request.getTicketMensajePie()));

        AdminSaveResponse response = empresaRepository.actualizar(empresaId, request);

        if ("ERROR_SQL".equals(response.getEstado())) {
            int code = SqlErrorMapper.extractCode(response.getMensaje());
            log.error("[actualizar] Error SQL en SP — empresaId={} sqlCode={} detalle='{}'",
                    empresaId, code, response.getMensaje());
            return new AdminSaveResponse("ERROR_SQL", SqlErrorMapper.toSafeMessage(response.getMensaje()), 0L);
        }

        if (!"OK".equals(response.getEstado())) {
            log.warn("[actualizar] SP devolvió error — estado={} mensaje='{}' empresaId={}",
                    response.getEstado(), response.getMensaje(), empresaId);
        }

        return response;
    }

    @Override
    public AdminSaveResponse actualizarLogo(Long empresaId, MultipartFile file) {

        if (file != null && file.getSize() > LOGO_MAX_BYTES) {
            throw new ImagenValidationException("El logo supera el tamaño máximo permitido (2MB)");
        }

        String logoAnterior = empresaRepository.obtenerLogoUrlActual(empresaId);

        String logoUrl = storageService.subirImagen(file, "empresas/" + empresaId);

        empresaRepository.actualizarLogo(empresaId, logoUrl);

        storageService.eliminarImagen(logoAnterior);

        return new AdminSaveResponse("OK", "Logo actualizado correctamente", empresaId);
    }

    @Override
    public AdminSaveResponse actualizarTema(Long empresaId, EmpresaTemaRequest request) {

        empresaRepository.actualizarTema(empresaId,
                request.getColorPrimario().toUpperCase(),
                request.getColorSecundario().toUpperCase(),
                request.getColorAcento().toUpperCase());

        return new AdminSaveResponse("OK", "Tema actualizado correctamente", empresaId);
    }

    @Override
    public AdminSaveResponse actualizarLogoTicket(Long empresaId, MultipartFile file) {

        if (file != null && file.getSize() > LOGO_MAX_BYTES) {
            throw new ImagenValidationException("El logo del ticket supera el tamaño máximo permitido (2MB)");
        }

        String logoAnterior = empresaRepository.obtenerLogoTicketUrlActual(empresaId);

        String logoUrl = storageService.subirImagen(file, "empresas/" + empresaId + "/ticket");

        empresaRepository.actualizarLogoTicket(empresaId, logoUrl);

        storageService.eliminarImagen(logoAnterior);

        return new AdminSaveResponse("OK", "Logo del ticket actualizado correctamente", empresaId);
    }

    private String vacioComoNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
