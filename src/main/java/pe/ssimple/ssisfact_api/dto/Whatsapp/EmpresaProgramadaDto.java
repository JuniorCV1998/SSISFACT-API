package pe.ssimple.ssisfact_api.dto.Whatsapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Empresa que, en el minuto actual, debe disparar su recordatorio programado por WhatsApp. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaProgramadaDto {
    private Long empresaId;
    private String mensaje;
}
