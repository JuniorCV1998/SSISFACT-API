package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoRequest;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.ProductoService;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

@RestController
@RequestMapping("/producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("/guardar")
    public ResponseEntity<ApiResponse<ProductoResponse>> guardarProducto(
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        request.setEmpresaId(user.getEmpresaId());

        return ResponseBuilder.build(productoService.guardarProducto(request), "OK");
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<ProductoListResponse>> listarProductos(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "1") int estado) {

        boolean esAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        ProductoListResponse result = productoService.listarProductos(
                user.getEmpresaId(), busqueda, page, size, estado, esAdmin ? 1 : 0);

        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos correctamente", result));
    }

    // Para vendedores/clientes: campos reducidos, sin costo, solo productos con estado=1
    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<ProductoCatalogoListResponse>> listarCatalogo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductoCatalogoListResponse result = productoService.listarCatalogo(
                user.getEmpresaId(), busqueda, page, size);

        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos correctamente", result));
    }

    @PostMapping("/activar/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> activarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseBuilder.build(productoService.activarProducto(id, user.getEmpresaId()), "OK");
    }

    @PostMapping("/desactivar/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> desactivarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseBuilder.build(productoService.desactivarProducto(id, user.getEmpresaId()), "OK");
    }

    @PostMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> eliminarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseBuilder.build(productoService.eliminarProducto(id, user.getEmpresaId()), "OK");
    }
}
