package pe.ssimple.ssisfact_api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import pe.ssimple.ssisfact_api.dto.ApiResponse;
import pe.ssimple.ssisfact_api.dto.Sunat.SunatApiResponse;
import pe.ssimple.ssisfact_api.util.SqlErrorMapper;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SunatAuthenticationException.class)
    public ResponseEntity<SunatApiResponse<Void>>
    handleSunatAuthentication(SunatAuthenticationException ex) {

        log.warn("SUNAT: credenciales inválidas");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(SunatApiResponse.failure("CREDENCIALES_INVALIDAS", ex.getMessage()));
    }

    @ExceptionHandler(SunatManualReviewException.class)
    public ResponseEntity<SunatApiResponse<Void>>
    handleSunatManualReview(SunatManualReviewException ex) {

        log.warn("SUNAT: requiere revisión manual");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(SunatApiResponse.failure("REQUIERE_REVISION_MANUAL", ex.getMessage()));
    }

    @ExceptionHandler(SunatUnavailableException.class)
    public ResponseEntity<SunatApiResponse<Void>>
    handleSunatUnavailable(SunatUnavailableException ex) {

        log.error("SUNAT: servicio de automatización no disponible");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(SunatApiResponse.failure("SERVICIO_SUNAT_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(SunatCredentialsNotConfiguredException.class)
    public ResponseEntity<SunatApiResponse<Void>>
    handleSunatCredentialsNotConfigured(SunatCredentialsNotConfiguredException ex) {

        log.warn("SUNAT: credenciales no configuradas para la empresa");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SunatApiResponse.failure("CREDENCIALES_NO_CONFIGURADAS", ex.getMessage()));
    }

    @ExceptionHandler(VentaValidationException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleVentaValidation(VentaValidationException ex) {

        log.warn("Venta: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DocumentoValidationException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleDocumentoValidation(DocumentoValidationException ex) {

        log.warn("Documento: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ImagenValidationException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleImagenValidation(ImagenValidationException ex) {

        log.warn("Imagen: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {

        log.warn("Imagen: archivo demasiado grande");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("La imagen supera el tamaño máximo permitido (5MB)"));
    }

    @ExceptionHandler(AccesoSucursalDenegadoException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleAccesoSucursalDenegado(AccesoSucursalDenegadoException ex) {

        log.warn("Acceso a sucursal denegado: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleNoHandlerFound(NoHandlerFoundException ex) {

        log.warn("Ruta no encontrada: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("El recurso solicitado no existe"));
    }

    // Spring 6.1+/Boot 3.2+: cuando no hay handler, ResourceHttpRequestHandler
    // lanza esto (no NoHandlerFoundException) al no encontrar el recurso estático.
    // Es la excepción real que se dispara para cualquier ruta no mapeada.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleNoResourceFound(NoResourceFoundException ex) {

        log.warn("Ruta no encontrada: {} {}", ex.getHttpMethod(), ex.getResourcePath());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("El recurso solicitado no existe"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleValidationExceptions(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> "El campo '" + error.getField() + "' " + error.getDefaultMessage())
                .orElse("Error de validación");

        log.warn("Error de validación: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBadCredentials(BadCredentialsException ex) {

        log.warn("Credenciales inválidas: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "Usuario o contraseña incorrectos"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleDataAccessException(DataAccessException ex) {

        Throwable cause = ex.getMostSpecificCause();
        int errorCode = SqlErrorMapper.extractCode(cause.getMessage());
        log.error("[DataAccessException] tipo={} codigo={} causa='{}'",
                ex.getClass().getSimpleName(), errorCode, cause.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(SqlErrorMapper.toSafeMessage(cause.getMessage())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleRuntimeException(RuntimeException ex) {

        log.error("[RuntimeException] tipo={} mensaje='{}'", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleException(Exception ex) {

        log.error("[Exception] tipo={} mensaje='{}'", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Ocurrió un error interno"));
    }
}