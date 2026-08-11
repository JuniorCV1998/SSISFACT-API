package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SunatBotResponse {
    private boolean success;
    private String action;
    private String status;
    private String message;
    private SunatBotData data;
    private String startedAt;
    private String finishedAt;
}
