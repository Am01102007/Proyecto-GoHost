package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.dto.mensajeDtos.CrearMensajeDTO;
import co.edu.uniquindio.gohost.dto.mensajeDtos.MensajeResDTO;
import co.edu.uniquindio.gohost.exception.SecurityException;
import co.edu.uniquindio.gohost.model.Mensaje;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.repository.MensajeRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository repo;
    private final ReservaRepository reservaRepo;
    private final UsuarioRepository usuarioRepo;

    @Override
    public MensajeResDTO enviar(UUID remitenteId, CrearMensajeDTO dto) {
        Reserva r = reservaRepo.findById(dto.reservaId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Reserva no existe"));

        boolean esParticipante = r.getHuesped().getId().equals(remitenteId)
                || r.getAlojamiento().getAnfitrion().getId().equals(remitenteId);
        if (!esParticipante) {
            throw new SecurityException("No puedes enviar mensajes en una reserva de la que no eres parte");
        }

        var remitente = usuarioRepo.findById(remitenteId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuario no existe"));

        Mensaje m = Mensaje.builder()
                .reserva(r)
                .remitente(remitente)
                .contenido(dto.contenido())
                .creadoEn(LocalDateTime.now())
                .build();
        Mensaje saved = repo.save(m);
        return toRes(saved);
    }

    @Override
    public Page<MensajeResDTO> listarPorReserva(UUID usuarioId, UUID reservaId, Pageable pageable) {
        Reserva r = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Reserva no existe"));
        boolean esParticipante = r.getHuesped().getId().equals(usuarioId)
                || r.getAlojamiento().getAnfitrion().getId().equals(usuarioId);
        if (!esParticipante) {
            throw new SecurityException("No puedes ver mensajes de una reserva de la que no eres parte");
        }
        return repo.findByReserva(reservaId, pageable).map(this::toRes);
    }

    private MensajeResDTO toRes(Mensaje m) {
        return new MensajeResDTO(
                m.getId(),
                m.getReserva().getId(),
                m.getRemitente().getId(),
                m.getRemitente().getNombre(),
                m.getContenido(),
                m.getCreadoEn()
        );
    }
}

