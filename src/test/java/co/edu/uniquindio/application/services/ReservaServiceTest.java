package co.edu.uniquindio.application.services;



import co.edu.uniquindio.gohost.model.*;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.impl.ReservaServiceImpl;
import co.edu.uniquindio.gohost.service.mail.MailService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas UNITARIAS para ReservaServiceImpl usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ReservaService")
class ReservaServiceUnitTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Usuario huespedMock;
    private Alojamiento alojamientoMock;
    private Reserva reservaMock;
    private UUID huespedId;
    private UUID alojamientoId;
    private UUID reservaId;

    @BeforeEach
    void setUp() {
        huespedId = UUID.randomUUID();
        alojamientoId = UUID.randomUUID();
        reservaId = UUID.randomUUID();

        huespedMock = Usuario.builder()
                .id(huespedId)
                .nombre("Ana")
                .email("ana@test.com")
                .rol(Rol.HUESPED)
                .build();

        Direccion direccion = Direccion.builder()
                .ciudad("Armenia")
                .build();

        alojamientoMock = Alojamiento.builder()
                .id(alojamientoId)
                .titulo("Casa de Prueba")
                .direccion(direccion)
                .build();

        reservaMock = Reserva.builder()
                .id(reservaId)
                .huesped(huespedMock)
                .alojamiento(alojamientoMock)
                .checkIn(LocalDate.of(2025, 11, 10))
                .checkOut(LocalDate.of(2025, 11, 12))
                .estado(EstadoReserva.PENDIENTE)
                .eliminada(false)
                .build();
    }

    // ========== PRUEBAS DE CREACIÓN ==========


    @Test
    @DisplayName("Crear reserva con fechas inválidas lanza excepción")
    void testCrearReservaConFechasInvalidasLanzaExcepcion() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 3, 5);
        LocalDate checkOut = LocalDate.of(2026, 3, 1); // Checkout antes de checkin

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.crear(alojamientoId, huespedId, checkIn, checkOut)
        );

        assertEquals("Rango de fechas inválido", exception.getMessage());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Crear reserva con traslape lanza excepción")
    void testCrearReservaConTraslapeLanzaExcepcion() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 3, 1);
        LocalDate checkOut = LocalDate.of(2026, 3, 5);

        when(reservaRepository.existsTraslape(alojamientoId, checkIn, checkOut)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservaService.crear(alojamientoId, huespedId, checkIn, checkOut)
        );

        assertEquals("Fechas no disponibles", exception.getMessage());
        verify(usuarioRepository, never()).findById(any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Crear reserva con huésped no existente lanza excepción")
    void testCrearReservaConHuespedNoExistenteLanzaExcepcion() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 3, 1);
        LocalDate checkOut = LocalDate.of(2026, 3, 5);

        when(reservaRepository.existsTraslape(alojamientoId, checkIn, checkOut)).thenReturn(false);
        when(usuarioRepository.findById(huespedId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservaService.crear(alojamientoId, huespedId, checkIn, checkOut)
        );

        assertTrue(exception.getMessage().contains("Huésped no existe"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Crear reserva con alojamiento no existente lanza excepción")
    void testCrearReservaConAlojamientoNoExistenteLanzaExcepcion() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 3, 1);
        LocalDate checkOut = LocalDate.of(2026, 3, 5);

        when(reservaRepository.existsTraslape(alojamientoId, checkIn, checkOut)).thenReturn(false);
        when(usuarioRepository.findById(huespedId)).thenReturn(Optional.of(huespedMock));
        when(alojamientoRepository.findById(alojamientoId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservaService.crear(alojamientoId, huespedId, checkIn, checkOut)
        );

        assertTrue(exception.getMessage().contains("Alojamiento no existe"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    // ========== PRUEBAS DE OBTENCIÓN ==========

    @Test
    @DisplayName("Obtener reserva existente retorna reserva")
    void testObtenerReservaExistente() {
        // Arrange
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act
        Reserva resultado = reservaService.obtener(reservaId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reservaMock, resultado);
        verify(reservaRepository).findById(reservaId);
    }

    @Test
    @DisplayName("Obtener reserva no existente lanza excepción")
    void testObtenerReservaNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        when(reservaRepository.findById(idNoExiste)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> reservaService.obtener(idNoExiste)
        );
    }

    @Test
    @DisplayName("Obtener reserva con DTO retorna DTO")
    void testObtenerReservaConDTO() {
        // Arrange
        when(reservaRepository.findByIdWithFotos(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act
        var resultado = reservaService.obtenerConDTO(reservaId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reservaId, resultado.id());
        verify(reservaRepository).findByIdWithFotos(reservaId);
    }

    // ========== PRUEBAS DE LISTADO ==========

    @Test
    @DisplayName("Listar por huésped retorna página de DTOs")
    void testListarPorHuesped() {
        // Arrange
        List<Reserva> reservas = List.of(reservaMock);
        Page<Reserva> pagina = new PageImpl<>(reservas);
        var pageable = PageRequest.of(0, 10);

        when(reservaRepository.findByHuespedIdWithFotos(huespedId, pageable)).thenReturn(pagina);

        // Act
        var resultado = reservaService.listarPorHuespedConDTO(huespedId, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(reservaRepository).findByHuespedIdWithFotos(huespedId, pageable);
    }

    @Test
    @DisplayName("Listar por anfitrión retorna página de DTOs")
    void testListarPorAnfitrion() {
        // Arrange
        UUID anfitrionId = UUID.randomUUID();
        List<Reserva> reservas = List.of(reservaMock);
        Page<Reserva> pagina = new PageImpl<>(reservas);
        var pageable = PageRequest.of(0, 10);

        when(reservaRepository.findByAlojamientoAnfitrionIdWithFotos(anfitrionId, pageable))
                .thenReturn(pagina);

        // Act
        var resultado = reservaService.listarPorAnfitrionConDTO(anfitrionId, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(reservaRepository).findByAlojamientoAnfitrionIdWithFotos(anfitrionId, pageable);
    }

    // ========== PRUEBAS DE ACTUALIZACIÓN ==========

    @Test
    @DisplayName("Actualizar estado de reserva exitosamente")
    void testActualizarEstadoDeReserva() {
        // Arrange
        EstadoReserva nuevoEstado = EstadoReserva.CONFIRMADA;

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);
        when(reservaRepository.findByIdWithFotos(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act
        var resultado = reservaService.actualizarConDTO(reservaId, null, null, nuevoEstado);

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Actualizar fechas de reserva exitosamente")
    void testActualizarFechasDeReserva() {
        // Arrange
        LocalDate nuevoCheckIn = LocalDate.of(2025, 12, 1);
        LocalDate nuevoCheckOut = LocalDate.of(2025, 12, 5);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.existsTraslape(alojamientoId, nuevoCheckIn, nuevoCheckOut))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);
        when(reservaRepository.findByIdWithFotos(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act
        var resultado = reservaService.actualizarConDTO(
                reservaId, nuevoCheckIn, nuevoCheckOut, null
        );

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository).existsTraslape(alojamientoId, nuevoCheckIn, nuevoCheckOut);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Actualizar reserva cancelada lanza excepción")
    void testActualizarReservaCanceladaLanzaExcepcion() {
        // Arrange
        reservaMock.setEstado(EstadoReserva.CANCELADA);
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservaService.actualizarConDTO(reservaId, null, null, EstadoReserva.CONFIRMADA)
        );

        assertTrue(exception.getMessage().contains("cancelada/eliminada no puede modificarse"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Actualizar con fechas inválidas lanza excepción")
    void testActualizarConFechasInvalidasLanzaExcepcion() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 3, 5);
        LocalDate checkOut = LocalDate.of(2026, 3, 1); // Inválido

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reservaService.actualizarConDTO(reservaId, checkIn, checkOut, null)
        );
    }

    // ========== PRUEBAS DE CANCELACIÓN ==========

    @Test
    @DisplayName("Cancelar reserva exitosamente")
    void testCancelarReservaExitoso() {
        // Arrange
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        // Act
        reservaService.cancelar(reservaId);

        // Assert
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Cancelar reserva ya cancelada es idempotente")
    void testCancelarReservaYaCanceladaEsIdempotente() {
        // Arrange
        reservaMock.setEstado(EstadoReserva.CANCELADA);
        reservaMock.setEliminada(true);
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));

        // Act
        reservaService.cancelar(reservaId);

        // Assert
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Cancelar reserva no existente lanza excepción")
    void testCancelarReservaNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        when(reservaRepository.findById(idNoExiste)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> reservaService.cancelar(idNoExiste)
        );
    }
}