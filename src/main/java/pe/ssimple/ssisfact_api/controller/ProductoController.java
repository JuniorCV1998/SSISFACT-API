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
import pe.ssimple.ssisfact_api.util.SucursalAccessGuard;

@RestController
@RequestMapping("/producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final SucursalAccessGuard sucursalAccessGuard;

    @PostMapping("/guardar")
    public ResponseEntity<ApiResponse<ProductoResponse>> guardarProducto(
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        request.setEmpresaId(user.getEmpresaId());

        // sucursalId es opcional en ProductoRequest (solo se manda si se quiere
        // cargar stock inicial); si no viene, no hay sucursal que validar.
        if (request.getSucursalId() != null) {
            sucursalAccessGuard.validar(user, request.getSucursalId());
        }

        return ResponseBuilder.build(productoService.guardarProducto(request), "OK");
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<ProductoListResponse>> listarProductos(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "1") int estado,
            @RequestParam(required = false) Long sucursalId) {

        boolean esAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        ProductoListResponse result = productoService.listarProductos(
                user.getEmpresaId(), busqueda, page, size, estado, esAdmin ? 1 : 0, sucursalId);

        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos correctamente", result));
    }

    // Para vendedores/clientes: campos reducidos, sin costo, solo productos con estado=1.
    // sucursalId opcional: si se manda (Punto de Venta), el stock devuelto es el de ESA
    // sucursal únicamente; si no se manda, es la suma de todas las sucursales.
    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<ProductoCatalogoListResponse>> listarCatalogo(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sucursalId) {

        ProductoCatalogoListResponse result = productoService.listarCatalogo(
                user.getEmpresaId(), busqueda, page, size, sucursalId);

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
