package co.edu.uniquindio.gohost.service.image;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * =============================================================================
 * ImageService — Servicio para gestión de imágenes en la nube.
 * =============================================================================
 *
 * Responsabilidades:
 *  - Subir imágenes a un servicio de terceros (Por ejemplo: Cloudinary).
 *  - Eliminar imágenes previamente almacenadas.
 *  - Validar tamaño y formato de los archivos recibidos.
 */
public interface ImageService {

    /**
     * Sube una imagen al servicio en la nube y retorna la información del recurso.
     *
     * @param archivo archivo recibido en formato multipart/form-data
     * @return resultado con información del recurso almacenado (URL, tamaño, etc.)
     * @throws IOException si ocurre un error al leer el archivo o al contactar el servicio
     * @throws IllegalArgumentException si el archivo es nulo, vacío o no es válido
     */
    ImageUploadResult subirImagen(MultipartFile archivo) throws IOException;

    /**
     * Elimina una imagen del servicio remoto por su identificador público.
     *
     * @param idPublico identificador único asignado por el servicio (public_id)
     * @throws IOException si ocurre un error de comunicación con el servicio
     * @throws IllegalArgumentException si el id es nulo o vacío
     */
    void eliminarImagen(String idPublico) throws IOException;
}