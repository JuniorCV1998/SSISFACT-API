package pe.ssimple.ssisfact_api.dto.RegisterCompany;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
public class CompanyRegisterRequest {

    private Integer idRegisterProcess;

    // Empresa
    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 120, message = "La razón social no puede superar los 120 caracteres")
    private String razonSocial;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 120, message = "La dirección no puede superar los 120 caracteres")
    private String direccion;

    // SOLO 9 DIGITOS NUMERICOS
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener exactamente 9 dígitos numéricos")
    private String telefono;

    @NotBlank(message = "El correo de empresa es obligatorio")
    @Email(message = "El correo de empresa no es válido")
    private String emailEmpresa;

    // Admin
    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 12, message = "El documento no puede superar los 12 caracteres")
    private String documento;

    @NotBlank(message = "El nombre del administrador es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombreAdmin;

    @NotBlank(message = "El correo del administrador es obligatorio")
    @Email(message = "El correo del administrador no es válido")
    private String emailAdmin;

    // SOLO 6 DIGITOS NUMERICOS
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^\\d{6}$", message = "La contraseña debe tener exactamente 6 dígitos numéricos")
    private String contrasena;

    @NotBlank(message = "Debe confirmar la contraseña")
    @Pattern(regexp = "^\\d{6}$", message = "La confirmación debe tener exactamente 6 dígitos numéricos")
    private String confirmarContrasena;

}