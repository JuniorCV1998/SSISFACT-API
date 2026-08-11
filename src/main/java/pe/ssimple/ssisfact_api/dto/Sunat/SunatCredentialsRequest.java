package pe.ssimple.ssisfact_api.dto.Sunat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatCredentialsRequest {

    @NotBlank(message = "el usuario SUNAT es obligatorio")
    private String username;

    @NotBlank(message = "la clave SOL es obligatoria")
    @ToString.Exclude
    private String password;
}
