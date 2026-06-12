package pe.ssimple.ssisfact_api.dto.RegisterCompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterResponse {

    private String estado;
    private String mensaje;
    private Integer id;

}