package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatMessageDto;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatPageResult;
import pe.ssimple.ssisfact_api.repository.SunatMensajeRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SunatMensajeRepositoryImpl implements SunatMensajeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsert(Long empresaId, String ruc, SunatMessageDto mensaje) {
        jdbcTemplate.queryForObject(
                "CALL sp_upsert_sunat_mensaje(?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> rs.getString("estado"),
                empresaId,
                ruc,
                mensaje.getId(),
                mensaje.getAsunto(),
                mensaje.getMensaje(),
                mensaje.getRemitente(),
                mensaje.getFechaPublicacion(),
                mensaje.isLeido(),
                mensaje.isTieneAdjunto());
    }

    @Override
    public SunatPageResult<SunatMessageDto> listar(Long empresaId, int pagina, int tamanio) {
        List<Object[]> rows = jdbcTemplate.query(
                "CALL sp_listar_sunat_mensajes(?,?,?)",
                (rs, rowNum) -> new Object[]{
                        new SunatMessageDto(
                                rs.getString("sunat_id"),
                                rs.getString("asunto"),
                                rs.getString("mensaje"),
                                rs.getString("remitente"),
                                rs.getString("fecha_publicacion"),
                                rs.getBoolean("leido"),
                                rs.getBoolean("tiene_adjunto")),
                        rs.getLong("total_count")
                },
                empresaId, pagina, tamanio);

        List<SunatMessageDto> items = rows.stream().map(r -> (SunatMessageDto) r[0]).toList();
        long total = rows.isEmpty() ? contar(empresaId) : (long) rows.get(0)[1];
        return new SunatPageResult<>(items, total);
    }

    private long contar(Long empresaId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sunat_mensajes WHERE empresa_id = ?", Long.class, empresaId);
        return total != null ? total : 0L;
    }
}
