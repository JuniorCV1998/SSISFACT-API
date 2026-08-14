package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Admin.AdminSaveResponse;
import pe.ssimple.ssisfact_api.dto.Admin.UsuarioAdminResponse;
import pe.ssimple.ssisfact_api.repository.UsuarioEmpresaRepository;
import pe.ssimple.ssisfact_api.service.UsuarioEmpresaService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioEmpresaServiceImpl implements UsuarioEmpresaService {

    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioAdminResponse> listarUsuariosEmpresa(Long empresaId) {
        return usuarioEmpresaRepository.listarUsuariosEmpresa(empresaId);
    }

    @Override
    public AdminSaveResponse asignarSucursal(Long usuarioId, Long empresaId, Long sucursalId) {
        return usuarioEmpresaRepository.asignarSucursal(usuarioId, empresaId, sucursalId);
    }

    @Override
    public AdminSaveResponse crearUsuario(Long empresaId, String nombre, String email, String documento,
                                           String password, String rol, Long sucursalId) {

        String contrasenaHash = passwordEncoder.encode(password);

        return usuarioEmpresaRepository.crearUsuario(
                empresaId, nombre, email, documento, contrasenaHash, rol.trim().toUpperCase(), sucursalId);
    }

    @Override
    public AdminSaveResponse asignarRol(Long usuarioId, Long empresaId, String rol) {
        return usuarioEmpresaRepository.asignarRol(usuarioId, empresaId, rol.trim().toUpperCase());
    }
}
