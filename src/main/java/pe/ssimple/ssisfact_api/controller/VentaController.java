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

import java.time.LocalDate;

@RestController
@RequestMapping("/venta")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<VentaResponse>> registrarVenta(
            @Valid @RequestBody VentaRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        VentaResponse response = ventaService.registrarVenta(request, user.getEmpresaId(), user.getUsuarioId());

        return ResponseEntity.ok(ApiResponse.success("Venta registrada correctamente", response));
    }

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

        VentaListResponse result = ventaService.listarVentas(
                user.getEmpresaId(), sucursalId, clienteId, cajaId, estado.isBlank() ? null : estado,
                fechaDesde, fechaHasta, page, size);

        return ResponseEntity.ok(ApiResponse.success("Ventas obtenidas correctamente", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaDetalleResponse>> obtenerVentaDetalle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        VentaDetalleResponse result = ventaService.obtenerVentaDetalle(id, user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Venta obtenida correctamente", result));
    }

    @PostMapping("/anular/{id}")
    public ResponseEntity<ApiResponse<VentaResponse>> anularVenta(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        VentaResponse response = ventaService.anularVenta(id, user.getEmpresaId());

        return ResponseEntity.ok(ApiResponse.success("Venta anulada correctamente", response));
    }
}
