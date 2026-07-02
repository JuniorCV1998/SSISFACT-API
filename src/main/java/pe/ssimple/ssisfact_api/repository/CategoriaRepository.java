package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaRequest;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSaveResponse;

import java.util.List;

public interface CategoriaRepository {
    List<CategoriaResponse> listarCategorias(Long empresaId, String busqueda, int estado);
    CategoriaSaveResponse guardarCategoria(CategoriaRequest request);
    CategoriaSaveResponse desactivarCategoria(Long categoriaId, Long empresaId);
}
