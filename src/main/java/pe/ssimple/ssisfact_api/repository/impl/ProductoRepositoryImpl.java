package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoCatalogoResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoItemResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoRequest;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoResponse;
import pe.ssimple.ssisfact_api.repository.ProductoRepository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductoRepositoryImpl implements ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ProductoResponse guardarProducto(ProductoRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_producto(?,?,?,?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> new ProductoResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getIdProducto(), // NULL: Crear, NOT NULL: Actualizar
                request.getEmpresaId(),
                request.getCategoriaNombre(),
                request.getCodigo(), // Código único dentro de la empresa
                request.getCodigoBarras(), // Código de barras
                request.getNombre(),
                request.getDescripcion(),
                request.getPrecio(),
                request.getCosto(),
                request.getStockMinimo(),
                request.getAfectoImpuesto(),
                request.getImagenUrl());
    }

    @Override
    public List<ProductoItemResponse> listarProductos(Long empresaId, String busqueda, int page, int size, int estado, int mostrarCosto) {

        return jdbcTemplate.query(
                "CALL sp_listar_productos(?,?,?,?,?,?)",
                (rs, rowNum) -> {
                    ProductoItemResponse item = new ProductoItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setEmpresaId(rs.getLong("empresa_id"));
                    item.setCategoriaId(rs.getLong("categoria_id"));
                    item.setCategoriaNombre(rs.getString("categoria_nombre"));
                    item.setCodigo(rs.getString("codigo"));
                    item.setCodigoBarras(rs.getString("codigo_barras"));
                    item.setNombre(rs.getString("nombre"));
                    item.setDescripcion(rs.getString("descripcion"));
                    item.setPrecio(rs.getBigDecimal("precio"));
                    item.setCosto(rs.getBigDecimal("costo")); // el SP ya devuelve NULL cuando mostrarCosto=0
                    item.setStockMinimo(rs.getInt("stock_minimo"));
                    item.setAfectoImpuesto(rs.getInt("afecto_impuesto"));
                    item.setStockTotal(rs.getInt("stock_total"));
                    item.setImagenUrl(rs.getString("imagen_url"));
                    item.setEstado(rs.getInt("estado"));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                    if (fa != null) item.setFechaActualizacion(fa.toLocalDateTime());
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, busqueda, page, size, estado, mostrarCosto);
    }

    @Override
    public List<ProductoCatalogoResponse> listarCatalogo(Long empresaId, String busqueda, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_productos(?,?,?,?,?,?)",
                (rs, rowNum) -> {
                    String codigoBarras = rs.getString("codigo_barras");
                    String codigo = (codigoBarras != null && !codigoBarras.isBlank())
                            ? codigoBarras
                            : rs.getString("codigo");

                    ProductoCatalogoResponse item = new ProductoCatalogoResponse();
                    item.setId(rs.getLong("id"));
                    item.setCategoriaNombre(rs.getString("categoria_nombre"));
                    item.setCodigo(codigo);
                    item.setNombre(rs.getString("nombre"));
                    item.setDescripcion(rs.getString("descripcion"));
                    item.setPrecio(rs.getBigDecimal("precio"));
                    item.setAfectoImpuesto(rs.getInt("afecto_impuesto"));
                    item.setStockTotal(rs.getInt("stock_total"));
                    item.setImagenUrl(rs.getString("imagen_url"));
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                // estado=1 (solo activos) y mostrarCosto=0 fijos: este endpoint es para vendedores/clientes
                empresaId, busqueda, page, size, 1, 0);
    }

    @Override
    public ProductoResponse activarProducto(Long productoId, Long empresaId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_activar_producto(?,?)",
                (rs, rowNum) -> new ProductoResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                productoId, empresaId);
    }

    @Override
    public ProductoResponse desactivarProducto(Long productoId, Long empresaId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_desactivar_producto(?,?)",
                (rs, rowNum) -> new ProductoResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                productoId, empresaId);
    }

    @Override
    public ProductoResponse eliminarProducto(Long productoId, Long empresaId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_eliminar_producto_definitivamente(?,?)",
                (rs, rowNum) -> new ProductoResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                productoId, empresaId);
    }
}
