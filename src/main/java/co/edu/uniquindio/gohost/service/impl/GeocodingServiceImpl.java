package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.config.GeocodingProperties;
import co.edu.uniquindio.gohost.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * GeocodingServiceImpl — Implementación de geocodificación (Nominatim por defecto)
 * =============================================================================
 *
 * - Lee configuración desde {@link GeocodingProperties} (application.yml → geocoding.*)
 * - Usa Nominatim (OpenStreetMap) por defecto, sin API key.
 * - Arma una consulta con direccion + ciudad + pais cuando estén presentes.
 * - Devuelve null si no se encuentran coordenadas.
 *
 * Buenas prácticas:
 * - Establece un User-Agent identificable (requerido por TOS de Nominatim).
 * - No lanza excepciones checked; loggea y retorna null en fallas recuperables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final GeocodingProperties props;

    // RestTemplate simple y suficiente para esta integración
    private final RestTemplate http = new RestTemplate();

    /**
     * Obtiene coordenadas geográficas a partir de una dirección.
     *
     * @param direccion Dirección base (obligatoria).
     * @param ciudad    Ciudad (opcional).
     * @param pais      País (opcional).
     * @return Coordenadas(lat, lon) o null si no hay coincidencias.
     */
    @Override
    public Coordenadas obtenerCoordenadas(String direccion, String ciudad, String pais) {
        try {
            if (!StringUtils.hasText(direccion)) {
                log.warn("Geocoding: dirección vacía, no se consulta proveedor");
                return null;
            }

            // 1) Componer texto de búsqueda (mejor precisión si hay ciudad/pais)
            StringBuilder query = new StringBuilder(direccion.trim());
            if (StringUtils.hasText(ciudad)) query.append(", ").append(ciudad.trim());
            if (StringUtils.hasText(pais))   query.append(", ").append(pais.trim());
            String q = query.toString();

            // 2) Construir URI del proveedor (Nominatim por defecto)
            URI uri = UriComponentsBuilder.fromHttpUrl(props.getBaseUrl())
                    .queryParam("q", q)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build(true)
                    .toUri();

            // 3) Headers (User-Agent requerido por Nominatim)
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.USER_AGENT, "gohost-app/1.0 (contacto: soporte@gohost.com)");

            // Si en el futuro se usa proveedor con API key
            if (StringUtils.hasText(props.getApiKey())) {
                // Ejemplos:
                // headers.set("Authorization", "Bearer " + props.getApiKey());
                // o añadir como query param en la base-url según proveedor.
            }

            // 4) Llamada HTTP
            ResponseEntity<List<Map<String, Object>>> res = http.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null || res.getBody().isEmpty()) {
                log.info("Geocoding: sin resultados para '{}'", q);
                return null;
            }

            // 5) Parseo mínimo (lat/lon vienen como String)
            Map<String, Object> item = res.getBody().get(0);
            double lat = Double.parseDouble(String.valueOf(item.get("lat")));
            double lon = Double.parseDouble(String.valueOf(item.get("lon")));

            log.debug("Geocoding: '{}' → lat={}, lon={}", q, lat, lon);
            return new Coordenadas(lat, lon);

        } catch (Exception e) {
            log.warn("Geocoding: fallo consultando '{}': {}", direccion, e.getMessage());
            return null; // falla recuperable: no romper el flujo de negocio
        }
    }
}