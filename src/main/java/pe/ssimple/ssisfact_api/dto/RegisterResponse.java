package pe.ssimple.ssisfact_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private String documento;
    private String nombre;
    private String apellido;
}