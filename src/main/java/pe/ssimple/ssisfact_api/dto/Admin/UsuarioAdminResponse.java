package pe.ssimple.ssisfact_api.dto.Admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioAdminResponse {
    private Long id;
    private String nombre;
    private String email;
    private String documento;
    private Long sucursalId;
    private Integer estado;
    private List<String> roles;
    private LocalDateTime fechaCreacion;
}
