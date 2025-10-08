package co.edu.uniquindio.gohost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * CloudinaryProperties — Configuración del servicio de almacenamiento de imágenes.
 * ============================================================================
 *
 * Lee las propiedades desde el archivo application.yml bajo el prefijo `nube.imagenes`.
 * Permite definir credenciales, carpeta y tamaño máximo de los archivos subidos.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nube.imagenes")
public class CloudinaryProperties {

    /** Habilita o deshabilita el servicio de carga en la nube. */
    private boolean habilitado = true;

    /** Nombre del “cloud” asignado por Cloudinary. */
    private String nombreNube;

    /** Clave pública (API Key). */
    private String claveApi;

    /** Clave secreta (API Secret). */
    private String claveSecreta;

    /** Carpeta remota donde se almacenarán las imágenes. */
    private String carpeta = "gohost/imagenes";

    /** Tamaño máximo permitido (en bytes). Por defecto 5MB. */
    private long tamanoMaximo = 5 * 1024 * 1024;
}