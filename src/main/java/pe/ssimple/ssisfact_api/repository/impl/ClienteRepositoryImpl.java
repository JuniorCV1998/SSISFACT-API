package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteItemResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;
import pe.ssimple.ssisfact_api.repository.ClienteRepository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClienteRepositoryImpl implements ClienteRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ClienteSaveResponse guardarCliente(ClienteRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_cliente(?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> new ClienteSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getIdCliente(), // NULL: Crear, NOT NULL: Actualizar
                request.getEmpresaId(),
                request.getTipoDocumento(),
                request.getNumeroDocumento(),
                request.getNombre(),
                request.getTelefono(),
                request.getEmail(),
                request.getDireccion());
    }

    @Override
    public List<ClienteItemResponse> listarClientes(Long empresaId, String busqueda, int estado, int page, int size) {

        return jdbcTemplate.query(
                "CALL sp_listar_clientes(?,?,?,?,?)",
                (rs, rowNum) -> {
                    ClienteItemResponse item = new ClienteItemResponse();
                    item.setId(rs.getLong("id"));
                    item.setEmpresaId(rs.getLong("empresa_id"));
                    item.setTipoDocumento(rs.getString("tipo_documento"));
                    item.setNumeroDocumento(rs.getString("numero_documento"));
                    item.setNombre(rs.getString("nombre"));
                    item.setTelefono(rs.getString("telefono"));
                    item.setEmail(rs.getString("email"));
                    item.setDireccion(rs.getString("direccion"));
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
    public ClienteSaveResponse desactivarCliente(Long clienteId, Long empresaId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_desactivar_cliente(?,?)",
                (rs, rowNum) -> new ClienteSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                clienteId, empresaId);
    }

    @Override
    public Long obtenerClienteGenerico(Long empresaId) {

        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM clientes WHERE empresa_id = ? AND numero_documento = '00000000' LIMIT 1",
                    Long.class,
                    empresaId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
