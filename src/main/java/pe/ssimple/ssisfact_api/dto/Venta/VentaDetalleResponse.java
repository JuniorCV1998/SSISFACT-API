package pe.ssimple.ssisfact_api.dto.Venta;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaDetalleResponse {
    private Long id;
    private Long empresaId;
    private Long sucursalId;
    private String sucursalNombre;
    private Long clienteId;
    private String clienteNombre;
    private String clienteTipoDocumento;
    private String clienteDocumento;
    private Long usuarioId;
    private String usuarioNombre;
    private Long cajaId;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fecha;
    private Long comprobanteId;
    private String comprobanteTipo;
    private String comprobanteSerie;
    private Integer comprobanteNumero;
    private List<VentaItemDetalleResponse> items;
    private List<PagoResponse> pagos;
}
