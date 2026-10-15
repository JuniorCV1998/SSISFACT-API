package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;
import pe.ssimple.ssisfact_api.dto.Whatsapp.ClienteContactoDto;
import pe.ssimple.ssisfact_api.dto.Whatsapp.EmpresaProgramadaDto;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigResponse;

import java.time.LocalTime;
import java.util.List;

public interface EmpresaRepository {
    EmpresaActualResponse obtenerActual(Long empresaId);
    AdminSaveResponse actualizar(Long empresaId, EmpresaActualizarRequest request);
    String obtenerLogoUrlActual(Long empresaId);
    void actualizarLogo(Long empresaId, String logoUrl);
    // 0=eliminada, 1=activa, 2=suspendida. null si la empresa no existe.
    Integer obtenerEstado(Long empresaId);
    void actualizarConfigAsistente(Long empresaId, String nombreAsistente, String tono, String vozGenero);
    void actualizarTema(Long empresaId, String colorPrimario, String colorSecundario, String colorAcento);
    String obtenerLogoTicketUrlActual(Long empresaId);
    void actualizarLogoTicket(Long empresaId, String logoTicketUrl);

    void actualizarWhatsappEstado(Long empresaId, boolean vinculado);
    void actualizarWhatsappConfig(Long empresaId, boolean activo, String mensaje, LocalTime horaEnvio,
                                  String mensajeComprobante);
    WhatsappConfigResponse obtenerWhatsappConfig(Long empresaId);
    List<ClienteContactoDto> listarClientesActivosConTelefono(Long empresaId);
    List<EmpresaProgramadaDto> listarEmpresasParaEnvioProgramado(String horaActualHHmm);
    void marcarWhatsappEnvioRealizado(Long empresaId);
}
