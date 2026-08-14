package pe.ssimple.ssisfact_api.dto.Sunat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatJobPollResponse {
    private String jobId;
    private String status;
    private SunatBotResponse result;
}
