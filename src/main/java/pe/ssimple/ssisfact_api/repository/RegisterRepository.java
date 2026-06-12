package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.entity.Usuario;

import java.util.Optional;

public interface RegisterRepository {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByDocumento(String documento);
    Usuario save(Usuario usuario);
    boolean existsByEmail(String email);
    boolean existsByDocumento(String documento);
}