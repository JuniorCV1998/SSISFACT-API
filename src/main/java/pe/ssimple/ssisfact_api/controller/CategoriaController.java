package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaRequest;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Categoria.CategoriaSimpleResponse;
import pe.ssimple.ssisfact_api.service.CategoriaService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.util.ResponseBuilder;

import java.util.List;

@RestController
@RequestMapping("/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping("/guardar")
    public ResponseEntity<ApiResponse<CategoriaSaveResponse>> guardarCategoria(
            @Valid @RequestBody CategoriaRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        request.setEmpresaId(user.getEmpresaId());

        return ResponseBuilder.build(categoriaService.guardarCategoria(request), "OK");
    }

    // Para administrar categorías: datos completos
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarCategorias(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda) {

        List<CategoriaResponse> result = categoriaService.listarCategorias(user.getEmpresaId(), busqueda);

        return ResponseEntity.ok(ApiResponse.success("Categorías obtenidas correctamente", result));
    }

    // Para el selector del flujo de crear producto: solo id y nombre, solo activas
    @GetMapping("/seleccionar")
    public ResponseEntity<ApiResponse<List<CategoriaSimpleResponse>>> listarCategoriasParaSeleccion(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "") String busqueda) {

        List<CategoriaSimpleResponse> result = categoriaService.listarCategoriasParaSeleccion(user.getEmpresaId(), busqueda);

        return ResponseEntity.ok(ApiResponse.success("Categorías obtenidas correctamente", result));
    }

    @PostMapping("/desactivar/{id}")
    public ResponseEntity<ApiResponse<CategoriaSaveResponse>> desactivarCategoria(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseBuilder.build(categoriaService.desactivarCategoria(id, user.getEmpresaId()), "OK");
    }
}
