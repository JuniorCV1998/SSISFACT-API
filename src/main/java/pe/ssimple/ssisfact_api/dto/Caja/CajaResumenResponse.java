package pe.ssimple.ssisfact_api.dto.Caja;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CajaResumenResponse {
    private Long id;
    private Long sucursalId;
    private String sucursalNombre;
    private Long usuarioId;
    private String usuarioNombre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String estado;
    private Integer cantidadVentas;
    private BigDecimal totalVendido;

    // Arqueo físico esperado: montoInicial + pagos en EFECTIVO (sin importar el estado de la caja,
    // sirve como preview antes de cerrar y también para consultar una caja ya cerrada).
    private BigDecimal montoEsperado;

    // Todo el dinero ingresado, sin importar el método (EFECTIVO + YAPE + PLIN + ...).
    private BigDecimal totalIngresado;

    private List<PagoMetodoResumen> pagosPorMetodo;
    private List<ProductoVendidoResumen> productosVendidos;
}
