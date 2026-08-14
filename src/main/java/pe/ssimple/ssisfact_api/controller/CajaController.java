package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaAbrirRequest;
import pe.ssimple.ssisfact_api.dto.Caja.CajaActualResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarRequest;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaListResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaResumenResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaSaveResponse;
import pe.ssimple.ssisfact_api.service.CajaService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;
import pe.ssimple.ssisfact_api.util.SucursalAccessGuard;

@RestController
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;
    private final SucursalAccessGuard sucursalAccessGuard;

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<CajaSaveResponse>> abrirCaja(
            @Valid @RequestBody CajaAbrirRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        sucursalAccessGuard.validar(user, request.getSucursalId());

        CajaSaveResponse response = cajaService.abrirCaja(
                request.getSucursalId(), user.getUsuarioId(), request.getMontoInicial());

        return ResponseBuilder.build(response, "OK");
    }

    @PostMapping("/cerrar")
    public ResponseEntity<ApiResponse<CajaCerrarResponse>> cerrarCaja(
            @Valid @RequestBody CajaCerrarRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        CajaCerrarResponse response = cajaService.cerrarCaja(
                request.getCajaId(), user.getUsuarioId(), request.getMontoFinal());

        return ResponseBuilder.build(response, "OK");
    }

    @GetMapping("/actual")
    public ResponseEntity<ApiResponse<CajaActualResponse>> obtenerCajaAbierta(
            @AuthenticationPrincipal CustomUserDetails user) {

        CajaActualResponse result = cajaService.obtenerCajaAbierta(user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success(
                result != null ? "Caja abierta encontrada" : "No tienes una caja abierta", result));
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<CajaListResponse>> listarCajas(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(defaultValue = "") String estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        CajaListResponse result = cajaService.listarCajas(
                user.getEmpresaId(), sucursalId, estado.isBlank() ? null : estado, page, size);

        return ResponseEntity.ok(ApiResponse.success("Cajas obtenidas correctamente", result));
    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<ApiResponse<CajaResumenResponse>> obtenerResumenCaja(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        CajaResumenResponse resumen = cajaService.obtenerResumenCaja(id, user.getEmpresaId());

        if (resumen == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La caja no existe"));
        }

        return ResponseEntity.ok(ApiResponse.success("Resumen de caja obtenido correctamente", resumen));
    }
}
