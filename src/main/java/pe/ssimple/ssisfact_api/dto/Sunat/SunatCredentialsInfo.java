package pe.ssimple.ssisfact_api.dto.Sunat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatCredentialsInfo {
    private String ruc;
    private String usernameSunat;
    @ToString.Exclude
    private String passwordSunatEncriptado;

    public boolean isConfigured() {
        return usernameSunat != null && !usernameSunat.isBlank()
                && passwordSunatEncriptado != null && !passwordSunatEncriptado.isBlank();
    }
}
