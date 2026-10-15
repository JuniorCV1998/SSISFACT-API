package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.PagoDigital.NotificacionPagoDigitalResponse;
import pe.ssimple.ssisfact_api.dto.PagoDigital.PagoDigitalConfigResponse;
import pe.ssimple.ssisfact_api.repository.PagoDigitalRepository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PagoDigitalRepositoryImpl implements PagoDigitalRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void habilitarToken(Long empresaId, String tokenHash) {
        jdbcTemplate.update(
                "INSERT INTO configuracion_empresa (empresa_id, pago_digital_webhook_estado, pago_digital_webhook_token_hash) " +
                        "VALUES (?, 'S', ?) " +
                        "ON DUPLICATE KEY UPDATE pago_digital_webhook_estado = 'S', pago_digital_webhook_token_hash = VALUES(pago_digital_webhook_token_hash)",
                empresaId, tokenHash);
    }

    @Override
    public void deshabilitar(Long empresaId) {
        jdbcTemplate.update(
                "UPDATE configuracion_empresa SET pago_digital_webhook_estado = 'N' WHERE empresa_id = ?",
                empresaId);
    }

    @Override
    public PagoDigitalConfigResponse obtenerConfig(Long empresaId) {

        List<PagoDigitalConfigResponse> resultado = jdbcTemplate.query(
                "SELECT pago_digital_webhook_estado, pago_digital_webhook_token_hash FROM configuracion_empresa WHERE empresa_id = ?",
                (rs, rowNum) -> new PagoDigitalConfigResponse(
                        "S".equals(rs.getString("pago_digital_webhook_estado")),
                        rs.getString("pago_digital_webhook_token_hash") != null,
                        null),
                empresaId);

        // Sin fila de configuración todavía (no debería pasar si la empresa ya existe,
        // pero se cubre de forma defensiva): deshabilitado y sin token.
        return resultado.isEmpty() ? new PagoDigitalConfigResponse(false, false, null) : resultado.get(0);
    }

    @Override
    public Long buscarEmpresaPorTokenHash(String tokenHash) {

        List<Long> resultado = jdbcTemplate.query(
                "SELECT empresa_id FROM configuracion_empresa WHERE pago_digital_webhook_token_hash = ? AND pago_digital_webhook_estado = 'S'",
                (rs, rowNum) -> rs.getLong("empresa_id"),
                tokenHash);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public Long insertarNotificacion(Long empresaId, String metodo, BigDecimal monto, String remitente, String textoOriginal) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO pago_digital_notificaciones (empresa_id, metodo, monto, remitente, texto_original, fecha_recibido, estado) " +
                            "VALUES (?,?,?,?,?,NOW(),'SIN_MATCH')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, empresaId);
            ps.setString(2, metodo);
            ps.setBigDecimal(3, monto);
            ps.setString(4, remitente);
            ps.setString(5, textoOriginal);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("No se pudo obtener el ID generado de la notificación de pago digital");
        }
        return key.longValue();
    }

    @Override
    public void confirmarNotificacion(Long notificacionId, Long ventaId, String estado) {
        jdbcTemplate.update(
                "UPDATE pago_digital_notificaciones SET estado = ?, venta_id = ?, fecha_asociacion = NOW() WHERE id = ?",
                estado, ventaId, notificacionId);
    }

    @Override
    public void marcarDescartada(Long notificacionId) {
        jdbcTemplate.update(
                "UPDATE pago_digital_notificaciones SET estado = 'DESCARTADA' WHERE id = ?",
                notificacionId);
    }

    @Override
    public NotificacionPagoDigitalResponse obtenerNotificacion(Long notificacionId, Long empresaId) {

        List<NotificacionPagoDigitalResponse> resultado = jdbcTemplate.query(
                "SELECT id, metodo, monto, remitente, texto_original, fecha_recibido, estado, venta_id, fecha_asociacion " +
                        "FROM pago_digital_notificaciones WHERE id = ? AND empresa_id = ?",
                (rs, rowNum) -> mapear(rs),
                notificacionId, empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<NotificacionPagoDigitalResponse> listarBandeja(Long empresaId, String estado, int page, int size) {

        int offset = (Math.max(page, 1) - 1) * size;

        return jdbcTemplate.query(
                "SELECT id, metodo, monto, remitente, texto_original, fecha_recibido, estado, venta_id, fecha_asociacion, " +
                        "COUNT(*) OVER() AS total_registros " +
                        "FROM pago_digital_notificaciones " +
                        "WHERE empresa_id = ? AND (? IS NULL OR estado = ?) " +
                        "ORDER BY fecha_recibido DESC " +
                        "LIMIT ? OFFSET ?",
                (rs, rowNum) -> {
                    NotificacionPagoDigitalResponse item = mapear(rs);
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, estado, estado, size, offset);
    }

    private NotificacionPagoDigitalResponse mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        NotificacionPagoDigitalResponse item = new NotificacionPagoDigitalResponse();
        item.setId(rs.getLong("id"));
        item.setMetodo(rs.getString("metodo"));
        item.setMonto(rs.getBigDecimal("monto"));
        item.setRemitente(rs.getString("remitente"));
        item.setTextoOriginal(rs.getString("texto_original"));
        Timestamp fechaRecibido = rs.getTimestamp("fecha_recibido");
        if (fechaRecibido != null) item.setFechaRecibido(fechaRecibido.toLocalDateTime());
        item.setEstado(rs.getString("estado"));
        Object ventaId = rs.getObject("venta_id");
        item.setVentaId(ventaId != null ? ((Number) ventaId).longValue() : null);
        Timestamp fechaAsociacion = rs.getTimestamp("fecha_asociacion");
        if (fechaAsociacion != null) item.setFechaAsociacion(fechaAsociacion.toLocalDateTime());
        return item;
    }
}
