package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;

public interface CompanyRegisterRepository {
    CompanyRegisterResponse registrarEmpresa(CompanyRegisterRequest request);

    CompanyRegisterResponse confirmarEmpresa(CompanyConfirmRequest request);
}