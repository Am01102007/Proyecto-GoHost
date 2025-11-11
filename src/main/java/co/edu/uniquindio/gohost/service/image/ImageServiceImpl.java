package co.edu.uniquindio.gohost.service.image;

import co.edu.uniquindio.gohost.config.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * =============================================================================
 * ImageServiceImpl — Implementación del servicio de imágenes con Cloudinary.
 * =============================================================================
 *
 * Requisitos:
 *   - Dependencia Maven:
 *       <dependency>
 *         <groupId>com.cloudinary</groupId>
 *         <artifactId>cloudinary-http44</artifactId>
 *         <version>1.39.0</version>
 *       </dependency>
 *
 * Propiedades externas:
 *   - Definidas en {@link CloudinaryProperties} con prefijo "nube.imagenes" en application.yml.
 *
 * Validaciones:
 *   - Tamaño máximo (si tamanoMaximo > 0).
 *   - Tipo de contenido permitido: JPEG/PNG/WebP/GIF.
 *
 * Errores:
 *   - Lanza IllegalArgumentException para entradas inválidas del cliente.
 *   - Lanza IOException cuando ocurre un problema de E/S o del proveedor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            MimeTypeUtils.IMAGE_JPEG_VALUE, // image/jpeg
            "image/jpg",
            MimeTypeUtils.IMAGE_PNG_VALUE,  // image/png
            "image/webp",
            MimeTypeUtils.IMAGE_GIF_VALUE   // image/gif
    );

    private final CloudinaryProperties props;

    /**
     * Construye un cliente de Cloudinary a partir de las propiedades.
     * Se crea bajo demanda para evitar mantener credenciales en memoria
     * si el servicio está deshabilitado.
     */
    private Cloudinary buildClient() {
        if (!props.isHabilitado()) {
            throw new IllegalStateException("Servicio de imágenes deshabilitado por configuración");
        }
        if (!StringUtils.hasText(props.getNombreNube())
                || !StringUtils.hasText(props.getClaveApi())
                || !StringUtils.hasText(props.getClaveSecreta())) {
            throw new IllegalStateException("Credenciales de Cloudinary incompletas");
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", props.getNombreNube(),
                "api_key", props.getClaveApi(),
                "api_secret", props.getClaveSecreta(),
                "secure", true
        ));
    }

    /**
     * Valida el archivo recibido según tamaño y tipo de contenido.
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se recibió archivo o está vacío");
        }

        long max = props.getTamanoMaximo();
        if (max > 0 && archivo.getSize() > max) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido: " + max + " bytes");
        }

        String contentType = archivo.getContentType();
        if (!StringUtils.hasText(contentType) || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + contentType);
        }
    }

    @Override
    public ImageUploadResult subirImagen(MultipartFile archivo) throws IOException {
        validarArchivo(archivo);

        try {
            Cloudinary cloudinary = buildClient();

            // Opciones de subida: carpeta, recurso de tipo imagen, no sobrescribir por defecto.
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", StringUtils.hasText(props.getCarpeta()) ? props.getCarpeta() : "gohost/imagenes",
                    "resource_type", "image",
                    "overwrite", false
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader()
                    .upload(archivo.getBytes(), options);

            String publicId  = (String) result.get("public_id");
            String url       = (String) result.get("url");
            String secureUrl = (String) result.get("secure_url");
            Integer width    = safeInt(result.get("width"));
            Integer height   = safeInt(result.get("height"));
            String format    = (String) result.get("format");
            Long bytes       = safeLong(result.get("bytes"));

            if (!StringUtils.hasText(publicId) || !StringUtils.hasText(secureUrl)) {
                throw new IOException("Respuesta inválida del proveedor de imágenes");
            }

            log.info("Imagen subida correctamente. public_id={}, bytes={}", publicId, bytes);
            return new ImageUploadResult(publicId, url, secureUrl, width, height, format, bytes);

        } catch (IllegalArgumentException iae) {
            // Entradas inválidas (tamaño, tipo, credenciales incompletas).
            throw iae;
        } catch (Exception ex) {
            log.error("Error subiendo imagen: {}", ex.getMessage(), ex);
            if (ex instanceof IOException io) {
                throw io;
            }
            // Propaga detalle del proveedor para diagnóstico (mapeado a 502 por el handler)
            throw new IOException("Error del proveedor de imágenes: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminarImagen(String idPublico) throws IOException {
        if (!StringUtils.hasText(idPublico)) {
            throw new IllegalArgumentException("El id público (public_id) es requerido");
        }

        try {
            Cloudinary cloudinary = buildClient();

            @SuppressWarnings("unchecked")
            Map<String, Object> res = (Map<String, Object>) cloudinary.uploader()
                    .destroy(idPublico, ObjectUtils.emptyMap());

            Object outcome = res.get("result"); // valores típicos: "ok", "not found"
            log.info("Resultado de eliminación de imagen '{}': {}", idPublico, outcome);

            // No se lanza excepción si no existe; se considera idempotente.
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception ex) {
            log.error("Error eliminando imagen '{}': {}", idPublico, ex.getMessage(), ex);
            if (ex instanceof IOException io) {
                throw io;
            }
            throw new IOException("Error del proveedor de imágenes al eliminar: " + ex.getMessage(), ex);
        }
    }

    private Integer safeInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long safeLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
