package pe.ssimple.ssisfact_api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Assistant.CarritoItemResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.CarritoResponse;
import pe.ssimple.ssisfact_api.service.AssistantCarritoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Estado en memoria del "pedido en curso" por usuario para el modo manos libres de Fact.
 * Mismo idioma que TokenBlacklistService: ConcurrentHashMap + purga programada, sin Redis/BD —
 * es estado efímero de sesión de voz, se pierde en un restart del proceso (aceptable en v1).
 */
@Slf4j
@Service
public class AssistantCarritoServiceImpl implements AssistantCarritoService {

    private final Map<Long, CarritoSesion> sesiones = new ConcurrentHashMap<>();

    @Value("${assistant.cart.inactivity-timeout-minutes}")
    private long ttlMinutos;

    @Override
    public CarritoResponse agregarItem(Long usuarioId, Long productoId, String nombre, String codigo,
                                        BigDecimal cantidad, BigDecimal precioUnitario) {
        CarritoSesion sesion = obtenerOCrearVigente(usuarioId);
        synchronized (sesion) {
            CarritoItemResponse existente = buscarPorProductoId(sesion, productoId);
            if (existente != null) {
                existente.setCantidad(existente.getCantidad().add(cantidad));
            } else {
                sesion.items.add(new CarritoItemResponse(productoId, nombre, codigo, cantidad, precioUnitario, null));
            }
            sesion.lastActivityMs = System.currentTimeMillis();
            return construirRespuesta(sesion);
        }
    }

    @Override
    public CarritoResponse quitarItem(Long usuarioId, Long productoId, BigDecimal cantidad) {
        CarritoSesion sesion = obtenerOCrearVigente(usuarioId);
        synchronized (sesion) {
            CarritoItemResponse existente = buscarPorProductoId(sesion, productoId);
            if (existente != null) {
                if (cantidad == null || existente.getCantidad().compareTo(cantidad) <= 0) {
                    sesion.items.remove(existente);
                } else {
                    existente.setCantidad(existente.getCantidad().subtract(cantidad));
                }
            }
            sesion.lastActivityMs = System.currentTimeMillis();
            return construirRespuesta(sesion);
        }
    }

    @Override
    public CarritoResponse obtenerCarrito(Long usuarioId) {
        CarritoSesion sesion = peekVigente(usuarioId);
        return sesion == null ? new CarritoResponse(List.of(), BigDecimal.ZERO) : construirRespuesta(sesion);
    }

    @Override
    public List<CarritoItemResponse> obtenerItemsCrudos(Long usuarioId) {
        CarritoSesion sesion = peekVigente(usuarioId);
        if (sesion == null) return List.of();
        synchronized (sesion) {
            return List.copyOf(sesion.items);
        }
    }

    @Override
    public void vaciarCarrito(Long usuarioId) {
        sesiones.remove(usuarioId);
    }

    @Override
    public String getCartContext(Long usuarioId) {
        CarritoSesion sesion = peekVigente(usuarioId);
        if (sesion == null || sesion.items.isEmpty()) {
            return "No hay ningún pedido en curso.";
        }
        synchronized (sesion) {
            StringBuilder sb = new StringBuilder("PEDIDO EN CURSO (aún no emitido):\n");
            BigDecimal total = BigDecimal.ZERO;
            for (CarritoItemResponse item : sesion.items) {
                BigDecimal subtotal = item.getPrecioUnitario().multiply(item.getCantidad());
                total = total.add(subtotal);
                sb.append("- ").append(item.getCantidad().stripTrailingZeros().toPlainString())
                        .append("x ").append(item.getNombre())
                        .append(" (S/.").append(item.getPrecioUnitario()).append(" c/u) = S/.")
                        .append(subtotal).append("\n");
            }
            sb.append("Total estimado: S/.").append(total);
            return sb.toString();
        }
    }

    /** Purga cada minuto las sesiones sin actividad reciente — evita que el mapa crezca indefinidamente. */
    @Scheduled(fixedRate = 60_000)
    public void purgarInactivos() {
        long ahora = System.currentTimeMillis();
        long ttlMs = ttlMinutos * 60_000;
        int antes = sesiones.size();
        sesiones.entrySet().removeIf(e -> ahora - e.getValue().lastActivityMs > ttlMs);
        int eliminados = antes - sesiones.size();
        if (eliminados > 0) {
            log.debug("Carritos de Fact: {} sesiones inactivas purgadas, {} restantes", eliminados, sesiones.size());
        }
    }

    private CarritoItemResponse buscarPorProductoId(CarritoSesion sesion, Long productoId) {
        return sesion.items.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst().orElse(null);
    }

    private CarritoSesion obtenerOCrearVigente(Long usuarioId) {
        return sesiones.compute(usuarioId, (id, actual) ->
                (actual == null || haExpirado(actual)) ? new CarritoSesion() : actual);
    }

    private CarritoSesion peekVigente(Long usuarioId) {
        CarritoSesion sesion = sesiones.get(usuarioId);
        if (sesion == null) return null;
        if (haExpirado(sesion)) {
            sesiones.remove(usuarioId, sesion);
            return null;
        }
        return sesion;
    }

    private boolean haExpirado(CarritoSesion sesion) {
        return System.currentTimeMillis() - sesion.lastActivityMs > ttlMinutos * 60_000;
    }

    private CarritoResponse construirRespuesta(CarritoSesion sesion) {
        List<CarritoItemResponse> copia = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItemResponse item : sesion.items) {
            BigDecimal subtotal = item.getPrecioUnitario().multiply(item.getCantidad());
            total = total.add(subtotal);
            copia.add(new CarritoItemResponse(item.getProductoId(), item.getNombre(), item.getCodigo(),
                    item.getCantidad(), item.getPrecioUnitario(), subtotal));
        }
        return new CarritoResponse(copia, total);
    }

    private static class CarritoSesion {
        final List<CarritoItemResponse> items = new ArrayList<>();
        volatile long lastActivityMs = System.currentTimeMillis();
    }
}
