package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Admin.EmpresaAdminItemResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;
import pe.ssimple.ssisfact_api.repository.AdminEmpresaRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminEmpresaRepositoryImpl implements AdminEmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<EmpresaAdminItemResponse> listarEmpresas(String busqueda, int estado, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_empresas_admin(?,?,?,?)",
                (rs, rowNum) -> {
                    EmpresaAdminItemResponse item = new EmpresaAdminItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setRuc(rs.getString("ruc"));
                    item.setEmail(rs.getString("email"));
                    item.setTelefono(rs.getString("telefono"));
                    item.setEstado(rs.getInt("estado"));
                    item.setPlan(rs.getString("plan"));
                    item.setMaxSucursal(rs.getInt("max_sucursal"));
                    item.setMaxUsuarios(rs.getInt("max_usuarios"));
                    Date fv = rs.getDate("fecha_vencimiento");
                    if (fv != null) item.setFechaVencimiento(fv.toLocalDate());
                    item.setTotalSucursales(rs.getInt("total_sucursales"));
                    item.setTotalUsuarios(rs.getInt("total_usuarios"));
                    item.setTotalVentasMes(rs.getInt("total_ventas_mes"));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                busqueda, estado, page, size);
    }

    @Override
    public EmpresaAdminDetalleResponse obtenerEmpresa(Long empresaId) {

        List<EmpresaAdminDetalleResponse> resultado = jdbcTemplate.query(
                "CALL sp_obtener_empresa_admin(?)",
                (rs, rowNum) -> {
                    EmpresaAdminDetalleResponse item = new EmpresaAdminDetalleResponse();
                    item.setId(rs.getLong("id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setRuc(rs.getString("ruc"));
                    item.setEmail(rs.getString("email"));
                    item.setTelefono(rs.getString("telefono"));
                    item.setDireccion(rs.getString("direccion"));
                    item.setEstado(rs.getInt("estado"));
                    item.setPlan(rs.getString("plan"));
                    item.setMaxSucursal(rs.getInt("max_sucursal"));
                    item.setMaxUsuarios(rs.getInt("max_usuarios"));
                    Date fv = rs.getDate("fecha_vencimiento");
                    if (fv != null) item.setFechaVencimiento(fv.toLocalDate());
                    item.setTotalSucursales(rs.getInt("total_sucursales"));
                    item.setTotalUsuarios(rs.getInt("total_usuarios"));
                    item.setTotalVentasMes(rs.getInt("total_ventas_mes"));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    return item;
                },
                empresaId);

        return resultado.isEmpty() ? null : resultado.get(0);
    }

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
    public AdminSaveResponse actualizarEstadoEmpresa(Long empresaId, Integer estado) {

        return jdbcTemplate.queryForObject(
                "CALL sp_actualizar_estado_empresa(?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                empresaId, estado);
    }

    @Override
    public AdminSaveResponse actualizarPlanEmpresa(Long empresaId, String plan, Integer maxSucursal, Integer maxUsuarios, LocalDate fechaVencimiento) {

        return jdbcTemplate.queryForObject(
                "CALL sp_actualizar_plan_empresa(?,?,?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                empresaId, plan, maxSucursal, maxUsuarios, fechaVencimiento);
    }

    @Override
    public AdminSaveResponse asignarRolUsuario(Long usuarioId, Long empresaId, String rol) {

        return jdbcTemplate.queryForObject(
                "CALL sp_asignar_rol_usuario(?,?,?)",
                (rs, rowNum) -> new AdminSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                usuarioId, empresaId, rol);
    }
}
