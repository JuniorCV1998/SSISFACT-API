package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Assistant.CarritoItemResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.CarritoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AssistantCarritoService {

    CarritoResponse agregarItem(Long usuarioId, Long productoId, String nombre, String codigo,
                                 BigDecimal cantidad, BigDecimal precioUnitario);

    /** cantidad null = quitar la línea completa, sin importar la cantidad restante. */
    CarritoResponse quitarItem(Long usuarioId, Long productoId, BigDecimal cantidad);

    CarritoResponse obtenerCarrito(Long usuarioId);

    List<CarritoItemResponse> obtenerItemsCrudos(Long usuarioId);

    void vaciarCarrito(Long usuarioId);

    /** Texto formateado del pedido en curso para inyectar como bloque de system prompt no cacheable. */
    String getCartContext(Long usuarioId);
}
