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
        claims.put("empresaId", user.getEmpresaId());
        claims.put("sucursalId", user.getSucursalId());
        claims.put("roles", roles);

        String token = jwtService.generateToken(user.getUsername(), claims);

        LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), roles);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        // Invalidar la sesión si existe
        request.getSession(false).invalidate();
        
        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok(ApiResponse.success("Logout exitoso", null));
    }
}