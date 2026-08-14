package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Caja.CajaActualResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaListResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaResumenResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaSaveResponse;

import java.math.BigDecimal;

public interface CajaService {
    CajaSaveResponse abrirCaja(Long sucursalId, Long usuarioId, BigDecimal montoInicial);
    CajaCerrarResponse cerrarCaja(Long cajaId, Long usuarioId, BigDecimal montoFinal);
    CajaActualResponse obtenerCajaAbierta(Long usuarioId);
    CajaListResponse listarCajas(Long empresaId, Long sucursalId, String estado, int page, int size);
    CajaResumenResponse obtenerResumenCaja(Long cajaId, Long empresaId);
}
