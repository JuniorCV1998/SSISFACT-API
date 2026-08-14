package pe.ssimple.ssisfact_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Auth.LoginRequest;
import pe.ssimple.ssisfact_api.dto.Auth.LoginResponse;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.JwtService;
import pe.ssimple.ssisfact_api.service.TokenBlacklistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("usuarioId", user.getUsuarioId());
        claims.put("empresaId", user.getEmpresaId());
        claims.put("sucursalId", user.getSucursalId());
        claims.put("roles", roles);

        String token = jwtService.generateToken(user.getUsername(), claims);

        // Sesión única por usuario: invalida cualquier token anterior de este usuario
        tokenBlacklistService.registerActiveToken(user.getUsuarioId(), token, jwtService.extractExpiration(token));

        boolean esAdmin = roles.contains("ROLE_ADMIN");

        LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), roles, user.getSucursalId(), esAdmin);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
            } catch (Exception e) {
                // Token ya inválido/expirado: no hay nada que invalidar, el logout igual es "exitoso"
            }
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.success("Logout exitoso", null));
    }
}