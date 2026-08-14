package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Caja.CajaActualResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaItemResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaResumenResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Caja.PagoMetodoResumen;
import pe.ssimple.ssisfact_api.dto.Caja.ProductoVendidoResumen;
import pe.ssimple.ssisfact_api.repository.CajaRepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CajaRepositoryImpl implements CajaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CajaSaveResponse abrirCaja(Long sucursalId, Long usuarioId, BigDecimal montoInicial) {

        return jdbcTemplate.queryForObject(
                "CALL sp_abrir_caja(?,?,?)",
                (rs, rowNum) -> new CajaSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                sucursalId, usuarioId, montoInicial);
    }

    @Override
    public CajaCerrarResponse cerrarCaja(Long cajaId, Long usuarioId, BigDecimal montoFinal) {

        return jdbcTemplate.queryForObject(
                "CALL sp_cerrar_caja(?,?,?)",
                (rs, rowNum) -> new CajaCerrarResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id"),
                        rs.getBigDecimal("monto_inicial"),
                        rs.getBigDecimal("monto_final"),
                        rs.getBigDecimal("monto_esperado"),
                        rs.getBigDecimal("diferencia"),
                        null, null),
                cajaId, usuarioId, montoFinal);
    }

    @Override
    public CajaActualResponse obtenerCajaAbierta(Long usuarioId) {

        List<CajaActualResponse> resultado = jdbcTemplate.query(
                "CALL sp_obtener_caja_abierta(?)",
                (rs, rowNum) -> mapCajaActual(rs),
                usuarioId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<CajaItemResponse> listarCajas(Long empresaId, Long sucursalId, String estado, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_cajas(?,?,?,?,?)",
                (rs, rowNum) -> {
                    CajaItemResponse item = new CajaItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setSucursalId(rs.getLong("sucursal_id"));
                    item.setSucursalNombre(rs.getString("sucursal_nombre"));
                    item.setUsuarioId(rs.getLong("usuario_id"));
                    item.setUsuarioNombre(rs.getString("usuario_nombre"));
                    item.setMontoInicial(rs.getBigDecimal("monto_inicial"));
                    item.setMontoFinal(rs.getBigDecimal("monto_final"));
                    Timestamp fa = rs.getTimestamp("fecha_apertura");
                    if (fa != null) item.setFechaApertura(fa.toLocalDateTime());
                    Timestamp fc = rs.getTimestamp("fecha_cierre");
                    if (fc != null) item.setFechaCierre(fc.toLocalDateTime());
                    item.setEstado(rs.getString("estado"));
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, sucursalId, estado, page, size);
    }

    @Override
    public CajaResumenResponse obtenerResumenCabecera(Long cajaId, Long empresaId) {

        List<CajaResumenResponse> resultado = jdbcTemplate.query(
                "CALL sp_resumen_caja(?,?)",
                (rs, rowNum) -> {
                    CajaResumenResponse resumen = new CajaResumenResponse();
                    resumen.setId(rs.getLong("id"));
                    resumen.setSucursalId(rs.getLong("sucursal_id"));
                    resumen.setSucursalNombre(rs.getString("sucursal_nombre"));
                    resumen.setUsuarioId(rs.getLong("usuario_id"));
                    resumen.setUsuarioNombre(rs.getString("usuario_nombre"));
                    resumen.setMontoInicial(rs.getBigDecimal("monto_inicial"));
                    resumen.setMontoFinal(rs.getBigDecimal("monto_final"));
                    Timestamp fa = rs.getTimestamp("fecha_apertura");
                    if (fa != null) resumen.setFechaApertura(fa.toLocalDateTime());
                    Timestamp fc = rs.getTimestamp("fecha_cierre");
                    if (fc != null) resumen.setFechaCierre(fc.toLocalDateTime());
                    resumen.setEstado(rs.getString("estado"));
                    resumen.setCantidadVentas(rs.getInt("cantidad_ventas"));
                    resumen.setTotalVendido(rs.getBigDecimal("total_vendido"));
                    return resumen;
                },
                cajaId, empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<PagoMetodoResumen> obtenerPagosPorMetodo(Long cajaId) {

        return jdbcTemplate.query(
                "SELECT p.metodo, SUM(p.monto) AS total, COUNT(*) AS cantidad " +
                        "FROM pagos p INNER JOIN ventas v ON v.id = p.venta_id " +
                        "WHERE v.caja_id = ? AND v.estado <> 'ANULADA' " +
                        "GROUP BY p.metodo ORDER BY p.metodo",
                (rs, rowNum) -> new PagoMetodoResumen(
                        rs.getString("metodo"),
                        rs.getBigDecimal("total"),
                        rs.getInt("cantidad")),
                cajaId);
    }

    @Override
    public List<ProductoVendidoResumen> obtenerProductosVendidosCaja(Long cajaId) {

        return jdbcTemplate.query(
                "SELECT dv.producto_id, p.nombre AS producto_nombre, " +
                        "SUM(dv.cantidad) AS cantidad_vendida, SUM(dv.subtotal - dv.descuento) AS total_vendido " +
                        "FROM detalle_venta dv " +
                        "INNER JOIN ventas v ON v.id = dv.venta_id " +
                        "INNER JOIN productos p ON p.id = dv.producto_id " +
                        "WHERE v.caja_id = ? AND v.estado <> 'ANULADA' " +
                        "GROUP BY dv.producto_id, p.nombre " +
                        "ORDER BY cantidad_vendida DESC",
                (rs, rowNum) -> {
                    ProductoVendidoResumen item = new ProductoVendidoResumen();
                    item.setProductoId(rs.getLong("producto_id"));
                    item.setProductoNombre(rs.getString("producto_nombre"));
                    item.setCantidadVendida(rs.getBigDecimal("cantidad_vendida"));
                    item.setTotalVendido(rs.getBigDecimal("total_vendido"));
                    return item;
                },
                cajaId);
    }

    private CajaActualResponse mapCajaActual(java.sql.ResultSet rs) throws java.sql.SQLException {
        CajaActualResponse item = new CajaActualResponse();
        item.setId(rs.getLong("id"));
        item.setSucursalId(rs.getLong("sucursal_id"));
        item.setSucursalNombre(rs.getString("sucursal_nombre"));
        item.setUsuarioId(rs.getLong("usuario_id"));
        item.setMontoInicial(rs.getBigDecimal("monto_inicial"));
        item.setMontoFinal(rs.getBigDecimal("monto_final"));
        Timestamp fa = rs.getTimestamp("fecha_apertura");
        if (fa != null) item.setFechaApertura(fa.toLocalDateTime());
        Timestamp fc = rs.getTimestamp("fecha_cierre");
        if (fc != null) item.setFechaCierre(fc.toLocalDateTime());
        item.setEstado(rs.getString("estado"));
        return item;
    }
}
