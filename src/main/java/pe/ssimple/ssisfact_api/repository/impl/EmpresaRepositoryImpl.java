package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmpresaRepositoryImpl implements EmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public EmpresaActualResponse obtenerActual(Long empresaId) {

        List<EmpresaActualResponse> resultado = jdbcTemplate.query(
                "SELECT e.id, e.nombre, e.ruc, e.direccion, e.telefono, e.email, e.logo_url, " +
                "COALESCE(ce.igv_habilitado, 0) AS igv_habilitado, " +
                "COALESCE(ce.impuesto, 18.00) AS igv_porcentaje, " +
                "ce.ticket_mensaje_pie, " +
                "COALESCE(ce.asistente_nombre, 'Fact') AS asistente_nombre, " +
                "COALESCE(ce.asistente_tono, 'CERCANO') AS asistente_tono, " +
                "COALESCE(ce.voz_genero, 'FEMENINA') AS voz_genero " +
                "FROM empresas e " +
                "LEFT JOIN configuracion_empresa ce ON ce.empresa_id = e.id " +
                "WHERE e.id = ?",
                (rs, rowNum) -> new EmpresaActualResponse(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("ruc"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("logo_url"),
                        rs.getBoolean("igv_habilitado"),
                        rs.getBigDecimal("igv_porcentaje"),
                        rs.getString("ticket_mensaje_pie"),
                        rs.getString("asistente_nombre"),
                        rs.getString("asistente_tono"),
                        rs.getString("voz_genero")),
                empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public AdminSaveResponse actualizar(Long empresaId, EmpresaActualizarRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_actualizar_empresa(?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                empresaId,
                request.getNombre(),
                request.getRuc(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEmail(),
                request.isIgvHabilitado() ? 1 : 0,
                request.getIgvPorcentaje(),
                request.getTicketMensajePie());
    }

    @Override
    public String obtenerLogoUrlActual(Long empresaId) {

        List<String> resultado = jdbcTemplate.query(
                "SELECT logo_url FROM empresas WHERE id = ?",
                (rs, rowNum) -> rs.getString("logo_url"),
                empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public void actualizarLogo(Long empresaId, String logoUrl) {
        jdbcTemplate.update(
                "UPDATE empresas SET logo_url = ?, fecha_actualizacion = NOW() WHERE id = ?",
                logoUrl, empresaId);
    }

    @Override
    public Integer obtenerEstado(Long empresaId) {

        List<Integer> resultado = jdbcTemplate.query(
                "SELECT estado FROM empresas WHERE id = ?",
                (rs, rowNum) -> rs.getInt("estado"),
                empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public void actualizarConfigAsistente(Long empresaId, String nombreAsistente, String tono, String vozGenero) {
        jdbcTemplate.update(
                "INSERT INTO configuracion_empresa (empresa_id, asistente_nombre, asistente_tono, voz_genero) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE asistente_nombre = VALUES(asistente_nombre), asistente_tono = VALUES(asistente_tono), voz_genero = VALUES(voz_genero)",
                empresaId, nombreAsistente, tono, vozGenero);
    }
}
