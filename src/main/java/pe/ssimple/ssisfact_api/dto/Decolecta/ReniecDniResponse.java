package pe.ssimple.ssisfact_api.dto.Decolecta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// Respuesta cruda de GET /reniec/dni?numero={dni}
@Data
public class ReniecDniResponse {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("first_last_name")
    private String firstLastName;

    @JsonProperty("second_last_name")
    private String secondLastName;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("document_number")
    private String documentNumber;
}
