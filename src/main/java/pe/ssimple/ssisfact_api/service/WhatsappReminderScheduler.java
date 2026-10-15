package pe.ssimple.ssisfact_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.ssimple.ssisfact_api.dto.Whatsapp.ClienteContactoDto;
import pe.ssimple.ssisfact_api.dto.Whatsapp.EmpresaProgramadaDto;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cada minuto, revisa qué empresas tienen el recordatorio de WhatsApp activo y
 * configurado para esta hora, y les envía el mensaje (con {nombre} reemplazado)
 * a todos sus clientes activos con teléfono. Un fallo individual no detiene el
 * resto del envío; se marca whatsapp_fecha_ultimo_envio al terminar para no
 * reenviar el mismo día.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsappReminderScheduler {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final EmpresaRepository empresaRepository;
    private final EvolutionApiClient evolutionApiClient;

    @Scheduled(cron = "0 * * * * *", zone = "America/Lima")
    public void enviarRecordatoriosProgramados() {
        String horaActual = LocalTime.now(ZONA_LIMA).format(FORMATO_HORA);

        List<EmpresaProgramadaDto> empresas = empresaRepository.listarEmpresasParaEnvioProgramado(horaActual);
        for (EmpresaProgramadaDto empresa : empresas) {
            enviarRecordatorioEmpresa(empresa);
        }
    }

    private void enviarRecordatorioEmpresa(EmpresaProgramadaDto empresa) {
        List<ClienteContactoDto> clientes = empresaRepository.listarClientesActivosConTelefono(empresa.getEmpresaId());
        String instanceName = "empresa_" + empresa.getEmpresaId();

        int enviados = 0;
        for (ClienteContactoDto cliente : clientes) {
            try {
                String texto = empresa.getMensaje().replace("{nombre}", cliente.getNombre());
                evolutionApiClient.enviarTexto(instanceName, cliente.getTelefono(), texto);
                enviados++;
            } catch (Exception e) {
                log.warn("Recordatorio WhatsApp: fallo al enviar a cliente {} de empresa {}: {}",
                        cliente.getId(), empresa.getEmpresaId(), e.getMessage());
            }
        }

        empresaRepository.marcarWhatsappEnvioRealizado(empresa.getEmpresaId());
        log.info("Recordatorio WhatsApp: empresa {} — {}/{} mensajes enviados",
                empresa.getEmpresaId(), enviados, clientes.size());
    }
}
