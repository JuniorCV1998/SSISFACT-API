package pe.ssimple.ssisfact_api.dto.Admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpresaAdminItemResponse {
    private Long id;
    private String nombre;
    private String ruc;
    private String email;
    private String telefono;
    private Integer estado;
    private String plan;
    private Integer maxSucursal;
    private Integer maxUsuarios;
    private LocalDate fechaVencimiento;
    private Integer totalSucursales;
    private Integer totalUsuarios;
    private Integer totalVentasMes;
    private LocalDateTime fechaCreacion;

    @JsonIgnore
    private Integer totalRegistros;
}
