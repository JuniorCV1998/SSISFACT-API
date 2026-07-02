package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaRequest;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSimpleResponse;

import java.util.List;

public interface CategoriaService {
    List<CategoriaResponse> listarCategorias(Long empresaId, String busqueda);
    List<CategoriaSimpleResponse> listarCategoriasParaSeleccion(Long empresaId, String busqueda);
    CategoriaSaveResponse guardarCategoria(CategoriaRequest request);
    CategoriaSaveResponse desactivarCategoria(Long categoriaId, Long empresaId);
}
