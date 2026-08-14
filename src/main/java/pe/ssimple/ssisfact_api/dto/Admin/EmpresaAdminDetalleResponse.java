package pe.ssimple.ssisfact_api.dto.Admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpresaAdminDetalleResponse {
    private Long id;
    private String nombre;
    private String ruc;
    private String email;
    private String telefono;
    private String direccion;
    private Integer estado;
    private String plan;
    private Integer maxSucursal;
    private Integer maxUsuarios;
    private LocalDate fechaVencimiento;
    private Integer totalSucursales;
    private Integer totalUsuarios;
    private Integer totalVentasMes;
    private Boolean sunatConfigurado;
    private LocalDateTime fechaCreacion;
    private List<UsuarioAdminResponse> usuarios;
}
