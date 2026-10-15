package pe.ssimple.ssisfact_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicContentBlock;
import pe.ssimple.ssisfact_api.dto.Assistant.CarritoItemResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteRequest;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSaveResponse;
import pe.ssimple.ssisfact_api.dto.Cliente.ClienteSimpleResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoItemResponse;
import pe.ssimple.ssisfact_api.dto.Producto.ProductoListResponse;
import pe.ssimple.ssisfact_api.dto.Stock.StockRequest;
import pe.ssimple.ssisfact_api.dto.Stock.StockResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalItemResponse;
import pe.ssimple.ssisfact_api.dto.Sucursal.SucursalListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaDetalleResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaItemRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListItemResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaListResponse;
import pe.ssimple.ssisfact_api.dto.Venta.VentaRequest;
import pe.ssimple.ssisfact_api.dto.Venta.VentaResponse;
import pe.ssimple.ssisfact_api.repository.AssistantConversationLogRepository;
import pe.ssimple.ssisfact_api.repository.VentaRepository;
import pe.ssimple.ssisfact_api.service.AssistantCarritoService;
import pe.ssimple.ssisfact_api.service.AssistantMemoryService;
import pe.ssimple.ssisfact_api.service.ClienteService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.ProductoService;
import pe.ssimple.ssisfact_api.service.StockService;
import pe.ssimple.ssisfact_api.service.SucursalService;
import pe.ssimple.ssisfact_api.service.ToolExecutorService;
import pe.ssimple.ssisfact_api.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Despacha cada tool_use que pide Claude hacia los servicios ya existentes de
// ventas/inventario/clientes. empresaId/sucursalId SIEMPRE del usuario autenticado
// (CustomUserDetails, resuelto del JWT) — cualquier valor equivalente que venga en
// el "input" del tool_use se ignora, igual que ClienteRequest.empresaId se completa
// en el controller y no se recibe del cliente. Nunca lanza: cualquier fallo se
// traduce en un tool_result con isError=true para que Claude lo explique al usuario.
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutorServiceImpl implements ToolExecutorService {

    private static final Map<String, String> PALABRAS_NUMERO = Map.ofEntries(
            Map.entry("cero", "0"), Map.entry("zero", "0"),
            Map.entry("uno", "1"), Map.entry("one", "1"),
            Map.entry("dos", "2"), Map.entry("two", "2"),
            Map.entry("tres", "3"), Map.entry("three", "3"),
            Map.entry("cuatro", "4"), Map.entry("four", "4"),
            Map.entry("cinco", "5"), Map.entry("five", "5"),
            Map.entry("seis", "6"), Map.entry("six", "6"),
            Map.entry("siete", "7"), Map.entry("seven", "7"),
            Map.entry("ocho", "8"), Map.entry("eight", "8"),
            Map.entry("nueve", "9"), Map.entry("nine", "9"),
            Map.entry("diez", "10"), Map.entry("ten", "10")
    );

    private final ClienteService clienteService;
    private final VentaService ventaService;
    private final ProductoService productoService;
    private final StockService stockService;
    private final SucursalService sucursalService;
    private final VentaRepository ventaRepository;
    private final AssistantMemoryService assistantMemoryService;
    private final AssistantCarritoService assistantCarritoService;
    private final AssistantConversationLogRepository assistantConversationLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public AnthropicContentBlock ejecutar(String toolName, Map<String, Object> input, String toolUseId, CustomUserDetails user, Long sucursalId) {

        Object resultado;
        boolean isError = false;

        try {
            resultado = switch (toolName) {
                case "buscar_cliente" -> buscarCliente(input, user);
                case "registrar_venta" -> registrarVenta(input, user, sucursalId);
                case "buscar_producto", "consultar_stock" -> buscarProducto(input, user, sucursalId);
                case "registrar_ingreso_stock" -> registrarIngresoStock(input, user, sucursalId);
                case "reporte_ventas" -> reporteVentas(input, user);
                case "consultar_comprobante" -> consultarComprobante(input, user);
                case "anular_comprobante" -> anularComprobante(input, user);
                case "registrar_cliente" -> registrarCliente(input, user);
                case "guardar_correccion" -> guardarCorreccion(input, user);
                case "agregar_al_pedido" -> agregarAlPedido(input, user, sucursalId);
                case "quitar_del_pedido" -> quitarDelPedido(input, user);
                case "ver_pedido" -> assistantCarritoService.obtenerCarrito(user.getUsuarioId());
                case "vaciar_pedido" -> vaciarPedido(user);
                case "emitir_pedido" -> emitirPedido(input, user, sucursalId);
                default -> Map.of("error", "Herramienta desconocida: " + toolName);
            };
        } catch (PermisoAsistenteException e) {
            resultado = Map.of("error", e.getMessage());
            isError = true;
        } catch (Exception e) {
            log.warn("Error ejecutando tool '{}' para empresa {}: {}", toolName, user.getEmpresaId(), e.getMessage());
            resultado = Map.of("error", "No se pudo completar la acción: " + e.getMessage());
            isError = true;
        }

        String resultJson = toJson(resultado);
        registrarLog(toolName, input, resultJson, user);

        return AnthropicContentBlock.toolResult(toolUseId, resultJson, isError);
    }

    private Object buscarCliente(Map<String, Object> input, CustomUserDetails user) {
        String busqueda = str(input, "busqueda");
        List<ClienteSimpleResponse> resultado = clienteService.listarClientesParaSeleccion(user.getEmpresaId(), busqueda);
        return Map.of("clientes", resultado);
    }

    private Object registrarVenta(Map<String, Object> input, CustomUserDetails user, Long sucursalId) {

        if (sucursalId == null) {
            return Map.of("error", "No se pudo determinar la sucursal. ¿Desde qué sucursal estás vendiendo?");
        }
        String metodoPago = str(input, "metodoPago");
        if (metodoPago == null || metodoPago.isBlank()) {
            return Map.of("error", "Falta el método de pago. Pregunta al usuario con qué método paga: efectivo, yape, plin, transferencia o tarjeta.");
        }

        VentaRequest request = new VentaRequest();
        request.setSucursalId(sucursalId);
        request.setTipoDocumento(str(input, "tipoDocumento") != null ? str(input, "tipoDocumento") : "NOTA_PEDIDO");
        Long clienteId = longVal(input, "clienteId");
        request.setClienteId(clienteId);
        request.setDescuento(bigDecimal(input, "descuento"));

        List<Map<String, Object>> itemsInput = list(input, "items");
        List<VentaItemRequest> items = new ArrayList<>();
        for (Map<String, Object> itemInput : itemsInput) {
            VentaItemRequest item = new VentaItemRequest();
            item.setProductoId(longVal(itemInput, "productoId"));
            item.setCantidad(bigDecimal(itemInput, "cantidad"));
            item.setPrecioUnitario(bigDecimal(itemInput, "precioUnitario"));
            item.setDescuento(bigDecimal(itemInput, "descuento"));
            items.add(item);
        }
        request.setItems(items);
        request.setMetodoPagoContado(metodoPago);

        VentaResponse response = ventaService.registrarVenta(request, user.getEmpresaId(), user.getUsuarioId());
        return response;
    }

    private Object buscarProducto(Map<String, Object> input, CustomUserDetails user, Long sucursalId) {
        String busqueda = str(input, "busqueda");
        String nombreSucursal = str(input, "sucursal");

        Long sucursalEfectiva = sucursalId;
        String sucursalConsultada = null;
        if (nombreSucursal != null && !nombreSucursal.isBlank()) {
            SucursalItemResponse sucursal = resolverSucursal(nombreSucursal, user);
            if (sucursal == null) {
                return Map.of("error", "No se encontró una sucursal llamada '" + nombreSucursal + "'");
            }
            sucursalEfectiva = sucursal.getId();
            sucursalConsultada = sucursal.getNombre();
        }

        ProductoListResponse resultado = buscarProductoTolerante(user.getEmpresaId(), busqueda, sucursalEfectiva, 20);
        List<Map<String, Object>> productos = new ArrayList<>();
        for (ProductoItemResponse p : resultado.getItems()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("nombre", p.getNombre());
            item.put("codigo", p.getCodigo());
            item.put("precio", p.getPrecio());
            item.put("stockTotal", p.getStockTotal());
            item.put("stockSucursal", p.getStockSucursal());
            productos.add(item);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        if (sucursalConsultada != null) {
            respuesta.put("sucursalConsultada", sucursalConsultada);
        }
        respuesta.put("productos", productos);
        return respuesta;
    }

    private SucursalItemResponse resolverSucursal(String nombreSucursal, CustomUserDetails user) {
        SucursalListResponse resultado = sucursalService.listarSucursales(
                user.getEmpresaId(), nombreSucursal, 1, 1, 5);
        return resultado.getItems().isEmpty() ? null : resultado.getItems().get(0);
    }

    /**
     * Búsqueda de producto tolerante a dictado por voz. sp_listar_productos exige que TODA
     * la búsqueda aparezca como un único substring contiguo contra nombre/código, pero el
     * reconocimiento de voz suele deletrear nombres con dígitos pegados a letras (ej. "7Up")
     * como palabras separadas ("Seven up"), lo que nunca matchea tal cual. Normaliza números
     * deletreados a dígitos y pega cada dígito con la palabra corta que lo sigue (ej. "7 up"
     * -> "7up", "500 ml" -> "500ml", igual a como se guardan los nombres reales); si aun así
     * no hay resultados, reintenta solo con el token más distintivo en vez de la frase completa.
     */
    private ProductoListResponse buscarProductoTolerante(Long empresaId, String busqueda, Long sucursalId, int size) {
        List<String> tokens = fusionarDigitosConSufijo(normalizarNumeros(busqueda));
        String busquedaFusionada = String.join(" ", tokens);

        ProductoListResponse resultado = productoService.listarProductos(
                empresaId, busquedaFusionada, 1, size, 1, 0, sucursalId);
        if (!resultado.getItems().isEmpty()) {
            return resultado;
        }

        String tokenClave = tokenMasDistintivo(tokens);
        if (tokenClave == null || tokenClave.equalsIgnoreCase(busquedaFusionada)) {
            return resultado;
        }
        return productoService.listarProductos(empresaId, tokenClave, 1, size, 1, 0, sucursalId);
    }

    private String normalizarNumeros(String texto) {
        String resultado = texto;
        for (Map.Entry<String, String> e : PALABRAS_NUMERO.entrySet()) {
            resultado = resultado.replaceAll("(?iu)\\b" + e.getKey() + "\\b", e.getValue());
        }
        return resultado;
    }

    private List<String> fusionarDigitosConSufijo(String texto) {
        String[] palabras = texto.trim().split("\\s+");
        List<String> fusionadas = new ArrayList<>();
        int i = 0;
        while (i < palabras.length) {
            String actual = palabras[i];
            if (actual.matches("\\d+") && i + 1 < palabras.length && palabras[i + 1].matches("\\p{L}{1,3}")) {
                fusionadas.add(actual + palabras[i + 1]);
                i += 2;
            } else {
                fusionadas.add(actual);
                i++;
            }
        }
        return fusionadas;
    }

    private String tokenMasDistintivo(List<String> tokens) {
        String mejor = null;
        for (String token : tokens) {
            if (token.length() < 3 || token.matches("\\d+")) continue;
            if (mejor == null || token.length() > mejor.length()) {
                mejor = token;
            }
        }
        return mejor;
    }

    private Object registrarIngresoStock(Map<String, Object> input, CustomUserDetails user, Long sucursalId) {

        if (sucursalId == null) {
            return Map.of("error", "No se pudo determinar la sucursal. ¿Desde qué sucursal estás registrando el ingreso?");
        }

        StockRequest request = new StockRequest();
        request.setProductoId(longVal(input, "productoId"));
        request.setSucursalId(sucursalId);
        request.setCantidad(intVal(input, "cantidad"));
        request.setTipo(str(input, "tipo"));
        request.setMotivo(str(input, "motivo"));

        StockResponse response = stockService.ingresarStock(request);
        return response;
    }

    private Object reporteVentas(Map<String, Object> input, CustomUserDetails user) {

        LocalDate fechaInicio = LocalDate.parse(str(input, "fechaInicio"));
        LocalDate fechaFin = LocalDate.parse(str(input, "fechaFin"));
        boolean esAdmin = esAdmin(user);
        Long sucursalId = esAdmin ? null : user.getSucursalId();

        VentaListResponse ventas = ventaService.listarVentas(
                user.getEmpresaId(), sucursalId, null, null, null, fechaInicio, fechaFin, 1, 1000);

        BigDecimal totalVentas = BigDecimal.ZERO;
        int cantidadVentas = 0;
        for (VentaListItemResponse v : ventas.getItems()) {
            if (!"ANULADA".equals(v.getEstado())) {
                totalVentas = totalVentas.add(v.getTotal());
                cantidadVentas++;
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("fechaInicio", fechaInicio.toString());
        resultado.put("fechaFin", fechaFin.toString());
        resultado.put("cantidadVentas", cantidadVentas);
        resultado.put("totalVentas", totalVentas);
        return resultado;
    }

    private Object consultarComprobante(Map<String, Object> input, CustomUserDetails user) {

        Long ventaId = resolverVentaId(input, user);
        if (ventaId == null) {
            return Map.of("error", "No se encontró un comprobante con esa serie y número");
        }
        VentaDetalleResponse detalle = ventaService.obtenerVentaDetalle(ventaId, user.getEmpresaId());
        return detalle;
    }

    private Object anularComprobante(Map<String, Object> input, CustomUserDetails user) {

        if (!esAdmin(user)) {
            throw new PermisoAsistenteException("Solo un administrador puede anular comprobantes");
        }

        Long ventaId = resolverVentaId(input, user);
        if (ventaId == null) {
            return Map.of("error", "No se encontró un comprobante con esa serie y número");
        }
        VentaResponse response = ventaService.anularVenta(ventaId, user.getEmpresaId());
        return response;
    }

    private Long resolverVentaId(Map<String, Object> input, CustomUserDetails user) {
        String serie = str(input, "serie");
        Integer numero = intVal(input, "numero");
        return ventaRepository.buscarVentaIdPorComprobante(user.getEmpresaId(), serie, numero);
    }

    private Object registrarCliente(Map<String, Object> input, CustomUserDetails user) {

        ClienteRequest request = new ClienteRequest();
        request.setEmpresaId(user.getEmpresaId());
        request.setTipoDocumento(str(input, "tipoDocumento"));
        request.setNumeroDocumento(str(input, "numeroDocumento"));
        request.setNombre(str(input, "nombre"));
        request.setTelefono(str(input, "telefono"));
        request.setEmail(str(input, "email"));
        request.setDireccion(str(input, "direccion"));

        ClienteSaveResponse response = clienteService.guardarCliente(request);
        return response;
    }

    private Object guardarCorreccion(Map<String, Object> input, CustomUserDetails user) {
        String textoOriginal = str(input, "textoOriginal");
        String correccion = str(input, "correccion");
        assistantMemoryService.saveCorrection(user.getEmpresaId(), textoOriginal, correccion);
        return Map.of("guardado", true);
    }

    private Object agregarAlPedido(Map<String, Object> input, CustomUserDetails user, Long sucursalId) {
        String busqueda = str(input, "busqueda");
        if (busqueda == null || busqueda.isBlank()) {
            return Map.of("error", "Debes indicar qué producto agregar");
        }
        BigDecimal cantidad = bigDecimal(input, "cantidad");
        if (cantidad == null) cantidad = BigDecimal.ONE;

        ProductoListResponse resultado = buscarProductoTolerante(user.getEmpresaId(), busqueda, sucursalId, 5);
        List<ProductoItemResponse> items = resultado.getItems();
        if (items.isEmpty()) {
            return Map.of("error", "No se encontró ningún producto con '" + busqueda + "'");
        }
        if (items.size() > 1) {
            return Map.of("ambiguo", true, "candidatos", candidatosProducto(items),
                    "mensaje", "Hay varios productos que coinciden con '" + busqueda + "', pregunta al usuario cuál es antes de agregarlo");
        }

        ProductoItemResponse producto = items.get(0);
        return assistantCarritoService.agregarItem(user.getUsuarioId(), producto.getId(), producto.getNombre(),
                producto.getCodigo(), cantidad, producto.getPrecio());
    }

    private Object quitarDelPedido(Map<String, Object> input, CustomUserDetails user) {
        String busqueda = str(input, "busqueda");
        BigDecimal cantidad = bigDecimal(input, "cantidad");

        List<CarritoItemResponse> items = assistantCarritoService.obtenerItemsCrudos(user.getUsuarioId());
        if (items.isEmpty()) {
            return Map.of("error", "El pedido en curso está vacío");
        }

        List<CarritoItemResponse> coincidencias = items.stream()
                .filter(i -> busqueda == null
                        || i.getNombre().toLowerCase().contains(busqueda.toLowerCase())
                        || (i.getCodigo() != null && i.getCodigo().equalsIgnoreCase(busqueda)))
                .toList();
        if (coincidencias.isEmpty()) {
            return Map.of("error", "'" + busqueda + "' no está en el pedido en curso");
        }
        if (coincidencias.size() > 1) {
            return Map.of("ambiguo", true, "candidatos", coincidencias,
                    "mensaje", "Hay varias líneas del pedido que coinciden con '" + busqueda + "', pregunta al usuario cuál quitar");
        }

        return assistantCarritoService.quitarItem(user.getUsuarioId(), coincidencias.get(0).getProductoId(), cantidad);
    }

    private Object vaciarPedido(CustomUserDetails user) {
        assistantCarritoService.vaciarCarrito(user.getUsuarioId());
        return Map.of("vaciado", true);
    }

    private Object emitirPedido(Map<String, Object> input, CustomUserDetails user, Long sucursalId) {
        List<CarritoItemResponse> items = assistantCarritoService.obtenerItemsCrudos(user.getUsuarioId());
        if (items.isEmpty()) {
            return Map.of("error", "El pedido en curso está vacío, agrega productos antes de emitir la venta");
        }
        if (sucursalId == null) {
            return Map.of("error", "No se pudo determinar la sucursal. ¿Desde qué sucursal estás vendiendo?");
        }
        String metodoPago = str(input, "metodoPago");
        if (metodoPago == null || metodoPago.isBlank()) {
            return Map.of("error", "Falta el método de pago. Pregunta al usuario con qué método paga: efectivo, yape, plin, transferencia o tarjeta.");
        }

        VentaRequest request = new VentaRequest();
        request.setSucursalId(sucursalId);
        request.setClienteId(longVal(input, "clienteId"));
        request.setTipoDocumento(str(input, "tipoDocumento") != null ? str(input, "tipoDocumento") : "NOTA_PEDIDO");
        request.setMetodoPagoContado(metodoPago);

        List<VentaItemRequest> ventaItems = new ArrayList<>();
        for (CarritoItemResponse item : items) {
            VentaItemRequest ventaItem = new VentaItemRequest();
            ventaItem.setProductoId(item.getProductoId());
            ventaItem.setCantidad(item.getCantidad());
            ventaItem.setPrecioUnitario(item.getPrecioUnitario());
            ventaItems.add(ventaItem);
        }
        request.setItems(ventaItems);

        VentaResponse response = ventaService.registrarVenta(request, user.getEmpresaId(), user.getUsuarioId());
        assistantCarritoService.vaciarCarrito(user.getUsuarioId());
        return response;
    }

    private List<Map<String, Object>> candidatosProducto(List<ProductoItemResponse> items) {
        List<Map<String, Object>> candidatos = new ArrayList<>();
        for (ProductoItemResponse p : items) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("id", p.getId());
            c.put("nombre", p.getNombre());
            c.put("codigo", p.getCodigo());
            c.put("precio", p.getPrecio());
            candidatos.add(c);
        }
        return candidatos;
    }

    private boolean esAdmin(CustomUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    private void registrarLog(String toolName, Map<String, Object> input, String resultJson, CustomUserDetails user) {
        try {
            assistantConversationLogRepository.registrarLog(
                    user.getEmpresaId(), user.getUsuarioId(), "tool", null,
                    toolName, toJson(input), resultJson);
        } catch (Exception e) {
            log.warn("No se pudo registrar el log de la tool '{}': {}", toolName, e.getMessage());
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return List.of();
    }

    private String str(Map<String, Object> input, String key) {
        Object value = input.get(key);
        return value == null ? null : value.toString();
    }

    private Long longVal(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }

    private Integer intVal(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(value.toString());
    }

    private BigDecimal bigDecimal(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private static class PermisoAsistenteException extends RuntimeException {
        PermisoAsistenteException(String message) {
            super(message);
        }
    }
}
