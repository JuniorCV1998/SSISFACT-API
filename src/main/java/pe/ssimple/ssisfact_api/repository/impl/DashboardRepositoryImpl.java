package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.TransaccionRecienteResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;
import pe.ssimple.ssisfact_api.repository.DashboardRepository;
import pe.ssimple.ssisfact_api.util.ComprobanteCodigoUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> obtenerVentasHoyVsAyer(Long empresaId, Long sucursalId) {

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                "COALESCE(SUM(CASE WHEN DATE(fecha) = CURDATE() THEN total END), 0) AS total_hoy, " +
                "COALESCE(SUM(CASE WHEN DATE(fecha) = CURDATE() - INTERVAL 1 DAY THEN total END), 0) AS total_ayer, " +
                "COUNT(CASE WHEN DATE(fecha) = CURDATE() THEN 1 END) AS cantidad_hoy, " +
                "COUNT(CASE WHEN DATE(fecha) = CURDATE() - INTERVAL 1 DAY THEN 1 END) AS cantidad_ayer " +
                "FROM ventas " +
                "WHERE empresa_id = ? AND estado = 'PAGADA' AND fecha >= CURDATE() - INTERVAL 1 DAY ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        agregarFiltroSucursal(sql, params, sucursalId, "");

        return jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    }

    @Override
    public Map<String, Object> obtenerClientesHoyVsAyer(Long empresaId, Long sucursalId) {

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                "COUNT(DISTINCT CASE WHEN DATE(fecha) = CURDATE() THEN cliente_id END) AS clientes_hoy, " +
                "COUNT(DISTINCT CASE WHEN DATE(fecha) = CURDATE() - INTERVAL 1 DAY THEN cliente_id END) AS clientes_ayer " +
                "FROM ventas " +
                "WHERE empresa_id = ? AND estado = 'PAGADA' AND fecha >= CURDATE() - INTERVAL 1 DAY ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        agregarFiltroSucursal(sql, params, sucursalId, "");

        return jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    }

    @Override
    public Map<String, Object> obtenerProductosEnStock(Long empresaId, Long sucursalId) {

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(stock_total), 0) AS total_unidades, " +
                "COALESCE(SUM(CASE WHEN stock_total <= stock_minimo THEN 1 ELSE 0 END), 0) AS stock_bajo " +
                "FROM ( " +
                "  SELECT p.id, COALESCE(p.stock_minimo, 0) AS stock_minimo, " +
                "    COALESCE((SELECT SUM(i.stock) FROM inventarios i WHERE i.producto_id = p.id ");

        List<Object> params = new ArrayList<>();
        if (sucursalId != null) {
            sql.append("AND i.sucursal_id = ? ");
            params.add(sucursalId);
        }

        sql.append("), 0) AS stock_total " +
                "  FROM productos p " +
                "  WHERE p.empresa_id = ? AND p.estado = 1 " +
                ") t");
        params.add(empresaId);

        return jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> obtenerVentasPorDia(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo) {

        StringBuilder sql = new StringBuilder(
                "SELECT DATE(fecha) AS dia, SUM(total) AS total " +
                "FROM ventas " +
                "WHERE empresa_id = ? AND estado = 'PAGADA' AND fecha >= ? AND fecha < ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
        agregarFiltroSucursal(sql, params, sucursalId, "");

        sql.append("GROUP BY DATE(fecha)");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<ProductoMasVendidoResponse> obtenerProductosMasVendidosHoy(Long empresaId, Long sucursalId, int limite) {

        StringBuilder sql = new StringBuilder(
                "SELECT p.id AS producto_id, p.nombre, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS total " +
                "FROM detalle_venta dv " +
                "INNER JOIN ventas v ON v.id = dv.venta_id " +
                "INNER JOIN productos p ON p.id = dv.producto_id " +
                "WHERE v.empresa_id = ? AND v.estado = 'PAGADA' AND DATE(v.fecha) = CURDATE() ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        agregarFiltroSucursal(sql, params, sucursalId, "v.");

        sql.append("GROUP BY p.id, p.nombre ORDER BY unidades DESC LIMIT ").append(limite);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new ProductoMasVendidoResponse(
                        rs.getLong("producto_id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("unidades"),
                        rs.getBigDecimal("total"),
                        0), // porcentajeRelativo se calcula en el service
                params.toArray());
    }

    @Override
    public List<MetodoPagoResponse> obtenerMetodosPagoHoy(Long empresaId, Long sucursalId) {

        StringBuilder sql = new StringBuilder(
                "SELECT pg.metodo, SUM(pg.monto) AS total " +
                "FROM pagos pg " +
                "INNER JOIN ventas v ON v.id = pg.venta_id " +
                "WHERE v.empresa_id = ? AND v.estado = 'PAGADA' AND DATE(v.fecha) = CURDATE() ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        agregarFiltroSucursal(sql, params, sucursalId, "v.");

        sql.append("GROUP BY pg.metodo ORDER BY total DESC");

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new MetodoPagoResponse(
                        rs.getString("metodo"),
                        rs.getBigDecimal("total"),
                        0), // porcentaje se calcula en el service
                params.toArray());
    }

    @Override
    public List<VentaSucursalResponse> obtenerVentasPorSucursalHoy(Long empresaId) {

        String sql =
                "SELECT s.id AS sucursal_id, s.nombre AS sucursal_nombre, " +
                "COALESCE(SUM(CASE WHEN v.estado = 'PAGADA' AND DATE(v.fecha) = CURDATE() THEN v.total END), 0) AS total, " +
                "COUNT(CASE WHEN v.estado = 'PAGADA' AND DATE(v.fecha) = CURDATE() THEN v.id END) AS cantidad " +
                "FROM sucursales s " +
                "LEFT JOIN ventas v ON v.sucursal_id = s.id " +
                "WHERE s.empresa_id = ? AND s.estado = 1 " +
                "GROUP BY s.id, s.nombre " +
                "ORDER BY total DESC";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new VentaSucursalResponse(
                        rs.getLong("sucursal_id"),
                        rs.getString("sucursal_nombre"),
                        rs.getBigDecimal("total"),
                        rs.getInt("cantidad")),
                empresaId);
    }

    @Override
    public List<TransaccionRecienteResponse> obtenerTransaccionesRecientes(Long empresaId, Long sucursalId, int limite) {

        StringBuilder sql = new StringBuilder(
                "SELECT v.id AS venta_id, c.tipo AS comprobante_tipo, c.serie, c.numero, " +
                "cl.nombre AS cliente_nombre, v.total, v.estado, v.fecha " +
                "FROM ventas v " +
                "LEFT JOIN comprobantes c ON c.venta_id = v.id " +
                "LEFT JOIN clientes cl ON cl.id = v.cliente_id " +
                "WHERE v.empresa_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        agregarFiltroSucursal(sql, params, sucursalId, "v.");

        sql.append("ORDER BY v.fecha DESC LIMIT ").append(limite);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> {
                    Timestamp fecha = rs.getTimestamp("fecha");
                    return new TransaccionRecienteResponse(
                            rs.getLong("venta_id"),
                            rs.getString("comprobante_tipo"),
                            ComprobanteCodigoUtil.formatear(rs.getString("serie"), (Integer) rs.getObject("numero")),
                            rs.getString("cliente_nombre"),
                            rs.getBigDecimal("total"),
                            rs.getString("estado"),
                            fecha != null ? fecha.toLocalDateTime() : null);
                },
                params.toArray());
    }

    private void agregarFiltroSucursal(StringBuilder sql, List<Object> params, Long sucursalId, String alias) {
        if (sucursalId != null) {
            sql.append("AND ").append(alias).append("sucursal_id = ? ");
            params.add(sucursalId);
        }
    }
}
