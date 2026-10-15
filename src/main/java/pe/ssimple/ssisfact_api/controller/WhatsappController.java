package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigRequest;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappEstadoResponse;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappVincularResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.WhatsappService;

@RestController
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
public class WhatsappController {

    private final WhatsappService whatsappService;

    @PostMapping("/conectar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WhatsappVincularResponse>> conectar(
            @AuthenticationPrincipal CustomUserDetails user) {

        WhatsappVincularResponse result = whatsappService.vincular(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Escanea el código QR con WhatsApp", result));
    }

    // Accesible a cualquier rol: el cajero necesita saber si el envío por WhatsApp está disponible.
    // Solo el ADMIN recibe el número vinculado.
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<WhatsappEstadoResponse>> estado(
            @AuthenticationPrincipal CustomUserDetails user) {

        WhatsappEstadoResponse result = whatsappService.consultarEstado(user.getEmpresaId());
        if (!esAdmin(user)) {
            result.setNumero(null);
        }

        return ResponseEntity.ok(ApiResponse.success("Estado de WhatsApp obtenido", result));
    }

    @PostMapping("/desconectar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desconectar(
            @AuthenticationPrincipal CustomUserDetails user) {

        whatsappService.desvincular(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("WhatsApp desvinculado", null));
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WhatsappConfigResponse>> obtenerConfig(
            @AuthenticationPrincipal CustomUserDetails user) {

        WhatsappConfigResponse result = whatsappService.obtenerConfig(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Configuración de WhatsApp obtenida", result));
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WhatsappConfigResponse>> actualizarConfig(
            @Valid @RequestBody WhatsappConfigRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        WhatsappConfigResponse result = whatsappService.actualizarConfig(user.getEmpresaId(), request);

        return ResponseEntity.ok(ApiResponse.success("Configuración de WhatsApp actualizada", result));
    }

    private boolean esAdmin(CustomUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
