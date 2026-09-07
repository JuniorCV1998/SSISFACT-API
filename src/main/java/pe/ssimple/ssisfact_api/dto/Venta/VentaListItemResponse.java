package pe.ssimple.ssisfact_api.dto.Venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaListItemResponse {
    private Long id;
    private Long sucursalId;
    private String sucursalNombre;
    private Long clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private Long usuarioId;
    private String usuarioNombre;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fecha;
    private String comprobanteTipo;
    private String comprobanteSerie;
    private Integer comprobanteNumero;
    // Listo para imprimir: "NP01-000001" (serie + correlativo con 6 dígitos).
    private String comprobanteCodigo;

    @JsonIgnore
    private Integer totalRegistros;
}
