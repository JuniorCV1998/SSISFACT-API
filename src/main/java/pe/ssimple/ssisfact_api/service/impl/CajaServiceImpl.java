package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Caja.CajaActualResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaCerrarResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaItemResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaListResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaResumenResponse;
import pe.ssimple.ssisfact_api.dto.Caja.CajaSaveResponse;
import pe.ssimple.ssisfact_api.dto.Caja.PagoMetodoResumen;
import pe.ssimple.ssisfact_api.repository.CajaRepository;
import pe.ssimple.ssisfact_api.service.CajaService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;

    @Override
    public CajaSaveResponse abrirCaja(Long sucursalId, Long usuarioId, BigDecimal montoInicial) {
        return cajaRepository.abrirCaja(sucursalId, usuarioId, montoInicial);
    }

    @Override
    public CajaCerrarResponse cerrarCaja(Long cajaId, Long usuarioId, BigDecimal montoFinal) {

        CajaCerrarResponse response = cajaRepository.cerrarCaja(cajaId, usuarioId, montoFinal);

        if ("OK".equals(response.getEstado())) {
            List<PagoMetodoResumen> detallePagos = cajaRepository.obtenerPagosPorMetodo(cajaId);

            response.setDetallePagos(detallePagos);
            response.setTotalIngresado(sumarTotales(detallePagos));
        }

        return response;
    }

    @Override
    public CajaActualResponse obtenerCajaAbierta(Long usuarioId) {
        return cajaRepository.obtenerCajaAbierta(usuarioId);
    }

    @Override
    public CajaListResponse listarCajas(Long empresaId, Long sucursalId, String estado, int page, int size) {

        List<CajaItemResponse> items = cajaRepository.listarCajas(empresaId, sucursalId, estado, page, size);

        int total = items.isEmpty() ? 0 : items.get(0).getTotalRegistros();

        return new CajaListResponse(items, total, page, size);
    }

    @Override
    public CajaResumenResponse obtenerResumenCaja(Long cajaId, Long empresaId) {

        CajaResumenResponse resumen = cajaRepository.obtenerResumenCabecera(cajaId, empresaId);
        if (resumen == null) {
            return null;
        }

        List<PagoMetodoResumen> pagosPorMetodo = cajaRepository.obtenerPagosPorMetodo(cajaId);

        resumen.setPagosPorMetodo(pagosPorMetodo);
        resumen.setProductosVendidos(cajaRepository.obtenerProductosVendidosCaja(cajaId));
        resumen.setTotalIngresado(sumarTotales(pagosPorMetodo));
        resumen.setMontoEsperado(resumen.getMontoInicial().add(obtenerTotalEfectivo(pagosPorMetodo)));

        return resumen;
    }

    private BigDecimal sumarTotales(List<PagoMetodoResumen> pagos) {
        return pagos.stream()
                .map(PagoMetodoResumen::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal obtenerTotalEfectivo(List<PagoMetodoResumen> pagos) {
        return pagos.stream()
                .filter(p -> "EFECTIVO".equals(p.getMetodo()))
                .map(PagoMetodoResumen::getTotal)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
