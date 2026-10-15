package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicContentBlock;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicMessage;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicMessageRequest;
import pe.ssimple.ssisfact_api.dto.Anthropic.AnthropicMessageResponse;
import pe.ssimple.ssisfact_api.dto.Assistant.AssistantChatResponse;
import pe.ssimple.ssisfact_api.dto.Empresa.EmpresaActualResponse;
import pe.ssimple.ssisfact_api.repository.AssistantConversationLogRepository;
import pe.ssimple.ssisfact_api.repository.EmpresaRepository;
import pe.ssimple.ssisfact_api.repository.SucursalRepository;
import pe.ssimple.ssisfact_api.service.AnthropicClient;
import pe.ssimple.ssisfact_api.service.AssistantCarritoService;
import pe.ssimple.ssisfact_api.service.AssistantMemoryService;
import pe.ssimple.ssisfact_api.service.AssistantToolCatalog;
import pe.ssimple.ssisfact_api.service.ClaudeService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.ToolExecutorService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeServiceImpl implements ClaudeService {

    private static final String NOMBRE_ASISTENTE_DEFAULT = "Fact";

    private final AnthropicClient anthropicClient;
    private final AssistantMemoryService assistantMemoryService;
    private final AssistantCarritoService assistantCarritoService;
    private final ToolExecutorService toolExecutorService;
    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final AssistantConversationLogRepository assistantConversationLogRepository;

    @Value("${anthropic.model}")
    private String model;

    @Value("${assistant.max-tool-roundtrips}")
    private int maxToolRoundtrips;

    @Override
    public AssistantChatResponse chat(CustomUserDetails user, String mensaje, String moduloActivo, Long sucursalId) {

        EmpresaActualResponse empresa = empresaRepository.obtenerActual(user.getEmpresaId());
        String nombreAsistente = empresa != null && empresa.getAsistenteNombre() != null
                ? empresa.getAsistenteNombre() : NOMBRE_ASISTENTE_DEFAULT;
        Long sucursalEfectiva = resolverSucursalEfectiva(user, sucursalId);

        List<AnthropicContentBlock> systemPrompt = List.of(
                AnthropicContentBlock.textCacheable(construirSystemPromptEstable(user, moduloActivo, empresa, nombreAsistente)),
                AnthropicContentBlock.text(assistantCarritoService.getCartContext(user.getUsuarioId())));

        List<AnthropicMessage> messages = new ArrayList<>();
        messages.add(new AnthropicMessage("user", List.of(AnthropicContentBlock.text(mensaje))));

        Set<String> toolsUsados = new LinkedHashSet<>();
        String ultimoTexto = null;

        for (int ronda = 0; ronda <= maxToolRoundtrips; ronda++) {

            AnthropicMessageRequest request = new AnthropicMessageRequest(
                    model, 1024, systemPrompt, messages, AssistantToolCatalog.tools());

            Optional<AnthropicMessageResponse> respuestaOpt = anthropicClient.enviarMensaje(request);
            if (respuestaOpt.isEmpty()) {
                registrarLog(user, "user", mensaje, null, null);
                return new AssistantChatResponse(nombreAsistente + " no está disponible en este momento, intenta de nuevo.", List.copyOf(toolsUsados));
            }

            AnthropicMessageResponse respuesta = respuestaOpt.get();
            ultimoTexto = extraerTexto(respuesta.getContent());

            if (!"tool_use".equals(respuesta.getStopReason())) {
                break;
            }

            if (ronda == maxToolRoundtrips) {
                log.warn("Fact alcanzó el tope de {} round-trips de tools para empresa {}", maxToolRoundtrips, user.getEmpresaId());
                if (ultimoTexto == null || ultimoTexto.isBlank()) {
                    ultimoTexto = "Se alcanzó el límite de pasos permitidos para esta consulta, intenta ser más específico.";
                }
                break;
            }

            messages.add(new AnthropicMessage("assistant", respuesta.getContent()));

            List<AnthropicContentBlock> toolResults = new ArrayList<>();
            for (AnthropicContentBlock block : respuesta.getContent()) {
                if ("tool_use".equals(block.getType())) {
                    toolsUsados.add(block.getName());
                    AnthropicContentBlock resultado = toolExecutorService.ejecutar(
                            block.getName(), block.getInput(), block.getId(), user, sucursalEfectiva);
                    toolResults.add(resultado);
                }
            }
            messages.add(new AnthropicMessage("user", toolResults));
        }

        String respuestaFinal = (ultimoTexto == null || ultimoTexto.isBlank())
                ? "Entendido."
                : ultimoTexto;

        registrarLog(user, "user", mensaje, null, null);
        registrarLog(user, "assistant", respuestaFinal, null, null);
        assistantMemoryService.learnFromConversation(user.getEmpresaId(), user.getUsuarioId(), mensaje, respuestaFinal);

        return new AssistantChatResponse(respuestaFinal, List.copyOf(toolsUsados));
    }

    /**
     * Sucursal a usar para las tools que la requieren (registrar_venta, emitir_pedido, etc.).
     * Si el usuario tiene sucursal fija en el JWT (CAJERO/ALMACEN), esa manda SIEMPRE — el
     * front no puede sobreescribirla. Si no la tiene (ADMIN/SUPERADMIN, que operan sobre
     * todas las sucursales de la empresa), se usa la que el front mande como "sucursal activa"
     * del selector, validando primero que pertenezca a la misma empresa del usuario.
     */
    private Long resolverSucursalEfectiva(CustomUserDetails user, Long sucursalIdFront) {
        if (user.getSucursalId() != null) {
            return user.getSucursalId();
        }
        if (sucursalIdFront != null && sucursalRepository.perteneceAEmpresa(sucursalIdFront, user.getEmpresaId())) {
            return sucursalIdFront;
        }
        return null;
    }

    private String construirSystemPromptEstable(CustomUserDetails user, String moduloActivo,
                                                  EmpresaActualResponse empresa, String nombreAsistente) {

        String nombreEmpresa = empresa != null ? empresa.getNombre() : "";
        String ruc = empresa != null ? empresa.getRuc() : "";
        String nombreUsuario = user.getNombre() != null ? user.getNombre() : user.getUsername();
        String memoria = assistantMemoryService.getMemoryContext(user.getEmpresaId());
        String tono = instruccionTono(empresa != null ? empresa.getAsistenteTono() : null);
        LocalDate hoy = LocalDate.now();
        String fechaActual = hoy + " (" + hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "PE"))
                + " " + hoy.getDayOfMonth() + " de " + hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "PE")) + " de " + hoy.getYear() + ")";

        return """
                Eres %s, el asistente inteligente del facturador Ssimple Fact.
                Ayudas a los usuarios peruanos a gestionar su negocio de forma rápida y natural.

                EMPRESA ACTIVA: %s (RUC: %s)
                USUARIO: %s
                MÓDULO: %s
                FECHA ACTUAL: %s — esta es la fecha real de hoy, úsala siempre que necesites "hoy" o calcular rangos de fechas para reportes. Nunca digas que no tienes acceso a la fecha ni inventes otra.

                LO QUE RECUERDAS DE ESTA EMPRESA:
                %s

                COMPORTAMIENTO:
                - Responde siempre en español, breve y directo.
                - Tono: %s
                - Usa los alias que conoces de esta empresa (ej: si sabes que 'margarita' = 'Galleta Margarita x6', resuélvelo solo sin preguntar).
                - Si detectas un alias nuevo que no conoces, resuélvelo con la herramienta de búsqueda y guárdalo con guardar_correccion si el usuario te corrige.
                - Para ventas: si la empresa casi siempre usa el mismo tipo de comprobante, asúmelo sin preguntar.
                - Confirma solo acciones destructivas (anulaciones).
                - Si el usuario te corrige, usa la herramienta guardar_correccion y agradece.
                - IGV peruano: 18%%.
                - Máximo 3 líneas de respuesta salvo que el usuario pida un reporte.
                - Para pedidos armados en varios mensajes (modo manos libres: "agrega...", "quita...", "emite venta"), usa agregar_al_pedido / quitar_del_pedido / ver_pedido / vaciar_pedido / emitir_pedido — van acumulando el PEDIDO EN CURSO que ves más abajo.
                - Para una venta completamente descrita en un solo mensaje (ej. "vende 3 panes y 2 gaseosas a Juan, boleta"), usa directamente registrar_venta sin pasar por el pedido en curso.
                - IMPORTANTE sobre memoria: cada mensaje que recibes se evalúa SOLO con el PEDIDO EN CURSO que ves más abajo como memoria — no recuerdas el texto de mensajes anteriores de esta conversación. Por eso, para reconocer la respuesta a una pregunta que tú mismo hiciste (ej. el método de pago), básate en el PEDIDO EN CURSO y en el contenido del mensaje actual, nunca asumas que "recuerdas" algo que no está en el PEDIDO EN CURSO ni en el mensaje de ahora.
                - Por la misma razón, NUNCA propongas una acción como pregunta aparte para que el usuario la confirme en otro mensaje (ej. "¿quieres que genere el reporte de ventas de hoy?") — si el usuario responde "sí" en el siguiente mensaje, no vas a recordar qué le ofreciste. En su lugar: si ya tienes todo lo necesario para ejecutar la acción (incluida la fecha de hoy, que ya conoces), ejecútala directamente de una vez; si falta un dato puntual (ej. el rango de fechas de un reporte), pide ESE dato directamente en el mismo mensaje en vez de ofrecer la acción.
                - Regla mecánica para cerrar una venta (pedido en curso): si el mensaje del usuario habla de emitir, cerrar, finalizar o cobrar una venta ("emite venta", "emite una venta", "cierra el pedido", "ya está", "cóbrame", "eso es todo", etc.) y NO menciona productos concretos en ese mismo mensaje:
                  a) Si ese mismo mensaje YA incluye el método de pago (efectivo, yape, plin, transferencia o tarjeta), llama de inmediato a emitir_pedido con ese valor en metodoPago.
                  b) Si no lo incluye, responde solo "¿Con qué método pagas? (efectivo, yape, plin, transferencia o tarjeta)" y NO llames a emitir_pedido todavía.
                - Regla mecánica para la respuesta al método de pago: si el PEDIDO EN CURSO tiene al menos un producto y el mensaje del usuario consiste únicamente en nombrar uno de los métodos de pago válidos (efectivo, yape, plin, transferencia, tarjeta — con o sin palabras sueltas alrededor, ej. "efectivo", "en efectivo", "yape por favor"), SIEMPRE interpreta eso como la respuesta a la pregunta del método de pago y llama de inmediato a emitir_pedido con ese valor en metodoPago — no vuelvas a preguntar, no pidas confirmación adicional, no es una acción destructiva.
                - En ambos casos (a y b arriba), una vez tengas el método de pago, llamar a emitir_pedido es obligatorio e inmediato — sin excepción, y sin responder en texto que el pedido está vacío o pidiendo detalles de productos sin haberla llamado antes. La herramienta es la única fuente de verdad: si el pedido en curso realmente está vacío, ella misma te lo dirá y ahí sí pides los productos. Solo evita emitir_pedido cuando el mensaje SÍ trae productos concretos a vender (ej. "vende 2 panes a Juan, boleta, efectivo") — ahí usa registrar_venta directamente, que también requiere el método de pago.
                - Si buscar_producto o agregar_al_pedido devuelven varias coincidencias ambiguas, pregunta al usuario cuál es antes de agregarlo.
                - Al confirmar una acción sobre un producto (agregar_al_pedido, quitar_del_pedido, registrar_venta, etc.), usa SIEMPRE el nombre exacto que viene en el campo "nombre" del resultado de la herramienta, nunca la frase textual que dijo el usuario — el usuario puede decir "Seven up" pero si la herramienta resolvió "7Up 500ml", tu confirmación debe decir "7Up 500ml".

                FORMATO DE RESPUESTA TRAS EJECUTAR ACCIÓN:
                "✓ [acción realizada]: [detalle breve]"
                Ejemplo: "✓ Venta registrada: 3 productos · S/.45.50"
                """.formatted(nombreAsistente, nombreEmpresa, ruc, nombreUsuario, moduloActivo == null ? "" : moduloActivo, fechaActual, memoria, tono);
    }

    private String instruccionTono(String tono) {
        return switch (tono == null ? "CERCANO" : tono) {
            case "FORMAL" -> "Trata al usuario de manera formal y profesional, sin coloquialismos.";
            case "DIRECTO" -> "Sé lo más directo y breve posible, evita frases de cortesía innecesarias.";
            default -> "Trata al usuario de forma cercana y cordial, como un colega de confianza.";
        };
    }

    private String extraerTexto(List<AnthropicContentBlock> content) {
        if (content == null) return null;
        StringBuilder sb = new StringBuilder();
        for (AnthropicContentBlock block : content) {
            if ("text".equals(block.getType()) && block.getText() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(block.getText());
            }
        }
        return sb.toString();
    }

    private void registrarLog(CustomUserDetails user, String rol, String mensaje, String toolName, String toolResult) {
        try {
            assistantConversationLogRepository.registrarLog(
                    user.getEmpresaId(), user.getUsuarioId(), rol, mensaje, toolName, null, toolResult);
        } catch (Exception e) {
            log.warn("No se pudo registrar el turno de conversación de la empresa {}: {}", user.getEmpresaId(), e.getMessage());
        }
    }
}
