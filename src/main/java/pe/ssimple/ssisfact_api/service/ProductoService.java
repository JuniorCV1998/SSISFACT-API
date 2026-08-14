package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoListResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoRequest;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoResponse;

public interface ProductoService {
    ProductoResponse guardarProducto(ProductoRequest request);
    ProductoListResponse listarProductos(Long empresaId, String busqueda, int page, int size, int estado, int mostrarCosto, Long sucursalId);
    ProductoCatalogoListResponse listarCatalogo(Long empresaId, String busqueda, int page, int size, Long sucursalId);
    ProductoResponse activarProducto(Long productoId, Long empresaId);
    ProductoResponse desactivarProducto(Long productoId, Long empresaId);
    ProductoResponse eliminarProducto(Long productoId, Long empresaId);
}
