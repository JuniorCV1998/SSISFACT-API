package pe.ssimple.ssisfact_api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.ssimple.ssisfact_api.config.R2Properties;
import pe.ssimple.ssisfact_api.exception.ImagenValidationException;
import pe.ssimple.ssisfact_api.service.StorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final S3Client r2Client;
    private final R2Properties r2Properties;

    @Override
    public String subirImagen(MultipartFile file, String carpeta) {

        if (file == null || file.isEmpty()) {
            throw new ImagenValidationException("Debes seleccionar una imagen");
        }

        String contentType = file.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new ImagenValidationException("Formato de imagen no permitido (solo JPG, PNG o WEBP)");
        }

        if (r2Properties.getPublicBaseUrl() == null || r2Properties.getPublicBaseUrl().isBlank()) {
            throw new ImagenValidationException("El almacenamiento de imágenes no está configurado todavía");
        }

        String extension = switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        String key = carpeta + "/" + UUID.randomUUID() + "." + extension;

        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(r2Properties.getBucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("[subirImagen] Error leyendo el archivo — key={}", key, e);
            throw new ImagenValidationException("No se pudo leer el archivo enviado");
        } catch (S3Exception e) {
            log.error("[subirImagen] Error subiendo a R2 — key={} mensaje='{}'", key, e.getMessage(), e);
            throw new ImagenValidationException("No se pudo subir la imagen, intenta nuevamente");
        }

        String base = r2Properties.getPublicBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + key;
    }

    @Override
    public void eliminarImagen(String imagenUrl) {

        if (imagenUrl == null || imagenUrl.isBlank()) {
            return;
        }

        String base = r2Properties.getPublicBaseUrl();
        if (base == null || base.isBlank() || !imagenUrl.startsWith(base)) {
            return; // No es una URL de nuestro bucket, no tocar
        }

        String prefix = base.endsWith("/") ? base : base + "/";
        String key = imagenUrl.substring(prefix.length());

        try {
            r2Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(r2Properties.getBucket())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            log.warn("[eliminarImagen] No se pudo borrar la imagen anterior — key={} mensaje='{}'", key, e.getMessage());
        }
    }
}
