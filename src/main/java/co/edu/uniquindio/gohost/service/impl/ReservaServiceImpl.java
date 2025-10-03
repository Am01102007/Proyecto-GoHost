package co.edu.uniquindio.gohost.service.impl;

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

    /** Crear una reserva nueva. */
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

    /** Listar reservas del huésped autenticado. */
    @Override
    @Transactional(readOnly = true)
    public Page<Reserva> listarPorHuesped(UUID huespedId, Pageable pageable) {
        return repo.findByHuespedId(huespedId, pageable);
    }

    /** Listar reservas de los alojamientos del anfitrión autenticado. */
    @Override
    @Transactional(readOnly = true)
    public Page<Reserva> listarPorAnfitrion(UUID anfitrionId, Pageable pageable) {
        return repo.findByAlojamientoAnfitrionId(anfitrionId, pageable);
    }

    /** Actualizar fechas y/o estado de una reserva. */
    @Override
    @Transactional
    public Reserva actualizar(UUID id, LocalDate in, LocalDate out, EstadoReserva estado) {
        var r = obtener(id);

        if (Boolean.TRUE.equals(r.isEliminada()) || r.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva cancelada/eliminada no puede modificarse");
        }

        // Actualizar rango de fechas si viene completo
        if (in != null && out != null) {
            validarRango(in, out);
            if (repo.existsTraslape(r.getAlojamiento().getId(), in, out)) {
                throw new IllegalStateException("Fechas no disponibles");
            }
            r.setCheckIn(in);
            r.setCheckOut(out);
        }

        // Actualizar estado (opcional)
        if (estado != null) {
            r.setEstado(estado);
        }

        return repo.save(r);
    }

    /** Cancelar (soft delete + estado CANCELADA). Idempotente. */
    @Override
    @Transactional
    public void cancelar(UUID id) {
        var r = obtener(id);
        if (r.getEstado() == EstadoReserva.CANCELADA && Boolean.TRUE.equals(r.isEliminada())) {
            return; // idempotente
        }
        r.setEstado(EstadoReserva.CANCELADA);
        r.setEliminada(true);
        repo.save(r);
    }

    /** Obtener una reserva por ID. */
    @Override
    @Transactional(readOnly = true)
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
}
