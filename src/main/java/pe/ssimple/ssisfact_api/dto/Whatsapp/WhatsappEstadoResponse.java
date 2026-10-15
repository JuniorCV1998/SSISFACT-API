package pe.ssimple.ssisfact_api.dto.Whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappEstadoResponse {
    // DESCONECTADO | CONECTANDO | CONECTADO
    private String estado;
    private String numero;
}
