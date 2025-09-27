
package co.edu.uniquindio.gohost.service.imp;

import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.repository.*;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/** Implementación JPA de ReservaService **/
@Service @RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final AlojamientoRepository alojRepo;

    public Reserva crear(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out) {
        if (out.isBefore(in) || out.equals(in)) throw new IllegalArgumentException("Rango de fechas inválido");
        if (repo.existsTraslape(alojamientoId, in, out)) throw new IllegalStateException("Fechas no disponibles");
        var huesped = usuarioRepo.findById(huespedId).orElseThrow(() -> new EntityNotFoundException("Huésped no existe"));
        var aloj = alojRepo.findById(alojamientoId).orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));
        return repo.save(Reserva.builder().huesped(huesped).alojamiento(aloj).checkIn(in).checkOut(out).estado(EstadoReserva.PENDIENTE).build());
    }

    public Page<Reserva> listarPorHuesped(UUID huespedId, Pageable pageable) { return repo.findByHuespedId(huespedId, pageable); }

    public Page<Reserva> listarPorAnfitrion(UUID anfitrionId, Pageable pageable) { return repo.findByAlojamientoAnfitrionId(anfitrionId, pageable); }

    public Reserva actualizar(UUID id, LocalDate in, LocalDate out, EstadoReserva estado) {
        var r = obtener(id);
        if (in != null && out != null) {
            if (out.isBefore(in) || out.equals(in)) throw new IllegalArgumentException("Rango inválido");
            if (repo.existsTraslape(r.getAlojamiento().getId(), in, out)) throw new IllegalStateException("Fechas no disponibles");
            r.setCheckIn(in); r.setCheckOut(out);
        }
        if (estado != null) r.setEstado(estado);
        return repo.save(r);
    }

    public void cancelar(UUID id) { var r = obtener(id); r.setEstado(EstadoReserva.CANCELADA); r.setEliminada(true); repo.save(r); }

    public Reserva obtener(UUID id) { return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Reserva no existe")); }
}
