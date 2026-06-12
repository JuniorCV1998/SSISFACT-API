package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;

public interface CompanyRegisterService {
    CompanyRegisterResponse registrarEmpresa(CompanyRegisterRequest request);
    CompanyRegisterResponse confirmarEmpresa(CompanyConfirmRequest request);
}