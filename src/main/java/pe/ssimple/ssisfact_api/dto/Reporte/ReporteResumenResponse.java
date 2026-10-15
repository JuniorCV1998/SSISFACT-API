package pe.ssimple.ssisfact_api.dto.Reporte;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.ssimple.ssisfact_api.dto.Dashboard.MetodoPagoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.ProductoMasVendidoResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaDiaResponse;
import pe.ssimple.ssisfact_api.dto.Dashboard.VentaSucursalResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReporteResumenResponse {
    private BigDecimal totalVendido;
    private int cantidadVentas;
    private BigDecimal ticketPromedio;
    // % vs el mismo rango de días inmediatamente anterior. null si ese período
    // anterior no tuvo ventas (no hay base de comparación).
    private BigDecimal variacionPorcentualVsPeriodoAnterior;
    private PendientesCobroResponse pendientesCobro;
    private List<VentaDiaResponse> ventasPorDia;
    private List<ProductoMasVendidoResponse> productosMasVendidos;
    private List<MetodoPagoResponse> metodosPago;
    // Solo viene para ADMIN viendo el consolidado (sin filtrar por sucursal) y
    // cuando la empresa tiene más de una sucursal activa. En cualquier otro
    // caso viene null y el front simplemente no debe mostrar esa sección.
    private List<VentaSucursalResponse> ventasPorSucursal;
    private List<ClienteTopResponse> topClientes;
}
