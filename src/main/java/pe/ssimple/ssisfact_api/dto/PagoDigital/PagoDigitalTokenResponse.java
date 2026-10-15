package pe.ssimple.ssisfact_api.dto.PagoDigital;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Token en claro — solo se devuelve en el momento de generarlo/regenerarlo, nunca más.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDigitalTokenResponse {
    private String token;
}
