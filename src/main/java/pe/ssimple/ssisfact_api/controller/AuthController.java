package pe.ssimple.ssisfact_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Auth.LoginRequest;
import pe.ssimple.ssisfact_api.dto.Auth.LoginResponse;
import pe.ssimple.ssisfact_api.service.JwtService;

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

        UserDetails user = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(user.getUsername());

        LoginResponse loginResponse = new LoginResponse(token, user.getUsername());
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