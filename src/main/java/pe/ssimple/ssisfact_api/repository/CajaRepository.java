package pe.ssimple.ssisfact_api.repository;

import pe.ssimple.ssisfact_api.dto.Caja.CajaActualResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaItemResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaResumenResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Caja.PagoMetodoResumen;
import pe.ssimple.ssisfact_api.dto.Caja.ProductoVendidoResumen;

import java.math.BigDecimal;
import java.util.List;

public interface CajaRepository {
    CajaSaveResponse abrirCaja(Long sucursalId, Long usuarioId, BigDecimal montoInicial);
    CajaCerrarResponse cerrarCaja(Long cajaId, Long usuarioId, BigDecimal montoFinal);
    CajaActualResponse obtenerCajaAbierta(Long usuarioId);
    List<CajaItemResponse> listarCajas(Long empresaId, Long sucursalId, String estado, int page, int size);

    // --- Resumen de caja (todos los métodos de pago, no solo efectivo) ---
    CajaResumenResponse obtenerResumenCabecera(Long cajaId, Long empresaId);
    List<PagoMetodoResumen> obtenerPagosPorMetodo(Long cajaId);
    List<ProductoVendidoResumen> obtenerProductosVendidosCaja(Long cajaId);
}
