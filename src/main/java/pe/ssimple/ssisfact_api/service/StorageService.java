package pe.ssimple.ssisfact_api.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    // Sube el archivo a R2 bajo una carpeta lógica (ej. "productos/{empresaId}")
    // y devuelve la URL pública final.
    String subirImagen(MultipartFile file, String carpeta);

    // Borra un objeto de R2 a partir de su URL pública. No lanza si la URL no
    // pertenece a nuestro bucket o si el borrado falla — es un best-effort de limpieza.
    void eliminarImagen(String imagenUrl);
}
