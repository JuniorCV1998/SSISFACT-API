package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Venta.ClienteVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.ComprobanteNumeroResult;
import pe.ssimple.ssisfact_api.dto.Venta.IniciarVentaResult;
import pe.ssimple.ssisfact_api.dto.Venta.InventarioLockInfo;
import pe.ssimple.ssisfact_api.dto.Venta.PagoResponse;
import pe.ssimple.ssisfact_api.dto.Venta.ProductoVentaInfo;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListItemResponse;
import pe.ssimple.ssisfact_api.repository.VentaRepository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VentaRepositoryImpl implements VentaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public IniciarVentaResult iniciarVenta(Long empresaId, Long sucursalId, Long clienteId, Long usuarioId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_iniciar_venta(?,?,?,?)",
                (rs, rowNum) -> new IniciarVentaResult(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id"),
                        rs.getLong("caja_id"),
                        rs.getLong("cliente_id")),
                empresaId, sucursalId, clienteId, usuarioId);
    }

    @Override
    public ClienteVentaInfo obtenerClienteInfo(Long clienteId) {

        List<ClienteVentaInfo> resultado = jdbcTemplate.query(
                "SELECT nombre, numero_documento FROM clientes WHERE id = ?",
                (rs, rowNum) -> new ClienteVentaInfo(rs.getString("nombre"), rs.getString("numero_documento")),
                clienteId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public ProductoVentaInfo obtenerProductoParaVenta(Long productoId, Long empresaId) {

        List<ProductoVentaInfo> resultado = jdbcTemplate.query(
                "SELECT id, nombre, precio FROM productos WHERE id = ? AND empresa_id = ? AND estado = 1",
                (rs, rowNum) -> new ProductoVentaInfo(
                        rs.getLong("id"), rs.getString("nombre"), rs.getBigDecimal("precio")),
                productoId, empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public InventarioLockInfo bloquearInventario(Long productoId, Long sucursalId) {

        List<InventarioLockInfo> resultado = jdbcTemplate.query(
                "SELECT id, stock FROM inventarios WHERE producto_id = ? AND sucursal_id = ? FOR UPDATE",
                (rs, rowNum) -> new InventarioLockInfo(rs.getLong("id"), rs.getInt("stock")),
                productoId, sucursalId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public void insertarDetalleVenta(Long ventaId, Long productoId, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal descuento) {

        jdbcTemplate.update(
                "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal) VALUES (?,?,?,?,?,?)",
                ventaId, productoId, cantidad, precioUnitario, descuento, subtotal);
    }

    @Override
    public void actualizarStock(Long inventarioId, BigDecimal nuevoStock) {

        jdbcTemplate.update(
                "UPDATE inventarios SET stock = ?, fecha_actualizacion = NOW() WHERE id = ?",
                nuevoStock, inventarioId);
    }

    @Override
    public void insertarMovimientoStock(Long productoId, Long sucursalId, String tipo, BigDecimal cantidad, String motivo, Long referenciaId) {

        jdbcTemplate.update(
                "INSERT INTO movimientos_stock (producto_id, sucursal_id, tipo, cantidad, motivo, referencia_id, fecha) " +
                        "VALUES (?,?,?,?,?,?,NOW())",
                productoId, sucursalId, tipo, cantidad, motivo, referenciaId);
    }

    @Override
    public ComprobanteNumeroResult generarNumeroComprobante(Long empresaId, String tipo) {

        return jdbcTemplate.queryForObject(
                "CALL sp_generar_numero_comprobante(?,?)",
                (rs, rowNum) -> new ComprobanteNumeroResult(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getString("serie"),
                        (Integer) rs.getObject("numero")),
                empresaId, tipo);
    }

    @Override
    public Long insertarComprobante(Long empresaId, Long ventaId, String tipo, String serie, Integer numero,
                                     String clienteNombre, String clienteDocumento,
                                     BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comprobantes (empresa_id, venta_id, tipo, serie, numero, cliente_nombre, " +
                            "cliente_documento, subtotal, descuento, impuestos, total, estado_sunat, fecha_emision) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,'PENDIENTE',NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, empresaId);
            ps.setLong(2, ventaId);
            ps.setString(3, tipo);
            ps.setString(4, serie);
            ps.setInt(5, numero);
            ps.setString(6, clienteNombre);
            ps.setString(7, clienteDocumento);
            ps.setBigDecimal(8, subtotal);
            ps.setBigDecimal(9, descuento);
            ps.setBigDecimal(10, impuestos);
            ps.setBigDecimal(11, total);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("No se pudo obtener el ID generado del comprobante");
        }
        return key.longValue();
    }

    @Override
    public void insertarPago(Long ventaId, String metodo, BigDecimal monto, String referencia) {

        jdbcTemplate.update(
                "INSERT INTO pagos (venta_id, metodo, monto, referencia, fecha) VALUES (?,?,?,?,NOW())",
                ventaId, metodo, monto, referencia);
    }

    @Override
    public void actualizarTotalesVenta(Long ventaId, BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total, String estado) {

        jdbcTemplate.update(
                "UPDATE ventas SET subtotal = ?, descuento = ?, impuestos = ?, total = ?, estado = ?, fecha_actualizacion = NOW() WHERE id = ?",
                subtotal, descuento, impuestos, total, estado, ventaId);
    }

    @Override
    public List<VentaListItemResponse> listarVentas(Long empresaId, Long sucursalId, Long clienteId, Long cajaId, String estado,
                                                      LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_ventas(?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> {
                    VentaListItemResponse item = new VentaListItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setSucursalId(rs.getLong("sucursal_id"));
                    item.setSucursalNombre(rs.getString("sucursal_nombre"));
                    item.setClienteId(rs.getLong("cliente_id"));
                    item.setClienteNombre(rs.getString("cliente_nombre"));
                    item.setClienteDocumento(rs.getString("cliente_documento"));
                    item.setUsuarioId(rs.getLong("usuario_id"));
                    item.setUsuarioNombre(rs.getString("usuario_nombre"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setDescuento(rs.getBigDecimal("descuento"));
                    item.setImpuestos(rs.getBigDecimal("impuestos"));
                    item.setTotal(rs.getBigDecimal("total"));
                    item.setEstado(rs.getString("estado"));
                    Timestamp fecha = rs.getTimestamp("fecha");
                    if (fecha != null) item.setFecha(fecha.toLocalDateTime());
                    item.setComprobanteTipo(rs.getString("comprobante_tipo"));
                    item.setComprobanteSerie(rs.getString("comprobante_serie"));
                    item.setComprobanteNumero((Integer) rs.getObject("comprobante_numero"));
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, sucursalId, clienteId, cajaId, estado, fechaDesde, fechaHasta, page, size);
    }

    @Override
    public VentaDetalleResponse obtenerVentaCabecera(Long ventaId, Long empresaId) {

        List<VentaDetalleResponse> resultado = jdbcTemplate.query(
                "CALL sp_obtener_venta_detalle(?,?)",
                (rs, rowNum) -> {
                    VentaDetalleResponse cabecera = new VentaDetalleResponse();
                    cabecera.setId(rs.getLong("id"));
                    cabecera.setEmpresaId(rs.getLong("empresa_id"));
                    cabecera.setSucursalId(rs.getLong("sucursal_id"));
                    cabecera.setSucursalNombre(rs.getString("sucursal_nombre"));
                    cabecera.setClienteId(rs.getLong("cliente_id"));
                    cabecera.setClienteNombre(rs.getString("cliente_nombre"));
                    cabecera.setClienteTipoDocumento(rs.getString("cliente_tipo_documento"));
                    cabecera.setClienteDocumento(rs.getString("cliente_documento"));
                    cabecera.setUsuarioId(rs.getLong("usuario_id"));
                    cabecera.setUsuarioNombre(rs.getString("usuario_nombre"));
                    cabecera.setCajaId(rs.getLong("caja_id"));
                    cabecera.setSubtotal(rs.getBigDecimal("subtotal"));
                    cabecera.setDescuento(rs.getBigDecimal("descuento"));
                    cabecera.setImpuestos(rs.getBigDecimal("impuestos"));
                    cabecera.setTotal(rs.getBigDecimal("total"));
                    cabecera.setEstado(rs.getString("estado"));
                    Timestamp fecha = rs.getTimestamp("fecha");
                    if (fecha != null) cabecera.setFecha(fecha.toLocalDateTime());
                    long comprobanteId = rs.getLong("comprobante_id");
                    if (!rs.wasNull()) cabecera.setComprobanteId(comprobanteId);
                    cabecera.setComprobanteTipo(rs.getString("comprobante_tipo"));
                    cabecera.setComprobanteSerie(rs.getString("comprobante_serie"));
                    cabecera.setComprobanteNumero((Integer) rs.getObject("comprobante_numero"));
                    return cabecera;
                },
                ventaId, empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<VentaItemDetalleResponse> listarItemsVenta(Long ventaId) {

        return jdbcTemplate.query(
                "SELECT dv.id, dv.producto_id, p.nombre AS producto_nombre, dv.cantidad, dv.precio_unitario, dv.descuento, dv.subtotal " +
                        "FROM detalle_venta dv " +
                        "INNER JOIN productos p ON p.id = dv.producto_id " +
                        "WHERE dv.venta_id = ? " +
                        "ORDER BY dv.id ASC",
                (rs, rowNum) -> {
                    VentaItemDetalleResponse item = new VentaItemDetalleResponse();
                    item.setId(rs.getLong("id"));
                    item.setProductoId(rs.getLong("producto_id"));
                    item.setProductoNombre(rs.getString("producto_nombre"));
                    item.setCantidad(rs.getBigDecimal("cantidad"));
                    item.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    item.setDescuento(rs.getBigDecimal("descuento"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    return item;
                },
                ventaId);
    }

    @Override
    public List<PagoResponse> listarPagosVenta(Long ventaId) {

        return jdbcTemplate.query(
                "SELECT id, metodo, monto, referencia, fecha FROM pagos WHERE venta_id = ? ORDER BY id ASC",
                (rs, rowNum) -> {
                    PagoResponse pago = new PagoResponse();
                    pago.setId(rs.getLong("id"));
                    pago.setMetodo(rs.getString("metodo"));
                    pago.setMonto(rs.getBigDecimal("monto"));
                    pago.setReferencia(rs.getString("referencia"));
                    Timestamp fecha = rs.getTimestamp("fecha");
                    if (fecha != null) pago.setFecha(fecha.toLocalDateTime());
                    return pago;
                },
                ventaId);
    }

    @Override
    public String obtenerEstadoVenta(Long ventaId, Long empresaId) {

        try {
            return jdbcTemplate.queryForObject(
                    "SELECT estado FROM ventas WHERE id = ? AND empresa_id = ?",
                    String.class,
                    ventaId, empresaId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String obtenerEstadoCajaVenta(Long ventaId) {

        return jdbcTemplate.queryForObject(
                "SELECT c.estado FROM cajas c INNER JOIN ventas v ON v.caja_id = c.id WHERE v.id = ?",
                String.class, ventaId);
    }

    @Override
    public Long obtenerSucursalVenta(Long ventaId) {

        return jdbcTemplate.queryForObject(
                "SELECT sucursal_id FROM ventas WHERE id = ?", Long.class, ventaId);
    }

    @Override
    public List<VentaItemDetalleResponse> listarProductoCantidadParaAnular(Long ventaId) {

        return jdbcTemplate.query(
                "SELECT producto_id, cantidad FROM detalle_venta WHERE venta_id = ?",
                (rs, rowNum) -> {
                    VentaItemDetalleResponse item = new VentaItemDetalleResponse();
                    item.setProductoId(rs.getLong("producto_id"));
                    item.setCantidad(rs.getBigDecimal("cantidad"));
                    return item;
                },
                ventaId);
    }

    @Override
    public void marcarVentaAnulada(Long ventaId) {

        jdbcTemplate.update(
                "UPDATE ventas SET estado = 'ANULADA', fecha_actualizacion = NOW() WHERE id = ?",
                ventaId);
    }
}
