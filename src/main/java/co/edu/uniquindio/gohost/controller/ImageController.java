package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.service.image.ImageService;
import co.edu.uniquindio.gohost.service.image.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

/**
 * Endpoints para carga y eliminación de imágenes en la nube.
 * Requiere autenticación JWT.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Sube una imagen (multipart/form-data).
     * Campo esperado: file
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<ImageUploadResult> upload(@RequestPart("file") MultipartFile file) throws IOException {
        ImageUploadResult result = imageService.subirImagen(file);
        // Location apuntando al recurso en la nube (secureUrl)
        return ResponseEntity
                .created(URI.create(result.secureUrl() != null ? result.secureUrl() : result.url()))
                .body(result);
    }

    /**
     * Elimina una imagen por su public_id.
     * Ejemplo: DELETE /api/images/gohost/imagenes/abc123
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String publicId) throws IOException {
        imageService.eliminarImagen(publicId);
        return ResponseEntity.noContent().build();
    }
}