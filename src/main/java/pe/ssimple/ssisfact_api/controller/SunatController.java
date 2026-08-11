package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatApiResponse;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatCredentialsRequest;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatMessageListResponse;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatNotificationListResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.SunatService;

@RestController
@RequestMapping("/api/sunat")
@RequiredArgsConstructor
public class SunatController {

    private final SunatService sunatService;

    @GetMapping("/notificaciones")
    public ResponseEntity<SunatApiResponse<SunatNotificationListResponse>> getNotificaciones(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(sunatService.getNotificaciones(user.getEmpresaId(), page, size, false));
    }

    @GetMapping("/mensajes")
    public ResponseEntity<SunatApiResponse<SunatMessageListResponse>> getMensajes(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(sunatService.getMensajes(user.getEmpresaId(), page, size, false));
    }

    @PostMapping("/notificaciones/sincronizar")
    public ResponseEntity<SunatApiResponse<SunatNotificationListResponse>> sincronizarNotificaciones(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(sunatService.getNotificaciones(user.getEmpresaId(), page, size, true));
    }

    @PostMapping("/mensajes/sincronizar")
    public ResponseEntity<SunatApiResponse<SunatMessageListResponse>> sincronizarMensajes(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(sunatService.getMensajes(user.getEmpresaId(), page, size, true));
    }

    @GetMapping("/credenciales")
    public ResponseEntity<SunatApiResponse<Boolean>> estadoCredenciales(
            @AuthenticationPrincipal CustomUserDetails user) {
        boolean configuradas = sunatService.tieneCredencialesConfiguradas(user.getEmpresaId());
        return ResponseEntity.ok(SunatApiResponse.success(configuradas, null));
    }

    @PostMapping("/credenciales")
    public ResponseEntity<SunatApiResponse<Void>> guardarCredenciales(
            @Valid @RequestBody SunatCredentialsRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        SunatApiResponse<Void> response =
                sunatService.guardarCredenciales(user.getEmpresaId(), request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
