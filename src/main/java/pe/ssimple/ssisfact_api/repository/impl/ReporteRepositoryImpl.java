package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;
import pe.ssimple.ssisfact_api.dto.Reporte.ClienteTopResponse;
import pe.ssimple.ssisfact_api.repository.ReporteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReporteRepositoryImpl implements ReporteRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> obtenerVentasRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo) {

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(total), 0) AS total_vendido, COUNT(*) AS cantidad_ventas " +
                "FROM ventas " +
                "WHERE empresa_id = ? AND estado = 'PAGADA' AND fecha >= ? AND fecha < ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
        agregarFiltroSucursal(sql, params, sucursalId, "");

        return jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    }

    @Override
    public Map<String, Object> obtenerPendientesCobroRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo) {

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(total), 0) AS monto, COUNT(*) AS cantidad " +
                "FROM ventas " +
                "WHERE empresa_id = ? AND estado = 'PENDIENTE' AND fecha >= ? AND fecha < ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
        agregarFiltroSucursal(sql, params, sucursalId, "");

        return jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    }

    @Override
    public List<ProductoMasVendidoResponse> obtenerProductosMasVendidosRango(
            Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo, int limite) {

        StringBuilder sql = new StringBuilder(
                "SELECT p.id AS producto_id, p.nombre, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS total " +
                "FROM detalle_venta dv " +
                "INNER JOIN ventas v ON v.id = dv.venta_id " +
                "INNER JOIN productos p ON p.id = dv.producto_id " +
                "WHERE v.empresa_id = ? AND v.estado = 'PAGADA' AND v.fecha >= ? AND v.fecha < ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
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
    public List<MetodoPagoResponse> obtenerMetodosPagoRango(Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo) {

        StringBuilder sql = new StringBuilder(
                "SELECT pg.metodo, SUM(pg.monto) AS total " +
                "FROM pagos pg " +
                "INNER JOIN ventas v ON v.id = pg.venta_id " +
                "WHERE v.empresa_id = ? AND v.estado = 'PAGADA' AND v.fecha >= ? AND v.fecha < ? ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
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
    public List<VentaSucursalResponse> obtenerVentasPorSucursalRango(Long empresaId, LocalDate desde, LocalDate hastaExclusivo) {

        String sql =
                "SELECT s.id AS sucursal_id, s.nombre AS sucursal_nombre, " +
                "COALESCE(SUM(v.total), 0) AS total, COUNT(v.id) AS cantidad " +
                "FROM sucursales s " +
                "LEFT JOIN ventas v ON v.sucursal_id = s.id AND v.estado = 'PAGADA' AND v.fecha >= ? AND v.fecha < ? " +
                "WHERE s.empresa_id = ? AND s.estado = 1 " +
                "GROUP BY s.id, s.nombre " +
                "ORDER BY total DESC";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new VentaSucursalResponse(
                        rs.getLong("sucursal_id"),
                        rs.getString("sucursal_nombre"),
                        rs.getBigDecimal("total"),
                        rs.getInt("cantidad")),
                desde, hastaExclusivo, empresaId);
    }

    @Override
    public List<ClienteTopResponse> obtenerTopClientesRango(
            Long empresaId, Long sucursalId, LocalDate desde, LocalDate hastaExclusivo, int limite) {

        StringBuilder sql = new StringBuilder(
                "SELECT cl.id AS cliente_id, cl.nombre, COUNT(v.id) AS cantidad_compras, SUM(v.total) AS total_comprado " +
                "FROM ventas v " +
                "INNER JOIN clientes cl ON cl.id = v.cliente_id " +
                "WHERE v.empresa_id = ? AND v.estado = 'PAGADA' AND v.fecha >= ? AND v.fecha < ? " +
                "AND cl.numero_documento != '00000000' ");

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(desde);
        params.add(hastaExclusivo);
        agregarFiltroSucursal(sql, params, sucursalId, "v.");

        sql.append("GROUP BY cl.id, cl.nombre ORDER BY total_comprado DESC LIMIT ").append(limite);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new ClienteTopResponse(
                        rs.getLong("cliente_id"),
                        rs.getString("nombre"),
                        rs.getInt("cantidad_compras"),
                        rs.getBigDecimal("total_comprado"),
                        0), // porcentajeRelativo se calcula en el service
                params.toArray());
    }

    private void agregarFiltroSucursal(StringBuilder sql, List<Object> params, Long sucursalId, String alias) {
        if (sucursalId != null) {
            sql.append("AND ").append(alias).append("sucursal_id = ? ");
            params.add(sucursalId);
        }
    }
}
