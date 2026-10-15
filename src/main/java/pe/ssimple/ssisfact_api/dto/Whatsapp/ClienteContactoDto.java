package pe.ssimple.ssisfact_api.dto.Whatsapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Proyección liviana de cliente para el envío de recordatorios programados por WhatsApp. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteContactoDto {
    private Long id;
    private String nombre;
    private String telefono;
}
