package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualizarRequest;
import pe.ssimple.ssisfact_api.dto.Whatsapp.ClienteContactoDto;
import pe.ssimple.ssisfact_api.dto.Whatsapp.EmpresaProgramadaDto;
import pe.ssimple.ssisfact_api.dto.Whatsapp.WhatsappConfigResponse;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmpresaRepositoryImpl implements EmpresaRepository {

    public static final String MENSAJE_COMPROBANTE_DEFAULT =
            "Hola 👋\n\nTe enviamos tu comprobante de compra.\n\n📄 Documento: {{TIPO_COMPROBANTE}} {{SERIE-NÚMERO}}\n\nAdjuntamos el comprobante en formato PDF.\n\n¡Gracias por tu compra! 😊";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public EmpresaActualResponse obtenerActual(Long empresaId) {

        List<EmpresaActualResponse> resultado = jdbcTemplate.query(
                "SELECT e.id, e.nombre, e.ruc, e.direccion, e.telefono, e.email, e.logo_url, e.logo_ticket_url, " +
                "COALESCE(ce.igv_habilitado, 0) AS igv_habilitado, " +
                "COALESCE(ce.impuesto, 18.00) AS igv_porcentaje, " +
                "ce.ticket_mensaje_pie, " +
                "COALESCE(ce.asistente_nombre, 'Fact') AS asistente_nombre, " +
                "COALESCE(ce.asistente_tono, 'CERCANO') AS asistente_tono, " +
                "COALESCE(ce.voz_genero, 'FEMENINA') AS voz_genero, " +
                "ce.color_primario, ce.color_secundario, ce.color_acento " +
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
                        rs.getString("logo_ticket_url"),
                        rs.getBoolean("igv_habilitado"),
                        rs.getBigDecimal("igv_porcentaje"),
                        rs.getString("ticket_mensaje_pie"),
                        rs.getString("asistente_nombre"),
                        rs.getString("asistente_tono"),
                        rs.getString("voz_genero"),
                        rs.getString("color_primario"),
                        rs.getString("color_secundario"),
                        rs.getString("color_acento")),
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
                "ON DUPLICATE KEY UPDATE asistente_nombre = VALUES(asistente_nombre), asistente_tono = VALUES(asistente_tono), voz_genero = COALESCE(VALUES(voz_genero), voz_genero)",
                empresaId, nombreAsistente, tono, vozGenero);
    }

    @Override
    public void actualizarTema(Long empresaId, String colorPrimario, String colorSecundario, String colorAcento) {
        jdbcTemplate.update(
                "INSERT INTO configuracion_empresa (empresa_id, color_primario, color_secundario, color_acento) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE color_primario = VALUES(color_primario), color_secundario = VALUES(color_secundario), color_acento = VALUES(color_acento)",
                empresaId, colorPrimario, colorSecundario, colorAcento);
    }

    @Override
    public String obtenerLogoTicketUrlActual(Long empresaId) {

        List<String> resultado = jdbcTemplate.query(
                "SELECT logo_ticket_url FROM empresas WHERE id = ?",
                (rs, rowNum) -> rs.getString("logo_ticket_url"),
                empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public void actualizarLogoTicket(Long empresaId, String logoTicketUrl) {
        jdbcTemplate.update(
                "UPDATE empresas SET logo_ticket_url = ?, fecha_actualizacion = NOW() WHERE id = ?",
                logoTicketUrl, empresaId);
    }

    @Override
    public void actualizarWhatsappEstado(Long empresaId, boolean vinculado) {
        jdbcTemplate.update(
                "INSERT INTO configuracion_empresa (empresa_id, whatsapp_estado) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE whatsapp_estado = VALUES(whatsapp_estado)",
                empresaId, vinculado ? "S" : "N");
    }

    @Override
    public void actualizarWhatsappConfig(Long empresaId, boolean activo, String mensaje, LocalTime horaEnvio,
                                         String mensajeComprobante) {
        jdbcTemplate.update(
                "INSERT INTO configuracion_empresa (empresa_id, whatsapp_campana_activo, whatsapp_mensaje, whatsapp_hora_envio, whatsapp_mensaje_comprobante) " +
                "VALUES (?, ?, ?, ?, NULLIF(?, '')) " +
                "ON DUPLICATE KEY UPDATE whatsapp_campana_activo = VALUES(whatsapp_campana_activo), " +
                "whatsapp_mensaje = COALESCE(VALUES(whatsapp_mensaje), whatsapp_mensaje), " +
                "whatsapp_hora_envio = COALESCE(VALUES(whatsapp_hora_envio), whatsapp_hora_envio), " +
                "whatsapp_mensaje_comprobante = IF(? IS NULL, whatsapp_mensaje_comprobante, NULLIF(?, ''))",
                empresaId, activo ? 1 : 0, mensaje,
                horaEnvio == null ? null : Time.valueOf(horaEnvio.withSecond(0)),
                mensajeComprobante, mensajeComprobante, mensajeComprobante);
    }

    @Override
    public WhatsappConfigResponse obtenerWhatsappConfig(Long empresaId) {

        List<WhatsappConfigResponse> resultado = jdbcTemplate.query(
                "SELECT whatsapp_mensaje, whatsapp_hora_envio, " +
                "COALESCE(whatsapp_mensaje_comprobante, ?) AS whatsapp_mensaje_comprobante, " +
                "whatsapp_campana_activo, whatsapp_estado " +
                "FROM configuracion_empresa WHERE empresa_id = ?",
                (rs, rowNum) -> {
                    Time horaEnvio = rs.getTime("whatsapp_hora_envio");
                    return new WhatsappConfigResponse(
                            rs.getString("whatsapp_mensaje"),
                            horaEnvio != null ? horaEnvio.toLocalTime().toString() : null,
                            rs.getString("whatsapp_mensaje_comprobante"),
                            rs.getBoolean("whatsapp_campana_activo"),
                            "S".equals(rs.getString("whatsapp_estado")));
                },
                MENSAJE_COMPROBANTE_DEFAULT, empresaId);

        return resultado.isEmpty()
                ? new WhatsappConfigResponse(null, null, MENSAJE_COMPROBANTE_DEFAULT, false, false)
                : resultado.get(0);
    }

    @Override
    public List<ClienteContactoDto> listarClientesActivosConTelefono(Long empresaId) {
        return jdbcTemplate.query(
                "SELECT id, nombre, telefono FROM clientes " +
                "WHERE empresa_id = ? AND estado = 1 AND telefono IS NOT NULL AND telefono <> ''",
                (rs, rowNum) -> new ClienteContactoDto(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono")),
                empresaId);
    }

    @Override
    public List<EmpresaProgramadaDto> listarEmpresasParaEnvioProgramado(String horaActualHHmm) {
        return jdbcTemplate.query(
                "SELECT ce.empresa_id, ce.whatsapp_mensaje FROM configuracion_empresa ce " +
                "INNER JOIN empresas e ON e.id = ce.empresa_id AND e.estado <> 0 " +
                "WHERE ce.whatsapp_estado = 'S' AND ce.whatsapp_campana_activo = 1 " +
                "AND TIME_FORMAT(ce.whatsapp_hora_envio, '%H:%i') = ? " +
                "AND (ce.whatsapp_fecha_ultimo_envio IS NULL OR ce.whatsapp_fecha_ultimo_envio <> CURDATE())",
                (rs, rowNum) -> new EmpresaProgramadaDto(
                        rs.getLong("empresa_id"),
                        rs.getString("whatsapp_mensaje")),
                horaActualHHmm);
    }

    @Override
    public void marcarWhatsappEnvioRealizado(Long empresaId) {
        jdbcTemplate.update(
                "UPDATE configuracion_empresa SET whatsapp_fecha_ultimo_envio = CURDATE() WHERE empresa_id = ?",
                empresaId);
    }
}
