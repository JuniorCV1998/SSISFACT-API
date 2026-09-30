package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantChatRequest;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantChatResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantConfigRequest;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantConfigResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantCorrectionRequest;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantMemoriaResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.CarritoResponse;
import pe.ssimple.ssisfact_api.repository.AssistantMemoryRepository;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;
import pe.ssimple.ssisfact_api.service.AssistantCarritoService;
import pe.ssimple.ssisfact_api.service.AssistantMemoryService;
import pe.ssimple.ssisfact_api.service.AssistantRateLimiterService;
import pe.ssimple.ssisfact_api.service.ClaudeService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final ClaudeService claudeService;
    private final AssistantMemoryService assistantMemoryService;
    private final AssistantMemoryRepository assistantMemoryRepository;
    private final AssistantRateLimiterService assistantRateLimiterService;
    private final AssistantCarritoService assistantCarritoService;
    private final EmpresaRepository empresaRepository;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AssistantChatResponse>> chat(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AssistantChatRequest request) {

        assistantRateLimiterService.checkLimit(user.getUsuarioId());

        AssistantChatResponse response = claudeService.chat(user, request.getMensaje(), request.getModuloActivo());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/correct")
    public ResponseEntity<ApiResponse<Void>> correct(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AssistantCorrectionRequest request) {

        assistantMemoryService.saveCorrection(user.getEmpresaId(), request.getContexto(), request.getCorreccion());

        return ResponseEntity.ok(ApiResponse.success("Corrección registrada", null));
    }

    @GetMapping("/memory")
    public ResponseEntity<ApiResponse<List<AssistantMemoriaResponse>>> listarMemoria(
            @AuthenticationPrincipal CustomUserDetails user) {

        List<AssistantMemoriaResponse> memorias = assistantMemoryRepository.listarMemorias(user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Memorias obtenidas correctamente", memorias));
    }

    @DeleteMapping("/memory/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarMemoria(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {

        int filas = assistantMemoryRepository.eliminarMemoria(id, user.getEmpresaId());
        if (filas == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Memoria no encontrada"));
        }

        return ResponseEntity.ok(ApiResponse.success("Memoria eliminada", null));
    }

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<CarritoResponse>> verCarrito(
            @AuthenticationPrincipal CustomUserDetails user) {

        CarritoResponse carrito = assistantCarritoService.obtenerCarrito(user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success(carrito));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<Void>> vaciarCarrito(
            @AuthenticationPrincipal CustomUserDetails user) {

        assistantCarritoService.vaciarCarrito(user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Pedido vaciado", null));
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssistantConfigResponse>> actualizarConfig(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AssistantConfigRequest request) {

        empresaRepository.actualizarConfigAsistente(user.getEmpresaId(), request.getNombreAsistente(),
                request.getTono(), request.getVozGenero());

        return ResponseEntity.ok(ApiResponse.success("Configuración de Fact actualizada",
                new AssistantConfigResponse(request.getNombreAsistente(), request.getTono(), request.getVozGenero())));
    }
}
