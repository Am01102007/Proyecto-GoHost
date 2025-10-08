package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.config.GeocodingProperties;
import co.edu.uniquindio.gohost.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * GeocodingServiceImpl — Implementación del servicio de geocodificación.
 * =============================================================================
 *
 * Este servicio obtiene coordenadas geográficas (latitud, longitud) a partir de
 * una dirección textual. Utiliza como proveedor por defecto Nominatim
 * (OpenStreetMap) y lee los parámetros desde la clase de configuración
 * {@link GeocodingProperties}.
 *
 * Características principales:
 *  - Permite habilitar o deshabilitar el servicio desde configuración.
 *  - Aplica timeouts configurables para evitar bloqueos de red.
 *  - Implementa reintentos automáticos en caso de fallos temporales.
 *  - Usa encabezados HTTP requeridos por los Términos de Servicio de Nominatim.
 *  - Devuelve null en lugar de lanzar excepciones, para no interrumpir el flujo
 *    de negocio en caso de error externo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final GeocodingProperties props;

    /**
     * Construye un RestTemplate con timeouts configurados.
     * Este método se utiliza en cada ejecución para garantizar que los valores
     * de timeout definidos en application.yml se apliquen correctamente.
     *
     * @return RestTemplate con timeouts de conexión y lectura configurados.
     */
    private RestTemplate buildRestTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeoutMs());
        factory.setReadTimeout(props.getTimeoutMs());
        return new RestTemplate(factory);
    }

    /**
     * Obtiene coordenadas geográficas a partir de una dirección, ciudad y país.
     * La implementación es resiliente a fallos del servicio externo y no lanza
     * excepciones controladas.
     *
     * @param direccion Texto de dirección principal (puede ser null o vacío).
     * @param ciudad    Ciudad opcional.
     * @param pais      País opcional.
     * @return Objeto {@code Coordenadas} con latitud y longitud, o null si no
     *         hay resultados o si la geocodificación está deshabilitada.
     */
    @Override
    public Coordenadas obtenerCoordenadas(String direccion, String ciudad, String pais) {
        // Validación inicial: verificar si la geocodificación está activa.
        if (!props.isEnabled()) {
            log.info("Geocoding deshabilitado mediante configuración (application.yml)");
            return null;
        }

        // Composición robusta de la query: une dirección, ciudad y país si existen.
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(direccion)) sb.append(direccion.trim());
        if (StringUtils.hasText(ciudad)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ciudad.trim());
        }
        if (StringUtils.hasText(pais)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(pais.trim());
        }

        String q = sb.toString();
        if (!StringUtils.hasText(q)) {
            log.warn("Geocoding: parámetros vacíos (direccion/ciudad/pais). No se realiza consulta.");
            return null;
        }

        // Construcción del URI para el proveedor definido en la configuración.
        URI uri = UriComponentsBuilder.fromHttpUrl(props.getBaseUrl())
                .queryParam("q", q)
                .queryParam("format", "json")
                .queryParam("limit", props.getLimit())
                .queryParam("accept-language", props.getLanguage())
                .build(true)
                .toUri();

        // Preparación de cabeceras HTTP. Nominatim requiere un User-Agent identificable.
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, props.getUserAgent());
        headers.set(HttpHeaders.REFERER, props.getReferer());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        // Si el proveedor requiere autenticación mediante API key, se agrega al header.
        if (StringUtils.hasText(props.getApiKey())) {
            headers.set("Authorization", "Bearer " + props.getApiKey());
        }

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        RestTemplate http = buildRestTemplate();

        // Se realizan varios intentos de consulta, según el valor configurado.
        int intentos = Math.max(0, props.getRetries()) + 1;
        for (int i = 1; i <= intentos; i++) {
            try {
                // Ejecución de la solicitud HTTP.
                ResponseEntity<List<Map<String, Object>>> res = http.exchange(
                        uri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {}
                );

                // Validación del estado de la respuesta HTTP.
                if (!res.getStatusCode().is2xxSuccessful()) {
                    log.warn("Geocoding: HTTP {} para '{}'", res.getStatusCodeValue(), q);
                    continue;
                }

                List<Map<String, Object>> body = res.getBody();
                if (body == null || body.isEmpty()) {
                    log.info("Geocoding: sin resultados para '{}'", q);
                    return null;
                }

                // Parseo del primer resultado. Los valores vienen como String.
                Map<String, Object> item = body.get(0);
                double lat = Double.parseDouble(String.valueOf(item.get("lat")));
                double lon = Double.parseDouble(String.valueOf(item.get("lon")));

                log.debug("Geocoding exitoso: '{}' → lat={}, lon={}", q, lat, lon);
                return new Coordenadas(lat, lon);

            } catch (RestClientException ex) {
                // Error típico de conexión o timeout. Se reintenta si quedan intentos.
                log.warn("Geocoding fallo intento {}/{} para '{}': {}", i, intentos, q, ex.getMessage());
                try {
                    Thread.sleep(150L * i); // Pequeño backoff incremental entre reintentos.
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception ex) {
                // Cualquier otro error inesperado (JSON parsing, etc.)
                log.error("Error inesperado en geocodificación de '{}'", q, ex);
                return null;
            }
        }

        // Si se agotaron los intentos, se devuelve null.
        log.warn("Geocoding agotó {} intentos sin éxito para '{}'", intentos, q);
        return null;
    }
}