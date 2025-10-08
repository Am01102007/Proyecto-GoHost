package co.edu.uniquindio.gohost.service.image;

/**
 * =============================================================================
 * ImageUploadResult — Resultado de una carga exitosa de imagen.
 * =============================================================================
 *
 * Representa la información esencial devuelta por el proveedor en la nube
 * (por ejemplo, Cloudinary), tras subir una imagen.
 *
 * Campos:
 *  - publicId: identificador único del recurso en la nube.
 *  - url: URL de acceso público (HTTP).
 *  - secureUrl: URL de acceso seguro (HTTPS).
 *  - width / height: dimensiones en píxeles.
 *  - format: formato del archivo (jpg, png, etc.).
 *  - bytes: tamaño del archivo en bytes.
 */
public record ImageUploadResult(
        String publicId,   // Identificador único en Cloudinary
        String url,        // URL pública
        String secureUrl,  // URL segura HTTPS
        Integer width,     // Ancho en píxeles
        Integer height,    // Alto en píxeles
        String format,     // Formato de la imagen
        Long bytes         // Tamaño en bytes
) {}