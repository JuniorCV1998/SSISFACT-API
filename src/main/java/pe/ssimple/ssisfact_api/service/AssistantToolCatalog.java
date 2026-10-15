package pe.ssimple.ssisfact_api.service;

import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicCacheControl;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicTool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Catálogo estático de las tools que Fact puede invocar (nombre, descripción y
// JSON Schema de argumentos), consumido por ClaudeServiceImpl al construir cada
// AnthropicMessageRequest y por ToolExecutorServiceImpl al despachar cada tool_use.
public final class AssistantToolCatalog {

    private AssistantToolCatalog() {
    }

    public static List<AnthropicTool> tools() {
        List<AnthropicTool> tools = new ArrayList<>(List.of(
                tool("buscar_cliente",
                        "Busca clientes ya registrados en la empresa por nombre o número de documento.",
                        schema(Map.of("busqueda", prop("string", "Nombre o número de documento a buscar")),
                                List.of("busqueda"))),

                tool("registrar_venta",
                        "Registra una venta con uno o más productos. Si no se especifica cliente, se usa el cliente genérico de la empresa.",
                        schema(orderedMap(
                                        "clienteId", prop("integer", "Id del cliente (opcional)"),
                                        "tipoDocumento", prop("string", "NOTA_PEDIDO, BOLETA o GUIA. Por defecto NOTA_PEDIDO si no se indica"),
                                        "items", arrayProp("Lista de productos a vender",
                                                schema(orderedMap(
                                                                "productoId", prop("integer", "Id del producto"),
                                                                "cantidad", prop("number", "Cantidad a vender"),
                                                                "precioUnitario", prop("number", "Precio unitario, opcional (usa el precio actual si no se indica)"),
                                                                "descuento", prop("number", "Descuento en monto fijo sobre esta línea, opcional")),
                                                        List.of("productoId", "cantidad"))),
                                        "descuento", prop("number", "Descuento global en monto fijo, opcional"),
                                        "metodoPago", prop("string", "EFECTIVO, YAPE, PLIN, TRANSFERENCIA o TARJETA. Obligatorio: si el usuario no ha dicho con qué método paga, pregúntaselo antes de llamar a esta herramienta — no asumas un método por defecto.")),
                                List.of("items", "metodoPago"))),

                tool("buscar_producto",
                        "Busca productos del catálogo de la empresa por nombre o código.",
                        schema(Map.of("busqueda", prop("string", "Nombre o código del producto")),
                                List.of("busqueda"))),

                tool("consultar_stock",
                        "Consulta el stock disponible de uno o más productos por nombre o código. " +
                                "Si el usuario menciona una sucursal específica, pásala en 'sucursal' para obtener el stock de esa sucursal puntual " +
                                "(no el de la sucursal del usuario ni el total). Si no se menciona ninguna sucursal, se omite y se devuelve el stock total entre todas las sucursales.",
                        schema(orderedMap(
                                        "busqueda", prop("string", "Nombre o código del producto"),
                                        "sucursal", prop("string", "Nombre de la sucursal a consultar, opcional. Solo si el usuario la menciona explícitamente")),
                                List.of("busqueda"))),

                tool("registrar_ingreso_stock",
                        "Registra un ingreso de stock (compra, devolución o ajuste) para un producto en la sucursal del usuario.",
                        schema(orderedMap(
                                        "productoId", prop("integer", "Id del producto"),
                                        "cantidad", prop("integer", "Cantidad a ingresar"),
                                        "tipo", prop("string", "COMPRA, DEVOLUCION o AJUSTE"),
                                        "motivo", prop("string", "Motivo del ingreso, opcional")),
                                List.of("productoId", "cantidad", "tipo"))),

                tool("reporte_ventas",
                        "Genera un reporte de ventas (totales y cantidad) entre dos fechas.",
                        schema(orderedMap(
                                        "fechaInicio", prop("string", "Fecha de inicio en formato YYYY-MM-DD"),
                                        "fechaFin", prop("string", "Fecha de fin en formato YYYY-MM-DD"),
                                        "agrupacion", prop("string", "Opcional, no usado en la v1")),
                                List.of("fechaInicio", "fechaFin"))),

                tool("consultar_comprobante",
                        "Consulta el detalle de una venta a partir de la serie y número de su comprobante.",
                        schema(orderedMap(
                                        "serie", prop("string", "Serie del comprobante, ej. NP01"),
                                        "numero", prop("integer", "Número correlativo del comprobante")),
                                List.of("serie", "numero"))),

                tool("anular_comprobante",
                        "Anula una venta a partir de la serie y número de su comprobante. Requiere rol administrador.",
                        schema(orderedMap(
                                        "serie", prop("string", "Serie del comprobante, ej. NP01"),
                                        "numero", prop("integer", "Número correlativo del comprobante"),
                                        "motivo", prop("string", "Motivo de la anulación")),
                                List.of("serie", "numero", "motivo"))),

                tool("registrar_cliente",
                        "Registra un nuevo cliente en la empresa.",
                        schema(orderedMap(
                                        "tipoDocumento", prop("string", "DNI, RUC, CE o PASAPORTE"),
                                        "numeroDocumento", prop("string", "Número de documento"),
                                        "nombre", prop("string", "Nombre o razón social"),
                                        "telefono", prop("string", "Teléfono, opcional"),
                                        "email", prop("string", "Correo, opcional"),
                                        "direccion", prop("string", "Dirección, opcional")),
                                List.of("tipoDocumento", "numeroDocumento", "nombre"))),

                tool("guardar_correccion",
                        "Guarda una corrección que el usuario hizo sobre una respuesta o acción anterior del asistente, para no repetir el mismo error.",
                        schema(orderedMap(
                                        "textoOriginal", prop("string", "Lo que el asistente entendió o hizo originalmente"),
                                        "correccion", prop("string", "La corrección indicada por el usuario")),
                                List.of("textoOriginal", "correccion"))),

                tool("agregar_al_pedido",
                        "Agrega un producto al pedido en curso (modo manos libres), o suma la cantidad si ya estaba agregado. " +
                                "Usa esto cuando el usuario va armando una venta en varios mensajes, no para una venta descrita completa en un solo mensaje (para eso usa registrar_venta).",
                        schema(orderedMap(
                                        "busqueda", prop("string", "Nombre o código del producto a agregar"),
                                        "cantidad", prop("number", "Cantidad a agregar, por defecto 1 si no se indica")),
                                List.of("busqueda"))),

                tool("quitar_del_pedido",
                        "Quita un producto del pedido en curso, o reduce su cantidad. Solo aplica a productos ya agregados al pedido en curso.",
                        schema(orderedMap(
                                        "busqueda", prop("string", "Nombre o código del producto a quitar, tal como aparece en el pedido en curso"),
                                        "cantidad", prop("number", "Cantidad a quitar; si se omite, se quita la línea completa")),
                                List.of("busqueda"))),

                tool("ver_pedido",
                        "Muestra el detalle completo del pedido en curso (modo manos libres), con sus productos, cantidades y total estimado.",
                        schema(Map.of(), List.of())),

                tool("vaciar_pedido",
                        "Vacía por completo el pedido en curso sin emitir ninguna venta. Úsalo si el usuario cancela o quiere empezar de nuevo.",
                        schema(Map.of(), List.of())),

                tool("emitir_pedido",
                        "Convierte el pedido en curso (modo manos libres) en una venta real. Requiere que el usuario ya haya indicado el " +
                                "método de pago en la conversación — si no lo ha dicho, NO llames a esta herramienta: pregúntale primero con qué " +
                                "método paga (efectivo, yape, plin, transferencia o tarjeta). Una vez tengas el método de pago, úsala de inmediato " +
                                "cuando el usuario diga algo equivalente a 'emite venta', 'cierra el pedido' o 'eso es todo' — no es una acción " +
                                "destructiva, no pidas más confirmación que el método de pago.",
                        schema(orderedMap(
                                        "clienteId", prop("integer", "Id del cliente, opcional"),
                                        "tipoDocumento", prop("string", "NOTA_PEDIDO, BOLETA o GUIA. Por defecto NOTA_PEDIDO si no se indica"),
                                        "metodoPago", prop("string", "EFECTIVO, YAPE, PLIN, TRANSFERENCIA o TARJETA — obligatorio, ya debe haberlo dicho el usuario antes de llamar a esta herramienta.")),
                                List.of("metodoPago")))
        ));

        // Cachea todo el catálogo de tools como un único prefijo (junto con el
        // system prompt) — se marca solo en el último elemento del arreglo.
        tools.get(tools.size() - 1).setCacheControl(AnthropicCacheControl.ephemeral());
        return tools;
    }

    private static AnthropicTool tool(String name, String description, Map<String, Object> inputSchema) {
        return new AnthropicTool(name, description, inputSchema);
    }

    private static Map<String, Object> schema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    private static Map<String, Object> prop(String type, String description) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", type);
        p.put("description", description);
        return p;
    }

    private static Map<String, Object> arrayProp(String description, Map<String, Object> itemsSchema) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", "array");
        p.put("description", description);
        p.put("items", itemsSchema);
        return p;
    }

    private static Map<String, Object> orderedMap(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }
}
