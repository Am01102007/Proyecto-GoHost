package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.dto.reservaDtos.ReservaResDTO;
import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación JPA de {@link ReservaService}.
 * Reglas:
 *  - Rango válido: in < out (intervalo semiabierto [in, out)).
 *  - No traslapar con reservas activas (no eliminadas y no CANCELADAS).
 *  - No modificar reservas eliminadas o CANCELADAS.
 */
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final AlojamientoRepository alojRepo;

    /** Crear una reserva nueva (retorna ENTIDAD). */
    @Override
    @Transactional
    public Reserva crear(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out) {
        validarRango(in, out);

        if (repo.existsTraslape(alojamientoId, in, out)) {
            throw new IllegalStateException("Fechas no disponibles");
        }

        var huesped = usuarioRepo.findById(huespedId)
                .orElseThrow(() -> new EntityNotFoundException("Huésped no existe"));

        var alojamiento = alojRepo.findById(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));

        return repo.save(Reserva.builder()
                .huesped(huesped)
                .alojamiento(alojamiento)
                .checkIn(in)
                .checkOut(out)
                .estado(EstadoReserva.PENDIENTE)
                .eliminada(false)
                .build());
    }

    /** Crear una reserva y retornar DTO (con alojamiento/fotos inicializados). */
    @Override
    @Transactional
    public ReservaResDTO crearConDTO(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out) {
        // 1) crear entidad con la lógica existente
        Reserva creada = crear(alojamientoId, huespedId, in, out);

        // 2) recargar con JOIN FETCH para inicializar alojamiento.fotos (evita LazyInitializationException)
        Reserva completa = repo.findByIdWithFotos(creada.getId())
                .orElseThrow(() -> new EntityNotFoundException("Reserva recién creada no encontrada"));

        // 3) mapear a DTO
        return toRes(completa);
    }

    /** Listar reservas del huésped autenticado como DTO. */
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResDTO> listarPorHuespedConDTO(UUID huespedId, Pageable pageable) {
        return repo.findByHuespedIdWithFotos(huespedId, pageable).map(this::toRes);
    }

    /** Listar reservas de los alojamientos del anfitrión autenticado como DTO. */
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResDTO> listarPorAnfitrionConDTO(UUID anfitrionId, Pageable pageable) {
        return repo.findByAlojamientoAnfitrionIdWithFotos(anfitrionId, pageable).map(this::toRes);
    }

    /** Obtener una reserva por ID como DTO. */
    @Override
    @Transactional(readOnly = true)
    public ReservaResDTO obtenerConDTO(UUID id) {
        Reserva r = repo.findByIdWithFotos(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        return toRes(r);
    }

    /** Actualizar una reserva y devolver DTO. */
    @Override
    @Transactional
    public ReservaResDTO actualizarConDTO(UUID id, LocalDate in, LocalDate out, EstadoReserva estado) {
        Reserva actualizada = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        if (actualizada.isEliminada() || actualizada.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva cancelada/eliminada no puede modificarse");
        }

        if (in != null && out != null) {
            validarRango(in, out);
            if (repo.existsTraslape(actualizada.getAlojamiento().getId(), in, out)) {
                throw new IllegalStateException("Fechas no disponibles");
            }
            actualizada.setCheckIn(in);
            actualizada.setCheckOut(out);
        }

        if (estado != null) {
            actualizada.setEstado(estado);
        }

        repo.save(actualizada);
        return toRes(repo.findByIdWithFotos(actualizada.getId()).orElseThrow());
    }

    /** Cancelar (soft delete + estado CANCELADA). Idempotente. */
    @Override
    @Transactional
    public void cancelar(UUID id) {
        var r = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        if (r.getEstado() == EstadoReserva.CANCELADA && r.isEliminada()) {
            return; // idempotente
        }
        r.setEstado(EstadoReserva.CANCELADA);
        r.setEliminada(true);
        repo.save(r);
    }

    /** Obtener una reserva por ID (entidad). */
    @Transactional(readOnly = true)
    @Override
    public Reserva obtener(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
    }

    /** Utilidad: valida que in < out. */
    private void validarRango(LocalDate in, LocalDate out) {
        if (in == null || out == null || !out.isAfter(in)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
    }

    /* =========================================================
       MAPEO MANUAL: Reserva → ReservaResDTO
       ========================================================= */

    private ReservaResDTO toRes(Reserva r) {
        return new ReservaResDTO(
                r.getId(),
                r.getCheckIn(),
                r.getCheckOut(),
                r.getEstado() != null ? EstadoReserva.valueOf(r.getEstado().name()) : null,
                r.isEliminada(),
                r.getHuesped() != null ? r.getHuesped().getId() : null,
                r.getHuesped() != null ? r.getHuesped().getNombre() : null, // asume getNombre()
                r.getAlojamiento() != null ? r.getAlojamiento().getId() : null,
                r.getAlojamiento() != null ? r.getAlojamiento().getTitulo() : null,
                (r.getAlojamiento() != null && r.getAlojamiento().getDireccion() != null)
                        ? r.getAlojamiento().getDireccion().getCiudad()
                        : "Sin ciudad"
        );
    }
}