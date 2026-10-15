package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.AsociarVentaPagoDigitalRequest;
import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalListResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalConfigResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalTokenResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.PagoDigitalService;

@RestController
@RequestMapping("/pagos-digitales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PagoDigitalController {

    private final PagoDigitalService pagoDigitalService;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PagoDigitalConfigResponse>> obtenerConfig(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(ApiResponse.success(
                "Configuración obtenida correctamente", pagoDigitalService.obtenerConfig(user.getEmpresaId())));
    }

    // El token se devuelve en claro solo en esta respuesta — no se vuelve a mostrar.
    // Sirve tanto para las notificaciones de Yape como de Plin (una sola macro por
    // app en el celular, mismo token, el "metodo" va en cada POST).
    @PostMapping("/config/regenerar-token")
    public ResponseEntity<ApiResponse<PagoDigitalTokenResponse>> regenerarToken(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(ApiResponse.success(
                "Token generado correctamente, guárdalo ahora: no se volverá a mostrar",
                pagoDigitalService.regenerarToken(user.getEmpresaId())));
    }

    @PostMapping("/config/deshabilitar")
    public ResponseEntity<ApiResponse<Void>> deshabilitar(
            @AuthenticationPrincipal CustomUserDetails user) {

        pagoDigitalService.deshabilitar(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Webhook de pagos digitales deshabilitado", null));
    }

    @GetMapping("/notificaciones")
    public ResponseEntity<ApiResponse<NotificacionPagoDigitalListResponse>> listarNotificaciones(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        NotificacionPagoDigitalListResponse result = pagoDigitalService.listarBandeja(user.getEmpresaId(), estado, page, size);

        return ResponseEntity.ok(ApiResponse.success("Notificaciones obtenidas correctamente", result));
    }

    @PostMapping("/notificaciones/{id}/asociar")
    public ResponseEntity<ApiResponse<Void>> asociarNotificacion(
            @PathVariable Long id,
            @Valid @RequestBody AsociarVentaPagoDigitalRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        pagoDigitalService.asociarManual(id, user.getEmpresaId(), request.getVentaId());

        return ResponseEntity.ok(ApiResponse.success("Pago asociado a la venta correctamente", null));
    }

    @PostMapping("/notificaciones/{id}/descartar")
    public ResponseEntity<ApiResponse<Void>> descartarNotificacion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        pagoDigitalService.descartar(id, user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Notificación descartada", null));
    }
}
