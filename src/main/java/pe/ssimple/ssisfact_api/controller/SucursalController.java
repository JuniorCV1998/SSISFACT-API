package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalListResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalRequest;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalSaveResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.SucursalService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/sucursal")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping("/guardar")
    public ResponseEntity<ApiResponse<SucursalSaveResponse>> guardarSucursal(
            @Valid @RequestBody SucursalRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        request.setEmpresaId(user.getEmpresaId());

        return ResponseBuilder.build(sucursalService.guardarSucursal(request), "OK");
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<SucursalListResponse>> listarSucursales(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "1") int estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        SucursalListResponse result = sucursalService.listarSucursales(
                user.getEmpresaId(), busqueda, estado, page, size);

        return ResponseEntity.ok(ApiResponse.success("Sucursales obtenidas correctamente", result));
    }
}
