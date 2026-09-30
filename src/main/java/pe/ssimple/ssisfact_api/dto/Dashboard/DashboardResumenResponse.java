package pe.ssimple.ssisfact_api.dto.Dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResumenResponse {
    private VentaHoyKpi ventasHoy;
    private ClientesAtendidosKpi clientesAtendidos;
    private TicketPromedioKpi ticketPromedio;
    private ProductosEnStockKpi productosEnStock;
    private List<VentaDiaResponse> ventasSemana;
    private List<ProductoMasVendidoResponse> productosMasVendidos;
    private List<MetodoPagoResponse> metodosPago;
    // Solo viene para ADMIN viendo el consolidado (sin filtrar por sucursal) y
    // cuando la empresa tiene más de una sucursal activa. En cualquier otro
    // caso viene null y el front simplemente no debe mostrar esa sección.
    private List<VentaSucursalResponse> ventasPorSucursal;
    private List<TransaccionRecienteResponse> transaccionesRecientes;
}
