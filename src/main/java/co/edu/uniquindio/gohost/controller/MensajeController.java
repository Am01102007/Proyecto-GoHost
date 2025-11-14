package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.mensajeDtos.CrearMensajeDTO;
import co.edu.uniquindio.gohost.dto.mensajeDtos.MensajeResDTO;
import co.edu.uniquindio.gohost.security.AuthenticationHelper;
import co.edu.uniquindio.gohost.service.MensajeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajes;
    private final AuthenticationHelper authHelper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeResDTO> enviar(HttpServletRequest request, @RequestBody CrearMensajeDTO dto) {
        UUID remitenteId = authHelper.getAuthenticatedUserId(request);
        MensajeResDTO res = mensajes.enviar(remitenteId, dto);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MensajeResDTO>> listar(HttpServletRequest request,
                                                      @PathVariable UUID reservaId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        UUID userId = authHelper.getAuthenticatedUserId(request);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(mensajes.listarPorReserva(userId, reservaId, pageable));
    }
}

