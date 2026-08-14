package pe.ssimple.ssisfact_api.dto.Cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSimpleResponse {
    private Long id;
    private String nombre;
    private String numeroDocumento;
}
