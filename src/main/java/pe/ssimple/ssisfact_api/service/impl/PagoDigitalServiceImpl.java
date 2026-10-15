package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalListResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalConfigResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalTokenResponse;
import pe.ssimple.ssisfact_api.exception.PagoDigitalValidationException;
import pe.ssimple.ssisfact_api.exception.WebhookAutenticacionException;
import pe.ssimple.ssisfact_api.repository.PagoDigitalRepository;
import pe.ssimple.ssisfact_api.repository.VentaRepository;
import pe.ssimple.ssisfact_api.service.NotificacionService;
import pe.ssimple.ssisfact_api.service.PagoDigitalService;
import pe.ssimple.ssisfact_api.service.VentaService;
import pe.ssimple.ssisfact_api.util.WebhookTokenUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoDigitalServiceImpl implements PagoDigitalService {

    // Ruta relativa del webhook — el front le antepone la base URL del backend (la
    // misma que ya usa para el resto de la API) al mostrársela al usuario.
    private static final String URL_WEBHOOK = "/webhooks/pagos-digitales/notificacion";

    // Billeteras soportadas por este webhook. "metodo" viaja en el body de cada POST
    // porque un solo celular puede tener macros de MacroDroid para Yape y para Plin
    // apuntando al mismo token/URL.
    private static final Set<String> METODOS_SOPORTADOS = Set.of("YAPE", "PLIN INTERBANK");

    private final PagoDigitalRepository pagoDigitalRepository;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;
    private final NotificacionService notificacionService;

    @Value("${pagos-digitales.matching.ventana-horas}")
    private int ventanaHoras;

    @Override
    public PagoDigitalConfigResponse obtenerConfig(Long empresaId) {

        PagoDigitalConfigResponse config = pagoDigitalRepository.obtenerConfig(empresaId);
        config.setUrlWebhook(URL_WEBHOOK);
        return config;
    }

    @Override
    @Transactional
    public PagoDigitalTokenResponse regenerarToken(Long empresaId) {

        String token = WebhookTokenUtil.generarToken();
        pagoDigitalRepository.habilitarToken(empresaId, WebhookTokenUtil.hash(token));

        log.info("[PagoDigital] token de webhook regenerado — empresaId={}", empresaId);

        return new PagoDigitalTokenResponse(token);
    }

    @Override
    public void deshabilitar(Long empresaId) {
        pagoDigitalRepository.deshabilitar(empresaId);
    }

    @Override
    @Transactional
    public void recibirNotificacion(String tokenPlano, String metodo, BigDecimal monto, String remitente, String textoOriginal) {

        if (tokenPlano == null || tokenPlano.isBlank()) {
            throw new WebhookAutenticacionException("Falta el token del webhook");
        }

        String metodoNormalizado = metodo == null ? "" : metodo.trim().toUpperCase();
        if (!METODOS_SOPORTADOS.contains(metodoNormalizado)) {
            throw new PagoDigitalValidationException("Método no soportado: " + metodo + " (debe ser YAPE o PLIN INTERBANK)");
        }

        Long empresaId = pagoDigitalRepository.buscarEmpresaPorTokenHash(WebhookTokenUtil.hash(tokenPlano));
        if (empresaId == null) {
            throw new WebhookAutenticacionException("Token inválido o webhook deshabilitado");
        }

        Long notificacionId = pagoDigitalRepository.insertarNotificacion(empresaId, metodoNormalizado, monto, remitente, textoOriginal);

        // 1° intento: match exacto (el monto cubre el saldo completo de la venta).
        // Tiene prioridad sobre el parcial porque es la señal más confiable — si hay
        // una única venta cuyo saldo coincide al centavo, es casi seguro que es esa.
        List<Long> candidatas = ventaRepository.buscarVentasPendientesPorSaldo(empresaId, monto, ventanaHoras);
        boolean esParcial = false;

        // 2° intento, solo si no hubo NINGÚN match exacto: el monto puede ser un abono
        // parcial (ej. venta de S/5 dividida en Yape S/2 + Plin S/3 — cada notificación
        // por separado nunca va a igualar el saldo completo). Se busca entre ventas con
        // saldo MAYOR al monto. Si una ambigüedad exacta ya existe (candidatas.size() > 1)
        // no se mezcla con parciales — esa ambigüedad se resuelve a mano igual que antes.
        if (candidatas.isEmpty()) {
            candidatas = ventaRepository.buscarVentasPendientesPorSaldoParcial(empresaId, monto, ventanaHoras);
            esParcial = true;
        }

        if (candidatas.size() == 1) {
            Long ventaId = candidatas.get(0);
            // registrarPagoVenta ya soporta montos menores al saldo (abono parcial, la
            // venta queda PENDIENTE con el saldo reducido) y montos que igualan el saldo
            // (la marca PAGADA) — no hace falta rama distinta para el caso parcial.
            ventaService.registrarPagoVenta(ventaId, empresaId, metodoNormalizado, monto, remitente);
            pagoDigitalRepository.confirmarNotificacion(notificacionId, ventaId, "CONFIRMADA_AUTO");
            log.info("[PagoDigital] notificación {} confirmada automáticamente — metodo={} ventaId={} empresaId={} parcial={}",
                    notificacionId, metodoNormalizado, ventaId, empresaId, esParcial);
        } else {
            log.info("[PagoDigital] notificación {} sin match único ({} candidatas, parcial={}) — queda en bandeja. metodo={} empresaId={}",
                    notificacionId, candidatas.size(), esParcial, metodoNormalizado, empresaId);

            // Sin esto, el pago queda "flotando" en la bandeja sin que nadie se entere:
            // la venta que lo está esperando (si existe) nunca recibe aviso porque
            // justamente no se pudo decidir a cuál asociarlo. Se avisa por la campana
            // para que alguien lo resuelva a mano en vez de esperar indefinidamente.
            // Igual que en VentaServiceImpl.registrarPagoVenta: las ventas se identifican
            // por el código de su comprobante, no por su id interno (que el front nunca
            // muestra en pantalla y por lo tanto no le sirve de nada al usuario).
            String remitenteTexto = remitente != null && !remitente.isBlank() ? " de " + remitente : "";
            String mensaje;
            if (candidatas.isEmpty()) {
                mensaje = "Llegó un pago de S/ " + monto + " por " + metodoNormalizado + remitenteTexto
                        + " que no coincide con ninguna venta pendiente, ni siquiera como abono parcial. "
                        + "Revísalo en la Bandeja de pagos digitales.";
            } else {
                String listaVentas = candidatas.stream()
                        .map(id -> {
                            String codigo = ventaRepository.obtenerCodigoComprobante(id);
                            return codigo != null ? codigo : "#" + id;
                        })
                        .collect(Collectors.joining(", "));
                mensaje = esParcial
                        ? "Llegó un pago de S/ " + monto + " por " + metodoNormalizado + remitenteTexto
                                + ", pero hay " + candidatas.size() + " ventas pendientes con saldo mayor a ese monto ("
                                + listaVentas + ") que podrían corresponder a un abono parcial y no se pudo confirmar "
                                + "solo. Asócialo manualmente en la Bandeja de pagos digitales."
                        : "Llegó un pago de S/ " + monto + " por " + metodoNormalizado + remitenteTexto
                                + ", pero hay " + candidatas.size() + " ventas pendientes con ese mismo monto ("
                                + listaVentas + ") y no se pudo confirmar solo. Asócialo manualmente en la Bandeja de pagos digitales.";
            }

            notificacionService.crear(empresaId, null, "PAGO_DIGITAL_SIN_MATCH", "Pago sin confirmar automáticamente",
                    mensaje, "PAGO_DIGITAL_NOTIFICACION", notificacionId);
        }
    }

    @Override
    public NotificacionPagoDigitalListResponse listarBandeja(Long empresaId, String estado, int page, int size) {

        List<NotificacionPagoDigitalResponse> items = pagoDigitalRepository.listarBandeja(empresaId, estado, page, size);
        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new NotificacionPagoDigitalListResponse(items, total, page, size);
    }

    @Override
    @Transactional
    public void asociarManual(Long notificacionId, Long empresaId, Long ventaId) {

        NotificacionPagoDigitalResponse notificacion = obtenerNotificacionPendiente(notificacionId, empresaId);

        ventaService.registrarPagoVenta(ventaId, empresaId, notificacion.getMetodo(), notificacion.getMonto(), notificacion.getRemitente());
        pagoDigitalRepository.confirmarNotificacion(notificacionId, ventaId, "CONFIRMADA_MANUAL");

        log.info("[PagoDigital] notificación {} asociada manualmente — ventaId={} empresaId={}",
                notificacionId, ventaId, empresaId);
    }

    @Override
    @Transactional
    public void descartar(Long notificacionId, Long empresaId) {

        obtenerNotificacionPendiente(notificacionId, empresaId);
        pagoDigitalRepository.marcarDescartada(notificacionId);
    }

    private NotificacionPagoDigitalResponse obtenerNotificacionPendiente(Long notificacionId, Long empresaId) {

        NotificacionPagoDigitalResponse notificacion = pagoDigitalRepository.obtenerNotificacion(notificacionId, empresaId);
        if (notificacion == null) {
            throw new PagoDigitalValidationException("La notificación no existe");
        }
        if (!"SIN_MATCH".equals(notificacion.getEstado())) {
            throw new PagoDigitalValidationException("La notificación ya fue procesada (estado actual: " + notificacion.getEstado() + ")");
        }
        return notificacion;
    }
}
