package pe.ssimple.ssisfact_api.dto.Sunat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SunatApiResponse<T> {
    private boolean success;
    private String status;
    private String message;
    private String source;
    private T data;

    public static <T> SunatApiResponse<T> success(T data, String source) {
        return SunatApiResponse.<T>builder()
                .success(true)
                .source(source)
                .data(data)
                .build();
    }

    public static <T> SunatApiResponse<T> failure(String status, String message) {
        return SunatApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .build();
    }
}
