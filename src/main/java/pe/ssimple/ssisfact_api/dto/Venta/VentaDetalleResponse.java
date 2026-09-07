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
    private String empresaNombre;
    private String empresaRuc;
    private String empresaDireccion;
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
    // Suma de las cantidades de todos los ítems, para el "N UNIDADES" del ticket.
    private BigDecimal totalUnidades;
    // Monto total en letras (ej. "DOSCIENTOS SESENTA CON 43/100 SOLES").
    private String montoEnLetras;
    private String estado;
    private LocalDateTime fecha;
    private Long comprobanteId;
    // "BOLETA"/"GUIA": clasificación interna de numeración (serie/correlativo), NO implica
    // que sea una boleta electrónica declarada ante SUNAT. Para imprimir, usar documentoTitulo.
    private String comprobanteTipo;
    private String comprobanteSerie;
    private Integer comprobanteNumero;
    // Listo para imprimir: "NP01-000001" (serie + correlativo con 6 dígitos).
    private String comprobanteCodigo;
    // Título honesto para imprimir en el ticket — no se declara ante SUNAT todavía.
    private String documentoTitulo;
    private List<VentaItemDetalleResponse> items;
    private List<PagoResponse> pagos;
}
