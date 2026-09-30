package pe.ssimple.ssisfact_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.JwtService;
import pe.ssimple.ssisfact_api.service.TokenBlacklistService;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmpresaRepository empresaRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        if (tokenBlacklistService.isBlacklisted(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, username)) {

                    // El JWT solo prueba que el usuario existía y la contraseña era
                    // correcta AL MOMENTO DEL LOGIN. Un token ya emitido sigue siendo
                    // válido (firma/expiración) aunque después el admin desactive al
                    // usuario o la empresa quede suspendida/pendiente — por eso se
                    // revalida el estado actual en CADA request, no solo en el login.
                    String motivoBloqueo = motivoBloqueo(userDetails);
                    if (motivoBloqueo != null) {
                        escribirRespuestaBloqueada(response, motivoBloqueo);
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token inválido, continuar sin autenticación
        }

        filterChain.doFilter(request, response);
    }

    private String motivoBloqueo(UserDetails userDetails) {
        if (!(userDetails instanceof CustomUserDetails user)) {
            return null;
        }

        Integer usuarioEstado = user.getEstado();
        if (usuarioEstado != null && usuarioEstado == 0) {
            return "Tu cuenta fue desactivada. Contacta a tu administrador.";
        }

        boolean esSuperadmin = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_SUPERADMIN"::equals);

        if (esSuperadmin) {
            return null;
        }

        Integer empresaEstado = empresaRepository.obtenerEstado(user.getEmpresaId());
        if (empresaEstado == null || empresaEstado == 0) {
            return "Tu empresa está suspendida. Contacta a soporte.";
        }
        if (empresaEstado == 2) {
            return "Tu empresa está pendiente de aprobación.";
        }

        return null;
    }

    private void escribirRespuestaBloqueada(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(mensaje)));
    }
}