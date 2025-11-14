package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.dto.mensajeDtos.CrearMensajeDTO;
import co.edu.uniquindio.gohost.dto.mensajeDtos.MensajeResDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MensajeService {
    MensajeResDTO enviar(UUID remitenteId, CrearMensajeDTO dto);
    Page<MensajeResDTO> listarPorReserva(UUID usuarioId, UUID reservaId, Pageable pageable);
}

