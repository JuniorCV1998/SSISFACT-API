package pe.ssimple.ssisfact_api.dto.Documento;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteDocumentoResponse {
    private Long id;
    private String codTipoDoc;
    private String nroDocumento;

    // Solo DNI (codTipoDoc = "01")
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    // Solo RUC (codTipoDoc = "06")
    private String razonSocial;
    private String estadoRuc;
    private String condicion;
    private String direccion;
    private String ubigeo;

    private String origenDatos; // RENIEC | SUNAT | MANUAL
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
