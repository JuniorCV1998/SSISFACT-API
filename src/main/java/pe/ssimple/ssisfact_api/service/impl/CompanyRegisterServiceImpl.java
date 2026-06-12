package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;
import pe.ssimple.ssisfact_api.repository.CompanyRegisterRepository;
import pe.ssimple.ssisfact_api.service.CompanyRegisterService;

@Service
@RequiredArgsConstructor
public class CompanyRegisterServiceImpl implements CompanyRegisterService {

    private final CompanyRegisterRepository companyRegisterRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CompanyRegisterResponse registrarEmpresa(CompanyRegisterRequest request) {

        // VALIDAR CONTRASEÑAS
        if (!request.getContrasena()
                .equals(request.getConfirmarContrasena())) {

            return new CompanyRegisterResponse(
                    "ERROR_PASSWORD",
                    "Las contraseñas no coinciden",
                    0);
        }
        // CORREO EMPRESA Y ADMINISTRADOR DEBEN SER DISTINTOS
        if (request.getEmailEmpresa()
                .equalsIgnoreCase(request.getEmailAdmin())) {

            return new CompanyRegisterResponse(
                    "ERROR_EMAIL",
                    "El correo de empresa y administrador no pueden ser iguales",
                    0);
        }
        // VALIDAR NOMBRE DE EMPRESA Y ADMINISTRADOR
        if (!request.getNombreAdmin()
                .matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {

            return new CompanyRegisterResponse(
                    "ERROR_NOMBRE",
                    "El nombre contiene caracteres inválidos",
                    0);
        }

        String passwordEncriptado = passwordEncoder.encode(request.getContrasena());

        request.setRuc(request.getRuc().trim());
        request.setTelefono(request.getTelefono().trim());
        request.setEmailEmpresa(request.getEmailEmpresa().trim());
        request.setEmailAdmin(request.getEmailAdmin().trim());
        request.setDocumento(request.getDocumento().trim());
        request.setContrasena(passwordEncriptado);
        request.setEmailEmpresa(
                request.getEmailEmpresa().trim().toLowerCase());
        request.setEmailAdmin(
                request.getEmailAdmin().trim().toLowerCase());

        return companyRegisterRepository.registrarEmpresa(request);
    }

    @Override
    public CompanyRegisterResponse confirmarEmpresa(CompanyConfirmRequest request) {
        return companyRegisterRepository.confirmarEmpresa(request);
    }
}