package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.dto.reservaDtos.ReservaResDTO;
import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reglas de negocio para la gestión de {@link Reserva}.
 * Convenciones:
 *  - Rango de fechas válido: in < out (intervalo semiabierto [in, out]).
 *  - La cancelación marca la reserva como CANCELADA (y, si aplica, eliminada=true en la entidad).
 */
public interface ReservaService {

    /**
     * Crea una reserva para un alojamiento y huésped.
     *
     * @param alojamientoId id del alojamiento
     * @param huespedId     id del huésped que reserva
     * @param in            fecha de check-in (inclusive)
     * @param out           fecha de check-out (exclusiva)
     * @return reserva creada (entidad)
     */
    Reserva crear(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out);

    /**
     * Crea una reserva y la retorna como DTO con alojamiento y fotos inicializados.
     *
     * <p>Úsala desde el controller para evitar exponer entidades JPA y prevenir
     * LazyInitializationException durante la serialización.</p>
     *
     * @param alojamientoId id del alojamiento
     * @param huespedId     id del huésped que reserva
     * @param in            fecha de check-in (inclusive)
     * @param out           fecha de check-out (exclusiva)
     * @return reserva creada como DTO
     */
    @Transactional
    ReservaResDTO crearConDTO(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out);

    /**
     * Lista reservas realizadas por un huésped como DTO.
     *
     * @param huespedId id del huésped
     * @param pageable  configuración de paginación
     * @return página de reservas del huésped
     */
    Page<ReservaResDTO> listarPorHuespedConDTO(UUID huespedId, Pageable pageable);

    /**
     * Lista reservas de los alojamientos de un anfitrión como DTO.
     *
     * @param anfitrionId id del anfitrión propietario
     * @param pageable    configuración de paginación
     * @return página de reservas de sus alojamientos
     */
    Page<ReservaResDTO> listarPorAnfitrionConDTO(UUID anfitrionId, Pageable pageable);

    /**
     * Actualiza fechas y/o estado de una reserva como DTO.
     * Solo se actualizan fechas si se envían ambas (in y out) y no hay traslape.
     *
     * @param id     id de la reserva
     * @param in     nueva fecha de check-in (opcional, requiere out también)
     * @param out    nueva fecha de check-out (opcional, requiere in también)
     * @param estado nuevo estado (opcional)
     * @return reserva actualizada como DTO
     */
    ReservaResDTO actualizarConDTO(UUID id, LocalDate in, LocalDate out, EstadoReserva estado);

    /**
     * Cancela una reserva (idempotente).
     * Convención: la implementación puede marcarla como CANCELADA (y opcionalmente eliminada=true).
     *
     * @param id id de la reserva
     */
    void cancelar(UUID id);

    /**
     * Obtiene una reserva por su id como DTO.
     *
     * @param id id de la reserva
     * @return reserva encontrada como DTO
     */
    ReservaResDTO obtenerConDTO(UUID id);

    /**
     * Obtiene una reserva por su id (entidad).
     *
     * @param id id de la reserva
     * @return entidad Reserva
     */
    @Transactional(readOnly = true)
    Reserva obtener(UUID id);
}