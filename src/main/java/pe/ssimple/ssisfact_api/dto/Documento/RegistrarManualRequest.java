package pe.ssimple.ssisfact_api.dto.Documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrarManualRequest {

    @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "^(01|06)$", message = "debe ser 01 (DNI) o 06 (RUC)")
    private String codTipoDoc;

    @NotBlank(message = "es obligatorio")
    private String nroDocumento;

    // Obligatorios si codTipoDoc = 01 (DNI) — validado en el service, no aquí,
    // porque depende del valor de codTipoDoc.
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    // Obligatorio si codTipoDoc = 06 (RUC)
    private String razonSocial;
    private String direccion;
}
