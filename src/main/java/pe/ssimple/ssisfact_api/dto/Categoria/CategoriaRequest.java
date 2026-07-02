package pe.ssimple.ssisfact_api.dto.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.ssimple.ssisfact_api.validator.SafeText;

@Data
public class CategoriaRequest {

    private Long idCategoria;

    // Se completa en el controller a partir del usuario autenticado, no se recibe del cliente
    private Long empresaId;

    @NotBlank(message = "es obligatorio")
    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @SafeText
    private String nombre;

    @Size(max = 255, message = "no puede superar los 255 caracteres")
    @SafeText
    private String descripcion;
}
