package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaRequest;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSaveResponse;
import pe.ssimple.ssisfact_api.repository.CategoriaRepository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<CategoriaResponse> listarCategorias(Long empresaId, String busqueda, int estado) {

        return jdbcTemplate.query(
                "CALL sp_listar_categorias(?,?,?)",
                (rs, rowNum) -> {
                    CategoriaResponse item = new CategoriaResponse();
                    item.setId(rs.getLong("id"));
                    item.setEmpresaId(rs.getLong("empresa_id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setDescripcion(rs.getString("descripcion"));
                    item.setEstado(rs.getInt("estado"));
                    Timestamp fc = rs.getTimestamp("fecha_creacion");
                    if (fc != null) item.setFechaCreacion(fc.toLocalDateTime());
                    Timestamp fa = rs.getTimestamp("fecha_actualizacion");
                    if (fa != null) item.setFechaActualizacion(fa.toLocalDateTime());
                    return item;
                },
                empresaId, busqueda, estado);
    }

    @Override
    public CategoriaSaveResponse guardarCategoria(CategoriaRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_categoria(?,?,?,?)",
                (rs, rowNum) -> new CategoriaSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                request.getIdCategoria(), // NULL: Crear, NOT NULL: Actualizar
                request.getEmpresaId(),
                request.getNombre(),
                request.getDescripcion());
    }

    @Override
    public CategoriaSaveResponse desactivarCategoria(Long categoriaId, Long empresaId) {

        return jdbcTemplate.queryForObject(
                "CALL sp_desactivar_categoria(?,?)",
                (rs, rowNum) -> new CategoriaSaveResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getLong("id")),
                categoriaId, empresaId);
    }
}
