package pe.ssimple.ssisfact_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ssimple.ssisfact_api.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByDocumento(String documento);
    Optional<Usuario> findByEmailOrDocumento(String email, String documento);
}