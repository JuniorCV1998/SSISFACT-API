package pe.ssimple.ssisfact_api.dto.Whatsapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappVincularResponse {
    private String qrBase64;
    private String pairingCode;
}
