package pe.ssimple.ssisfact_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.RegisterRequest;
import pe.ssimple.ssisfact_api.entity.Usuario;
import pe.ssimple.ssisfact_api.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificar si el documento ya existe
        if (usuarioRepository.findByDocumento(request.getDocumento()).isPresent()) {
            throw new RuntimeException("El documento ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setDocumento(request.getDocumento());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        /* usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setActivo(true); */

        return usuarioRepository.save(usuario);
    }
}