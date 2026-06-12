package pe.ssimple.ssisfact_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.RegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterResponse;
import pe.ssimple.ssisfact_api.entity.Usuario;
import pe.ssimple.ssisfact_api.service.RegisterService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = registerService.register(request);
        
        RegisterResponse response = new RegisterResponse();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setDocumento(usuario.getDocumento());
        /* response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido()); */
        
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente", response));
    }
}