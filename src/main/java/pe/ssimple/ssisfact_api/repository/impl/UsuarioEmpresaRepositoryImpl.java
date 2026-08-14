package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;
import pe.ssimple.ssisfact_api.repository.UsuarioEmpresaRepository;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsuarioEmpresaRepositoryImpl implements UsuarioEmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<UsuarioAdminResponse> listarUsuariosEmpresa(Long empresaId) {

        return jdbcTemplate.query(
                "CALL sp_listar_usuarios_empresa_admin(?)",
                (rs, rowNum) -> {
                    UsuarioAdminResponse item = new UsuarioAdminResponse();
                    item.setId(rs.getLong("id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setEmail(rs.getString("email"));
                    item.setDocumento(rs.getString("documento"));
                    long sucursalId = rs.getLong("sucursal_id");
                    if (!rs.wasNull()) item.setSucursalId(sucursalId);
                    item.setEstado(rs.getInt("estado"));
                    String roles = rs.getString("roles");
                    item.setRoles(roles == null ? List.of() : Arrays.asList(roles.split(",")));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    return item;
                },
                empresaId);
    }

    @Override
    public AdminSaveResponse asignarSucursal(Long usuarioId, Long empresaId, Long sucursalId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_asignar_sucursal_usuario(?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                usuarioId, empresaId, sucursalId);
    }

    @Override
    public AdminSaveResponse crearUsuario(Long empresaId, String nombre, String email, String documento,
                                           String contrasenaHash, String rol, Long sucursalId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_usuario_empresa(?,?,?,?,?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                empresaId, nombre, email, documento, contrasenaHash, rol, sucursalId);
    }

    @Override
    public AdminSaveResponse asignarRol(Long usuarioId, Long empresaId, String rol) {

        return jdbcTemplate.queryForObject(
                "CALL sp_asignar_rol_usuario_empresa(?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                usuarioId, empresaId, rol);
    }
}
