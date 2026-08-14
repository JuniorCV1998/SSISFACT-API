package pe.ssimple.ssisfact_api.dto.Decolecta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// Respuesta cruda de GET /sunat/ruc?numero={ruc}
@Data
public class SunatRucResponse {

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("numero_documento")
    private String numeroDocumento;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("condicion")
    private String condicion;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("ubigeo")
    private String ubigeo;
}
