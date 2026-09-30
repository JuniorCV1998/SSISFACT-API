package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Rol.RolAsignableResponse;

import java.util.List;

public interface RolService {
    List<RolAsignableResponse> listarAsignables();
}
