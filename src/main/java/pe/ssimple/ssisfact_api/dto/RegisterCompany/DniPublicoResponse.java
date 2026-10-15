package pe.ssimple.ssisfact_api.dto.RegisterCompany;

import lombok.Data;

// Vista pública y mínima del DNI para el formulario de alta de empresa (sin login).
@Data
public class DniPublicoResponse {
    private String codTipoDoc;
    private String nroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
}
