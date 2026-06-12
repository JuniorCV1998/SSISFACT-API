package pe.ssimple.ssisfact_api.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.ssimple.ssisfact_api.entity.Usuario;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RegisterRepositoryImpl implements RegisterRepository {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Optional<Usuario> findByDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento).isPresent();
    }
}