package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteListResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSimpleResponse;
import pe.ssimple.ssisfact_api.service.ClienteService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

import java.util.List;

@RestController
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/guardar")
    public ResponseEntity<ApiResponse<ClienteSaveResponse>> guardarCliente(
            @Valid @RequestBody ClienteRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        request.setEmpresaId(user.getEmpresaId());

        return ResponseBuilder.build(clienteService.guardarCliente(request), "OK");
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<ClienteListResponse>> listarClientes(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "-1") int estado,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        ClienteListResponse result = clienteService.listarClientes(user.getEmpresaId(), busqueda, estado, page, size);

        return ResponseEntity.ok(ApiResponse.success("Clientes obtenidos correctamente", result));
    }

    // Para el selector del flujo de venta: solo id, nombre y documento, solo activos
    @GetMapping("/seleccionar")
    public ResponseEntity<ApiResponse<List<ClienteSimpleResponse>>> listarClientesParaSeleccion(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda) {

        List<ClienteSimpleResponse> result = clienteService.listarClientesParaSeleccion(user.getEmpresaId(), busqueda);

        return ResponseEntity.ok(ApiResponse.success("Clientes obtenidos correctamente", result));
    }

    @PostMapping("/desactivar/{id}")
    public ResponseEntity<ApiResponse<ClienteSaveResponse>> desactivarCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseBuilder.build(clienteService.desactivarCliente(id, user.getEmpresaId()), "OK");
    }
}
