package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatMessageListResponse {
    private int pagina;
    @JsonProperty("tamanioPagina")
    private int tamanioPagina;
    private long total;
    @JsonProperty("totalPaginas")
    private int totalPaginas;
    private List<SunatMessageDto> mensajes;
    @JsonProperty("ultimaSincronizacion")
    private Instant ultimaSincronizacion;
}
