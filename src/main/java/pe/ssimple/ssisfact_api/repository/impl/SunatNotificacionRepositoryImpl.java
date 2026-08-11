package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatNotificationDto;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatPageResult;
import pe.ssimple.ssisfact_api.repository.SunatNotificacionRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SunatNotificacionRepositoryImpl implements SunatNotificacionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsert(Long empresaId, String ruc, SunatNotificationDto notificacion) {
        jdbcTemplate.queryForObject(
                "CALL sp_upsert_sunat_notificacion(?,?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> rs.getString("estado"),
                empresaId,
                ruc,
                notificacion.getId(),
                notificacion.getAsunto(),
                notificacion.getFechaPublicacion(),
                notificacion.getCategoriaCodigo(),
                notificacion.isLeido(),
                notificacion.isDestacado(),
                notificacion.isUrgente(),
                notificacion.isTieneAdjunto());
    }

    @Override
    public SunatPageResult<SunatNotificationDto> listar(Long empresaId, int pagina, int tamanio) {
        List<Object[]> rows = jdbcTemplate.query(
                "CALL sp_listar_sunat_notificaciones(?,?,?)",
                (rs, rowNum) -> new Object[]{
                        new SunatNotificationDto(
                                rs.getString("sunat_id"),
                                rs.getString("asunto"),
                                rs.getString("fecha_publicacion"),
                                rs.getString("categoria_codigo"),
                                rs.getString("categoria_descripcion"),
                                rs.getBoolean("leido"),
                                rs.getBoolean("destacado"),
                                rs.getBoolean("urgente"),
                                rs.getBoolean("tiene_adjunto")),
                        rs.getLong("total_count")
                },
                empresaId, pagina, tamanio);

        List<SunatNotificationDto> items = rows.stream().map(r -> (SunatNotificationDto) r[0]).toList();
        long total = rows.isEmpty() ? contar(empresaId) : (long) rows.get(0)[1];
        return new SunatPageResult<>(items, total);
    }

    private long contar(Long empresaId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sunat_notificaciones WHERE empresa_id = ?", Long.class, empresaId);
        return total != null ? total : 0L;
    }
}
