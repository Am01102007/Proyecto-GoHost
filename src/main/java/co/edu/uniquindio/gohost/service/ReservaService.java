package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
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
     * Crear una reserva y retornar DTO (con alojamiento/fotos inicializados).
     *
     * @param huespedId id del huésped
     * @param dto datos de la reserva a crear
     * @return reserva creada como DTO
     */
    @Transactional
    ReservaResDTO crearConDTO(UUID huespedId, CrearReservaDTO dto);

    /**
     * Lista reservas realizadas por un huésped como DTO con filtros y ordenamiento.
     *
     * @param huespedId id del huésped
     * @param fechaInicio fecha de inicio para filtrar (opcional)
     * @param fechaFin fecha de fin para filtrar (opcional)
     * @param estado estado de la reserva para filtrar (opcional)
     * @param pageable  configuración de paginación
     * @return página de reservas del huésped ordenadas por fecha más reciente
     */
    Page<ReservaResDTO> listarPorHuespedConDTO(UUID huespedId, LocalDate fechaInicio, LocalDate fechaFin, EstadoReserva estado, Pageable pageable);

    /**
     * Lista reservas de los alojamientos de un anfitrión como DTO.
     *
     * @param anfitrionId id del anfitrión propietario
     * @param pageable    configuración de paginación
     * @return página de reservas de sus alojamientos
     */
    Page<ReservaResDTO> listarPorAnfitrionConDTO(UUID anfitrionId, Pageable pageable);

    /**
     * Lista reservas de un alojamiento específico como DTO.
     * Valida que el anfitrión autenticado sea propietario del alojamiento.
     *
     * @param alojamientoId id del alojamiento
     * @param anfitrionId   id del anfitrión autenticado (para validación)
     * @param pageable      configuración de paginación
     * @return página de reservas del alojamiento
     */
    Page<ReservaResDTO> listarPorAlojamientoConDTO(UUID alojamientoId, UUID anfitrionId, Pageable pageable);

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