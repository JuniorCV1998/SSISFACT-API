package pe.ssimple.ssisfact_api.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.ssimple.ssisfact_api.entity.Usuario;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        this.usuario = usuario;
        this.authorities = authorities;
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    public Long getEmpresaId() {
        return usuario.getEmpresaId();
    }

    public Long getSucursalId() {
        return usuario.getSucursalId();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(authorities);
    }
}
