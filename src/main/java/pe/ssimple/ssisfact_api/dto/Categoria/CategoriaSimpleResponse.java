package pe.ssimple.ssisfact_api.dto.Categoria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaSimpleResponse {
    private Long id;
    private String nombre;
}
