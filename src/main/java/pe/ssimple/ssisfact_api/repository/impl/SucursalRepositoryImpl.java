package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalItemResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalRequest;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalSaveResponse;
import pe.ssimple.ssisfact_api.repository.SucursalRepository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SucursalRepositoryImpl implements SucursalRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public SucursalSaveResponse guardarSucursal(SucursalRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_sucursal(?,?,?,?,?,?)",
                (rs, rowNum) -> new SucursalSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getIdSucursal(), // NULL: Crear, NOT NULL: Actualizar
                request.getEmpresaId(),
                request.getNombre(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEstado());
    }

    @Override
    public List<SucursalItemResponse> listarSucursales(Long empresaId, String busqueda, int estado, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_sucursales(?,?,?,?,?)",
                (rs, rowNum) -> {
                    SucursalItemResponse item = new SucursalItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setEmpresaId(rs.getLong("empresa_id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setDireccion(rs.getString("direccion"));
                    item.setTelefono(rs.getString("telefono"));
                    item.setEstado(rs.getInt("estado"));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                    if (fa != null) item.setFechaActualizacion(fa.toLocalDateTime());
                    item.setTotalRegistros(rs.getInt("total_registros"));
                    return item;
                },
                empresaId, busqueda, estado, page, size);
    }

    @Override
    public boolean perteneceAEmpresa(Long sucursalId, Long empresaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sucursales WHERE id = ? AND empresa_id = ?",
                Integer.class, sucursalId, empresaId);
        return count != null && count > 0;
    }

    @Override
    public Integer obtenerMaxSucursales(Long empresaId) {
        // Mismo default (10) que usa sp_crear_o_actualizar_sucursal cuando no hay configuración.
        List<Integer> resultado = jdbcTemplate.query(
                "SELECT COALESCE(max_sucursal, 10) FROM configuracion_empresa WHERE empresa_id = ?",
                (rs, rowNum) -> rs.getInt(1),
                empresaId);
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}
