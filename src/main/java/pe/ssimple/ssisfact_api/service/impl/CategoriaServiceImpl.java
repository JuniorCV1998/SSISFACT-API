package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaRequest;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSimpleResponse;
import pe.ssimple.ssisfact_api.repository.CategoriaRepository;
import pe.ssimple.ssisfact_api.service.CategoriaService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<CategoriaResponse> listarCategorias(Long empresaId, String busqueda) {
        return categoriaRepository.listarCategorias(empresaId, busqueda.trim(), -1);
    }

    @Override
    public List<CategoriaSimpleResponse> listarCategoriasParaSeleccion(Long empresaId, String busqueda) {
        return categoriaRepository.listarCategorias(empresaId, busqueda.trim(), 1).stream()
                .map(categoria -> new CategoriaSimpleResponse(categoria.getId(), categoria.getNombre()))
                .toList();
    }

    @Override
    public CategoriaSaveResponse guardarCategoria(CategoriaRequest request) {

        if (request.getNombre().trim().isEmpty()) {
            return new CategoriaSaveResponse("ERROR_NOMBRE", "El nombre de la categoría no puede estar vacío", 0L);
        }

        request.setNombre(request.getNombre().trim());

        if (request.getDescripcion() != null) {
            request.setDescripcion(request.getDescripcion().trim());
        }

        return categoriaRepository.guardarCategoria(request);
    }

    @Override
    public CategoriaSaveResponse desactivarCategoria(Long categoriaId, Long empresaId) {
        return categoriaRepository.desactivarCategoria(categoriaId, empresaId);
    }
}
