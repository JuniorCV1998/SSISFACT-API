package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Rol.RolAsignableResponse;
import pe.ssimple.ssisfact_api.repository.RolRepository;
import pe.ssimple.ssisfact_api.service.RolService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    public List<RolAsignableResponse> listarAsignables() {
        return rolRepository.listarAsignables();
    }
}
