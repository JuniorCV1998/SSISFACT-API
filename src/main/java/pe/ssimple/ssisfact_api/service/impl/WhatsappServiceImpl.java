package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigRequest;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappEstadoResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappVincularResponse;
import pe.ssimple.ssisfact_api.exception.WhatsappValidationException;
import pe.ssimple.ssisfact_api.repository.ClienteRepository;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;
import pe.ssimple.ssisfact_api.service.EvolutionApiClient;
import pe.ssimple.ssisfact_api.service.VentaPdfService;
import pe.ssimple.ssisfact_api.service.VentaService;
import pe.ssimple.ssisfact_api.service.WhatsappService;

import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private final EvolutionApiClient evolutionApiClient;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final VentaService ventaService;
    private final VentaPdfService ventaPdfService;

    @Override
    public WhatsappVincularResponse vincular(Long empresaId) {
        String instanceName = instanceName(empresaId);

        if ("open".equalsIgnoreCase(evolutionApiClient.obtenerEstadoConexion(instanceName))) {
            throw new WhatsappValidationException(
                    "Ya tienes WhatsApp vinculado. Desvincula primero si deseas generar un nuevo código QR.");
        }

        evolutionApiClient.eliminarInstancia(instanceName);
        WhatsappVincularResponse resultado = evolutionApiClient.crearInstancia(instanceName);
        empresaRepository.actualizarWhatsappEstado(empresaId, false);
        return resultado;
    }

    @Override
    public WhatsappEstadoResponse consultarEstado(Long empresaId) {
        String instanceName = instanceName(empresaId);

        String estadoEvolution = evolutionApiClient.obtenerEstadoConexion(instanceName);
        boolean vinculado = "open".equalsIgnoreCase(estadoEvolution);
        String numero = vinculado ? evolutionApiClient.obtenerNumeroVinculado(instanceName) : null;

        empresaRepository.actualizarWhatsappEstado(empresaId, vinculado);
        return new WhatsappEstadoResponse(mapearEstado(estadoEvolution), numero);
    }

    private String mapearEstado(String estadoEvolution) {
        if ("open".equalsIgnoreCase(estadoEvolution)) {
            return "CONECTADO";
        }
        if ("connecting".equalsIgnoreCase(estadoEvolution)) {
            return "CONECTANDO";
        }
        return "DESCONECTADO";
    }

    @Override
    public void desvincular(Long empresaId) {
        evolutionApiClient.eliminarInstancia(instanceName(empresaId));
        empresaRepository.actualizarWhatsappEstado(empresaId, false);
    }

    @Override
    public WhatsappConfigResponse actualizarConfig(Long empresaId, WhatsappConfigRequest request) {
        WhatsappConfigResponse actual = empresaRepository.obtenerWhatsappConfig(empresaId);

        String mensaje = request.getMensaje() == null || request.getMensaje().isBlank() ? null : request.getMensaje();
        String horaEnvioTexto = request.getHoraEnvio() != null ? request.getHoraEnvio() : actual.getHoraEnvio();
        String mensajeFinal = mensaje != null ? mensaje : actual.getMensaje();

        if (request.isActivo() && (mensajeFinal == null || mensajeFinal.isBlank() || horaEnvioTexto == null)) {
            throw new WhatsappValidationException(
                    "Para activar el mensaje automático necesitas un mensaje y una hora de envío.");
        }

        LocalTime horaEnvio = request.getHoraEnvio() != null ? LocalTime.parse(request.getHoraEnvio()) : null;
        empresaRepository.actualizarWhatsappConfig(empresaId, request.isActivo(), mensaje, horaEnvio,
                request.getMensajeComprobante() == null ? null : request.getMensajeComprobante().trim());
        return obtenerConfig(empresaId);
    }

    @Override
    public WhatsappConfigResponse obtenerConfig(Long empresaId) {
        return empresaRepository.obtenerWhatsappConfig(empresaId);
    }

    @Override
    public void enviarVenta(Long empresaId, Long ventaId, String numeroOverride) {
        VentaDetalleResponse detalle = ventaService.obtenerVentaDetalle(ventaId, empresaId);

        String numero = (numeroOverride != null && !numeroOverride.isBlank())
                ? numeroOverride
                : clienteRepository.obtenerTelefono(detalle.getClienteId());

        if (numero == null || numero.isBlank()) {
            throw new WhatsappValidationException(
                    "El cliente no tiene teléfono registrado. Indica un número para enviar el comprobante.");
        }

        byte[] pdf = ventaPdfService.generarPdf(detalle);
        String nombreArchivo = (detalle.getComprobanteCodigo() != null
                ? detalle.getComprobanteCodigo() : ("venta-" + ventaId)) + ".pdf";

        String caption = empresaRepository.obtenerWhatsappConfig(empresaId).getMensajeComprobante()
                .replace("{{TIPO_COMPROBANTE}}", Objects.toString(detalle.getDocumentoTitulo(), ""))
                .replace("{{SERIE-NÚMERO}}", Objects.toString(detalle.getComprobanteCodigo(), ""));

        evolutionApiClient.enviarDocumento(instanceName(empresaId), numero, pdf, nombreArchivo, caption);
    }

    private String instanceName(Long empresaId) {
        return "empresa_" + empresaId;
    }
}
