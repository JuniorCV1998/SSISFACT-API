package pe.ssimple.ssisfact_api.dto.RegisterCompany;

import lombok.Data;

// Vista pública y mínima del RUC para el formulario de alta de empresa (sin login).
@Data
public class RucPublicoResponse {
    private String codTipoDoc;
    private String nroDocumento;
    private String razonSocial;
}
