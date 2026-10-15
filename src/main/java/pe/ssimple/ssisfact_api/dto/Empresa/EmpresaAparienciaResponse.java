package pe.ssimple.ssisfact_api.dto.Empresa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos visuales de la empresa que cualquier rol puede leer (sin RUC, email, teléfono ni IGV). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpresaAparienciaResponse {
    private String nombre;
    private String logoUrl;
    private String logoTicketUrl;
    private String colorPrimario;
    private String colorSecundario;
    private String colorAcento;
}
