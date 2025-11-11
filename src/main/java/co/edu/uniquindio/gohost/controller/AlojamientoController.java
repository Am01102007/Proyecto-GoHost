package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.AlojamientoCreatedDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.AlojamientoResDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.CrearAlojDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.EditAlojDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.FiltroBusquedaDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.FiltroAvanzadoDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.MetricasAlojamientoDTO;
import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.security.AuthenticationHelper;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import co.edu.uniquindio.gohost.service.image.ImageService;
import co.edu.uniquindio.gohost.service.image.ImageUploadResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestión de alojamientos.
 * Nota: el id del anfitrión se obtiene del token (atributos en la request),
 * no desde la URL, para evitar suplantaciones.
 */
@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
@Slf4j
public class AlojamientoController {

    private final AlojamientoService service;

    private final AuthenticationHelper authHelper;

    private final ImageService imageService;

    /**
     * Crea un alojamiento para el anfitrión autenticado.
     * Requiere rol ANFITRION (validado automáticamente por Spring Security).
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlojamientoCreatedDTO> crear(
            @Valid @RequestPart("data") CrearAlojDTO dto,
            @RequestPart("files") MultipartFile[] files,
            HttpServletRequest request) throws java.io.IOException {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);

        try {
            if (files == null || files.length == 0) {
                throw new IllegalArgumentException("Debe enviar entre 1 y 10 imágenes");
            }
            if (files.length < 1 || files.length > 10) {
                throw new IllegalArgumentException("Cantidad de imágenes inválida: " + files.length);
            }

            List<String> urls = new ArrayList<>(files.length);
            for (MultipartFile f : files) {
                ImageUploadResult res = imageService.subirImagen(f);
                urls.add(res.secureUrl() != null ? res.secureUrl() : res.url());
            }

            CrearAlojDTO dtoConFotos = new CrearAlojDTO(
                    dto.titulo(),
                    dto.descripcion(),
                    dto.ciudad(),
                    dto.pais(),
                    dto.calle(),
                    dto.zip(),
                    dto.precioNoche(),
                    dto.capacidad(),
                    urls,
                    dto.servicios()
            );

            AlojamientoCreatedDTO creado = service.crearConDTO(anfitrionId, dtoConFotos);
            URI location = URI.create("/api/alojamientos/" + creado.id());
            return ResponseEntity.created(location).body(creado);
        } catch (IllegalArgumentException iae) {
            log.warn("Datos inválidos al crear alojamiento: {}", iae.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (java.io.IOException ioe) {
            log.error("Fallo de proveedor de imágenes al crear alojamiento: {}", ioe.getMessage(), ioe);
            return ResponseEntity.status(502).build();
        }
    }

    @PreAuthorize("hasRole('ANFITRION')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }

    /** Lista paginada de todos los alojamientos. */
    @GetMapping
    public Page<AlojamientoResDTO> listar(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    /** Lista alojamientos del anfitrión autenticado. */
    @PreAuthorize("hasRole('ANFITRION')")
    @GetMapping("/anfitrion")
    public Page<AlojamientoResDTO> porAnfitrion(HttpServletRequest request,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        return service.listarPorAnfitrion(anfitrionId, PageRequest.of(page, size));
    }

    /** Obtiene un alojamiento por su ID. */
    @GetMapping("/{id}")
    public AlojamientoResDTO obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    /** Actualiza parcialmente un alojamiento. */
    @PreAuthorize("hasRole('ANFITRION')")
    @PatchMapping("/{id}")
    public AlojamientoResDTO actualizar(@PathVariable UUID id, @Valid @RequestBody EditAlojDTO dto, HttpServletRequest request) {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        return service.actualizarConValidaciones(id, dto, anfitrionId);
    }

    /** Búsqueda con filtros (ciudad, capacidad) y paginación. */
    @PostMapping("/search")
    public Page<AlojamientoResDTO> buscar(@RequestBody FiltroBusquedaDTO f) {
        int page = f.page() == null ? 0 : f.page();
        int size = f.size() == null ? 10 : f.size();
        return service.buscar(f.ciudad(), f.capacidad(), PageRequest.of(page, size));
    }

    /** Búsqueda avanzada con múltiples filtros (fechas, precios, servicios). */
    @PostMapping("/search/advanced")
    public Page<AlojamientoResDTO> busquedaAvanzada(@Valid @RequestBody FiltroAvanzadoDTO filtro) {
        return service.busquedaAvanzada(filtro);
    }

    /** Obtiene todas las ciudades disponibles para búsqueda predictiva. */
    @GetMapping("/ciudades")
    public List<String> obtenerCiudades() {
        return service.obtenerCiudades();
    }

    /** Búsqueda predictiva de ciudades por término. */
    @GetMapping("/ciudades/search")
    public List<String> buscarCiudades(@RequestParam String termino) {
        return service.buscarCiudades(termino);
    }

    /**
     * Obtiene métricas de un alojamiento específico.
     * Solo el anfitrión propietario puede ver las métricas de su alojamiento.
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @GetMapping("/{id}/metricas")
    public MetricasAlojamientoDTO obtenerMetricas(@PathVariable UUID id, HttpServletRequest request) {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        return service.obtenerMetricasConValidacion(id, anfitrionId);
    }

    /**
     * Obtiene métricas de todos los alojamientos del anfitrión autenticado con filtros de fecha.
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @GetMapping("/metricas")
    public List<MetricasAlojamientoDTO> obtenerMetricasPorAnfitrion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletRequest request) {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        return service.obtenerMetricasPorAnfitrion(anfitrionId, fechaInicio, fechaFin);
    }

}
