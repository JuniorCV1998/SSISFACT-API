package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Documento.ClienteDocumentoResponse;
import pe.ssimple.ssisfact_api.repository.ClienteDocumentoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClienteDocumentoRepositoryImpl implements ClienteDocumentoRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ClienteDocumentoResponse> buscarPorDocumento(String codTipoDoc, String nroDocumento) {

        List<ClienteDocumentoResponse> resultado = jdbcTemplate.query(
                "SELECT * FROM cliente_documento WHERE cod_tipo_doc = ? AND nro_documento = ? AND estado = 1",
                ClienteDocumentoRepositoryImpl::mapear,
                codTipoDoc, nroDocumento);

        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public ClienteDocumentoResponse guardar(String codTipoDoc, String nroDocumento,
                                             String nombres, String apellidoPaterno, String apellidoMaterno,
                                             String razonSocial, String estadoRuc, String condicion,
                                             String direccion, String ubigeo, String origenDatos) {

        jdbcTemplate.update(
                "INSERT INTO cliente_documento (cod_tipo_doc, nro_documento, nombres, apellido_paterno, " +
                        "apellido_materno, razon_social, estado_ruc, condicion, direccion, ubigeo, origen_datos, estado) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,1) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "nombres = VALUES(nombres), apellido_paterno = VALUES(apellido_paterno), " +
                        "apellido_materno = VALUES(apellido_materno), razon_social = VALUES(razon_social), " +
                        "estado_ruc = VALUES(estado_ruc), condicion = VALUES(condicion), " +
                        "direccion = VALUES(direccion), ubigeo = VALUES(ubigeo), origen_datos = VALUES(origen_datos), " +
                        "estado = 1",
                codTipoDoc, nroDocumento, nombres, apellidoPaterno, apellidoMaterno,
                razonSocial, estadoRuc, condicion, direccion, ubigeo, origenDatos);

        return buscarPorDocumento(codTipoDoc, nroDocumento)
                .orElseThrow(() -> new IllegalStateException("No se pudo leer el documento recién guardado"));
    }

    private static ClienteDocumentoResponse mapear(ResultSet rs, int rowNum) throws SQLException {
        ClienteDocumentoResponse item = new ClienteDocumentoResponse();
        item.setId(rs.getLong("id"));
        item.setCodTipoDoc(rs.getString("cod_tipo_doc"));
        item.setNroDocumento(rs.getString("nro_documento"));
        item.setNombres(rs.getString("nombres"));
        item.setApellidoPaterno(rs.getString("apellido_paterno"));
        item.setApellidoMaterno(rs.getString("apellido_materno"));
        item.setRazonSocial(rs.getString("razon_social"));
        item.setEstadoRuc(rs.getString("estado_ruc"));
        item.setCondicion(rs.getString("condicion"));
        item.setDireccion(rs.getString("direccion"));
        item.setUbigeo(rs.getString("ubigeo"));
        item.setOrigenDatos(rs.getString("origen_datos"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
        Timestamp fa = rs.getTimestamp("fecha_actualizacion");
        if (fa != null) item.setFechaActualizacion(fa.toLocalDateTime());
        return item;
    }
}
