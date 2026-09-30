package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.VentaService;
import pe.ssimple.ssisfact_api.util.SucursalAccessGuard;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/venta")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final SucursalAccessGuard sucursalAccessGuard;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VentaResponse>> registrarVenta(
            @Valid @RequestBody VentaRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        // Necesario aunque solo se pueda abrir caja en la sucursal asignada: si el
        // admin reasigna al usuario DESPUÉS de que ya tenía una caja abierta en la
        // sucursal anterior, esa caja sigue abierta y sp_iniciar_venta la acepta
        // igual — sin este guard, seguiría vendiendo en una sucursal que ya no es
        // la suya hasta que cierre esa caja vieja.
        sucursalAccessGuard.validar(user, request.getSucursalId());

        VentaResponse response = ventaService.registrarVenta(request, user.getEmpresaId(), user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Venta registrada correctamente", response));
    }

    // sucursalId es ignorado para no-admin: siempre se les fuerza SU sucursal
    // asignada, sin importar lo que manden. Si no tienen sucursal asignada,
    // no ven nada (no se filtra "sin sucursal" = toda la empresa).
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<VentaListResponse>> listarVentas(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long cajaId,
            @RequestParam(defaultValue = "") String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        boolean esAdmin = esAdmin(user);
        Long sucursalEfectiva = esAdmin ? sucursalId : user.getSucursalId();

        if (!esAdmin && sucursalEfectiva == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Ventas obtenidas correctamente", new VentaListResponse(List.of(), 0, page, size)));
        }

        VentaListResponse result = ventaService.listarVentas(
                user.getEmpresaId(), sucursalEfectiva, clienteId, cajaId, estado.isBlank() ? null : estado,
                fechaDesde, fechaHasta, page, size);

        return ResponseEntity.ok(ApiResponse.success("Ventas obtenidas correctamente", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaDetalleResponse>> obtenerVentaDetalle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        VentaDetalleResponse result = ventaService.obtenerVentaDetalle(id, user.getEmpresaId());

        // La venta propia siempre es visible para quien la hizo (ej. reimprimir su
        // propio ticket), aunque su sucursal asignada haya cambiado después.
        if (!result.getUsuarioId().equals(user.getUsuarioId())) {
            sucursalAccessGuard.validar(user, result.getSucursalId());
        }

        return ResponseEntity.ok(ApiResponse.success("Venta obtenida correctamente", result));
    }

    private boolean esAdmin(CustomUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping("/anular/{id}")
    public ResponseEntity<ApiResponse<VentaResponse>> anularVenta(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        VentaResponse response = ventaService.anularVenta(id, user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Venta anulada correctamente", response));
    }
}
