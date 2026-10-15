package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Notificacion.NotificacionResponse;
import pe.ssimple.ssisfact_api.repository.NotificacionRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificacionRepositoryImpl implements NotificacionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long insertar(Long empresaId, Long usuarioId, String tipo, String titulo, String mensaje,
                          String entidadTipo, Long entidadId) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO notificaciones (empresa_id, usuario_id, tipo, titulo, mensaje, entidad_tipo, entidad_id, fecha_creacion, leida) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'N')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, empresaId);
            if (usuarioId != null) ps.setLong(2, usuarioId); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, tipo);
            ps.setString(4, titulo);
            ps.setString(5, mensaje);
            ps.setString(6, entidadTipo);
            if (entidadId != null) ps.setLong(7, entidadId); else ps.setNull(7, java.sql.Types.BIGINT);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("No se pudo obtener el ID generado de la notificación");
        }
        return key.longValue();
    }

    // Visible para el usuario si es de toda la empresa (usuario_id NULL) o dirigida
    // a él puntualmente. No leídas primero, luego más recientes.
    @Override
    public List<NotificacionResponse> listar(Long empresaId, Long usuarioId, Boolean soloNoLeidas, int page, int size) {

        int offset = (Math.max(page, 1) - 1) * size;
        String filtroLeida = Boolean.TRUE.equals(soloNoLeidas) ? "N" : null;

        return jdbcTemplate.query(
                "SELECT id, tipo, titulo, mensaje, entidad_tipo, entidad_id, leida, fecha_creacion, fecha_leida, " +
                        "COUNT(*) OVER() AS total_registros " +
                        "FROM notificaciones " +
                        "WHERE empresa_id = ? AND (usuario_id IS NULL OR usuario_id = ?) " +
                        "AND (? IS NULL OR leida = ?) " +
                        "ORDER BY leida ASC, fecha_creacion DESC " +
                        "LIMIT ? OFFSET ?",
                (rs, rowNum) -> {
                    NotificacionResponse item = mapear(rs);
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, usuarioId, filtroLeida, filtroLeida, size, offset);
    }

    @Override
    public int contarNoLeidas(Long empresaId, Long usuarioId) {

        List<Integer> resultado = jdbcTemplate.query(
                "SELECT COUNT(*) AS total FROM notificaciones " +
                        "WHERE empresa_id = ? AND (usuario_id IS NULL OR usuario_id = ?) AND leida = 'N'",
                (rs, rowNum) -> rs.getInt("total"),
                empresaId, usuarioId);

        return resultado.isEmpty() ? 0 : resultado.get(0);
    }

    @Override
    public int marcarLeida(Long id, Long empresaId, Long usuarioId) {
        return jdbcTemplate.update(
                "UPDATE notificaciones SET leida = 'S', fecha_leida = NOW() " +
                        "WHERE id = ? AND empresa_id = ? AND (usuario_id IS NULL OR usuario_id = ?) AND leida = 'N'",
                id, empresaId, usuarioId);
    }

    @Override
    public void marcarTodasLeidas(Long empresaId, Long usuarioId) {
        jdbcTemplate.update(
                "UPDATE notificaciones SET leida = 'S', fecha_leida = NOW() " +
                        "WHERE empresa_id = ? AND (usuario_id IS NULL OR usuario_id = ?) AND leida = 'N'",
                empresaId, usuarioId);
    }

    private NotificacionResponse mapear(ResultSet rs) throws SQLException {
        NotificacionResponse item = new NotificacionResponse();
        item.setId(rs.getLong("id"));
        item.setTipo(rs.getString("tipo"));
        item.setTitulo(rs.getString("titulo"));
        item.setMensaje(rs.getString("mensaje"));
        item.setEntidadTipo(rs.getString("entidad_tipo"));
        Object entidadId = rs.getObject("entidad_id");
        item.setEntidadId(entidadId != null ? ((Number) entidadId).longValue() : null);
        item.setLeida("S".equals(rs.getString("leida")));
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) item.setFechaCreacion(fechaCreacion.toLocalDateTime());
        Timestamp fechaLeida = rs.getTimestamp("fecha_leida");
        if (fechaLeida != null) item.setFechaLeida(fechaLeida.toLocalDateTime());
        return item;
    }
}
