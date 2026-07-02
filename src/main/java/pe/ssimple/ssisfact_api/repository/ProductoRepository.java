package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoItemResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoRequest;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoResponse;

import java.util.List;

public interface ProductoRepository {
    ProductoResponse guardarProducto(ProductoRequest request);
    List<ProductoItemResponse> listarProductos(Long empresaId, String busqueda, int page, int size, int estado, int mostrarCosto);
    List<ProductoCatalogoResponse> listarCatalogo(Long empresaId, String busqueda, int page, int size);
    ProductoResponse activarProducto(Long productoId, Long empresaId);
    ProductoResponse desactivarProducto(Long productoId, Long empresaId);
    ProductoResponse eliminarProducto(Long productoId, Long empresaId);
}
