package pe.ssimple.ssisfact_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionListResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.NotificacionService;

import java.util.Map;

// Centro de notificaciones (campana) — cualquier usuario logueado ve las suyas y
// las de toda su empresa. No es solo para pagos digitales: cualquier otro módulo
// puede crear notificaciones inyectando NotificacionService (ver "tipo").
@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificacionListResponse>> listar(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Boolean soloNoLeidas,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        NotificacionListResponse result = notificacionService.listar(
                user.getEmpresaId(), user.getUsuarioId(), soloNoLeidas, page, size);

        return ResponseEntity.ok(ApiResponse.success("Notificaciones obtenidas correctamente", result));
    }

    @GetMapping("/no-leidas/contador")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> contarNoLeidas(
            @AuthenticationPrincipal CustomUserDetails user) {

        int total = notificacionService.contarNoLeidas(user.getEmpresaId(), user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Contador obtenido correctamente", Map.of("noLeidas", total)));
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        notificacionService.marcarLeida(id, user.getEmpresaId(), user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Notificación marcada como leída", null));
    }

    @PostMapping("/leer-todas")
    public ResponseEntity<ApiResponse<Void>> marcarTodasLeidas(
            @AuthenticationPrincipal CustomUserDetails user) {

        notificacionService.marcarTodasLeidas(user.getEmpresaId(), user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Notificaciones marcadas como leídas", null));
    }
}
