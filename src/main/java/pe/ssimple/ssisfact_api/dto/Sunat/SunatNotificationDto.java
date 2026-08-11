package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatNotificationDto {
    private String id;
    private String asunto;
    @JsonProperty("fechaPublicacion")
    private String fechaPublicacion;
    @JsonProperty("categoriaCodigo")
    private String categoriaCodigo;
    @JsonProperty("categoriaDescripcion")
    private String categoriaDescripcion;
    private boolean leido;
    private boolean destacado;
    private boolean urgente;
    @JsonProperty("tieneAdjunto")
    private boolean tieneAdjunto;
}
