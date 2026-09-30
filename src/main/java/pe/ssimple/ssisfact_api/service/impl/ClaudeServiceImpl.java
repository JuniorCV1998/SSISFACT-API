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
import pe.ssimple.ssisfact_api.service.AnthropicClient;
import pe.ssimple.ssisfact_api.service.AssistantCarritoService;
import pe.ssimple.ssisfact_api.service.AssistantMemoryService;
import pe.ssimple.ssisfact_api.service.AssistantToolCatalog;
import pe.ssimple.ssisfact_api.service.ClaudeService;
import pe.ssimple.ssisfact_api.service.CustomUserDetails;
import pe.ssimple.ssisfact_api.service.ToolExecutorService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    private final AssistantConversationLogRepository assistantConversationLogRepository;

    @Value("${anthropic.model}")
    private String model;

    @Value("${assistant.max-tool-roundtrips}")
    private int maxToolRoundtrips;

    @Override
    public AssistantChatResponse chat(CustomUserDetails user, String mensaje, String moduloActivo) {

        EmpresaActualResponse empresa = empresaRepository.obtenerActual(user.getEmpresaId());
        String nombreAsistente = empresa != null && empresa.getAsistenteNombre() != null
                ? empresa.getAsistenteNombre() : NOMBRE_ASISTENTE_DEFAULT;

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
                            block.getName(), block.getInput(), block.getId(), user);
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

    private String construirSystemPromptEstable(CustomUserDetails user, String moduloActivo,
                                                  EmpresaActualResponse empresa, String nombreAsistente) {

        String nombreEmpresa = empresa != null ? empresa.getNombre() : "";
        String ruc = empresa != null ? empresa.getRuc() : "";
        String nombreUsuario = user.getNombre() != null ? user.getNombre() : user.getUsername();
        String memoria = assistantMemoryService.getMemoryContext(user.getEmpresaId());
        String tono = instruccionTono(empresa != null ? empresa.getAsistenteTono() : null);

        return """
                Eres %s, el asistente inteligente del facturador Ssimple Fact.
                Ayudas a los usuarios peruanos a gestionar su negocio de forma rápida y natural.

                EMPRESA ACTIVA: %s (RUC: %s)
                USUARIO: %s
                MÓDULO: %s

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
                - Cuando el usuario diga algo equivalente a "emite venta", "cierra el pedido" o "eso es todo", SIEMPRE debes llamar a la herramienta emitir_pedido de inmediato, sin pedir confirmación adicional (no es una acción destructiva). NUNCA respondas en texto que el pedido está vacío, que hay que confirmar datos o que no hay nada que emitir sin haber llamado primero a emitir_pedido — el bloque PEDIDO EN CURSO puede no reflejar cambios recién hechos en el mismo mensaje, así que la herramienta emitir_pedido es la única fuente de verdad y ella misma te dirá si está vacío.
                - Si buscar_producto o agregar_al_pedido devuelven varias coincidencias ambiguas, pregunta al usuario cuál es antes de agregarlo.

                FORMATO DE RESPUESTA TRAS EJECUTAR ACCIÓN:
                "✓ [acción realizada]: [detalle breve]"
                Ejemplo: "✓ Venta registrada: 3 productos · S/.45.50"
                """.formatted(nombreAsistente, nombreEmpresa, ruc, nombreUsuario, moduloActivo == null ? "" : moduloActivo, memoria, tono);
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
