package pe.ssimple.ssisfact_api.util;

import org.springframework.http.ResponseEntity;
import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.SpResponse;

import java.util.Arrays;

public class ResponseBuilder {

    private ResponseBuilder() {}

    public static <T extends SpResponse> ResponseEntity<ApiResponse<T>> build(
            T response, String... successStates) {

        boolean isSuccess = Arrays.asList(successStates).contains(response.getEstado());

        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.success(response.getMensaje(), response));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error(response.getMensaje()));
    }
}
