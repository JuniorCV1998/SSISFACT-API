package pe.ssimple.ssisfact_api.dto.RegisterCompany;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyConfirmRequest {

    @NotNull(message = "El id del proceso es obligatorio")
    private Integer idRegisterProcess;
}