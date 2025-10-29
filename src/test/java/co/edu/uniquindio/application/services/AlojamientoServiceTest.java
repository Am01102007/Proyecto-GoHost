package co.edu.uniquindio.application.services;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.impl.AlojamientoServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas UNITARIAS para AlojamientoServiceImpl usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - AlojamientoService")
class AlojamientoServiceUnitTest {

    @Mock
    private AlojamientoRepository alojamientoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @InjectMocks
    private AlojamientoServiceImpl alojamientoService;

    private Usuario anfitrionMock;
    private Usuario huespedMock;
    private Alojamiento alojamientoMock;
    private UUID anfitrionId;
    private UUID alojamientoId;

    @BeforeEach
    void setUp() {
        anfitrionId = UUID.randomUUID();
        alojamientoId = UUID.randomUUID();

        anfitrionMock = Usuario.builder()
                .id(anfitrionId)
                .nombre("Bruno")
                .email("bruno@test.com")
                .rol(Rol.ANFITRION)
                .build();

        huespedMock = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Ana")
                .email("ana@test.com")
                .rol(Rol.HUESPED)
                .build();

        Direccion direccion = Direccion.builder()
                .calle("Calle 10 #5-20")
                .ciudad("Armenia")
                .pais("CO")
                .latitud(4.533333)
                .longitud(-75.683333)
                .build();

        alojamientoMock = Alojamiento.builder()
                .id(alojamientoId)
                .titulo("Casa de Prueba")
                .descripcion("Descripción de prueba")
                .direccion(direccion)
                .precioNoche(new BigDecimal("200000"))
                .capacidad(4)
                .fotos(new ArrayList<>())
                .activo(true)
                .anfitrion(anfitrionMock)
                .build();
    }

    // ========== PRUEBAS DE CREACIÓN ==========

    @Test
    @DisplayName("Crear alojamiento exitosamente")
    void testCrearAlojamientoExitoso() {
        // Arrange
        when(usuarioRepository.findById(anfitrionId)).thenReturn(Optional.of(anfitrionMock));
        when(alojamientoRepository.save(any(Alojamiento.class))).thenReturn(alojamientoMock);

        // Act
        Alojamiento resultado = alojamientoService.crear(anfitrionId, alojamientoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(anfitrionMock, resultado.getAnfitrion());
        verify(usuarioRepository).findById(anfitrionId);
        verify(alojamientoRepository).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Crear alojamiento con usuario sin rol ANFITRION lanza excepción")
    void testCrearAlojamientoConUsuarioSinRolAnfitrionLanzaExcepcion() {
        // Arrange
        UUID huespedId = huespedMock.getId();
        when(usuarioRepository.findById(huespedId)).thenReturn(Optional.of(huespedMock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alojamientoService.crear(huespedId, alojamientoMock)
        );

        assertEquals("El usuario no tiene rol ANFITRION", exception.getMessage());
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Crear alojamiento con anfitrión no existente lanza excepción")
    void testCrearAlojamientoConAnfitrionNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        when(usuarioRepository.findById(idNoExiste)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> alojamientoService.crear(idNoExiste, alojamientoMock)
        );

        assertTrue(exception.getMessage().contains("Anfitrión no encontrado"));
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Crear alojamiento inicializa valores por defecto")
    void testCrearAlojamientoInicializaValoresPorDefecto() {
        // Arrange
        Alojamiento alojamientoSinDefaults = Alojamiento.builder()
                .titulo("Test")
                .descripcion("Test")
                .direccion(new Direccion())
                .precioNoche(new BigDecimal("100000"))
                .capacidad(2)
                .activo(null) // Sin valor
                .fotos(null)  // Sin valor
                .build();

        when(usuarioRepository.findById(anfitrionId)).thenReturn(Optional.of(anfitrionMock));
        when(alojamientoRepository.save(any(Alojamiento.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Alojamiento resultado = alojamientoService.crear(anfitrionId, alojamientoSinDefaults);

        // Assert
        assertTrue(resultado.getActivo());
        assertNotNull(resultado.getFotos());
        assertTrue(resultado.getFotos().isEmpty());
    }

    // ========== PRUEBAS DE LISTADO ==========

    @Test
    @DisplayName("Listar alojamientos retorna página de DTOs")
    void testListarAlojamientos() {
        // Arrange
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(alojamientoRepository.findAllWithFotos(pageable)).thenReturn(pagina);

        // Act
        var resultado = alojamientoService.listar(pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).findAllWithFotos(pageable);
    }

    @Test
    @DisplayName("Listar por anfitrión retorna página de DTOs")
    void testListarPorAnfitrion() {
        // Arrange
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(usuarioRepository.existsById(anfitrionId)).thenReturn(true);
        when(alojamientoRepository.findByAnfitrionIdWithFotos(anfitrionId, pageable))
                .thenReturn(pagina);

        // Act
        var resultado = alojamientoService.listarPorAnfitrion(anfitrionId, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).findByAnfitrionIdWithFotos(anfitrionId, pageable);
    }

    @Test
    @DisplayName("Listar por anfitrión no existente lanza excepción")
    void testListarPorAnfitrionNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);

        when(usuarioRepository.existsById(idNoExiste)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> alojamientoService.listarPorAnfitrion(idNoExiste, pageable)
        );
        verify(alojamientoRepository, never()).findByAnfitrionIdWithFotos(any(), any());
    }

    // ========== PRUEBAS DE OBTENCIÓN ==========

    @Test
    @DisplayName("Obtener alojamiento existente retorna DTO")
    void testObtenerAlojamientoExistente() {
        // Arrange
        when(alojamientoRepository.findByIdWithFotos(alojamientoId))
                .thenReturn(Optional.of(alojamientoMock));

        // Act
        var resultado = alojamientoService.obtener(alojamientoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(alojamientoId, resultado.id());
        assertEquals("Casa de Prueba", resultado.titulo());
        verify(alojamientoRepository).findByIdWithFotos(alojamientoId);
    }

    @Test
    @DisplayName("Obtener alojamiento no existente lanza excepción")
    void testObtenerAlojamientoNoExistenteLanzaExcepcion() {
        // Arrange
        when(alojamientoRepository.findByIdWithFotos(alojamientoId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> alojamientoService.obtener(alojamientoId)
        );
    }

    // ========== PRUEBAS DE ACTUALIZACIÓN ==========

    @Test
    @DisplayName("Actualizar alojamiento exitosamente")
    void testActualizarAlojamientoExitoso() {
        // Arrange
        Alojamiento datosActualizados = Alojamiento.builder()
                .titulo("Título Actualizado")
                .precioNoche(new BigDecimal("250000"))
                .build();

        when(alojamientoRepository.findById(alojamientoId))
                .thenReturn(Optional.of(alojamientoMock));
        when(alojamientoRepository.save(any(Alojamiento.class)))
                .thenReturn(alojamientoMock);
        when(alojamientoRepository.findByIdWithFotos(alojamientoId))
                .thenReturn(Optional.of(alojamientoMock));

        // Act
        var resultado = alojamientoService.actualizar(alojamientoId, datosActualizados);

        // Assert
        assertNotNull(resultado);
        verify(alojamientoRepository).findById(alojamientoId);
        verify(alojamientoRepository).save(any(Alojamiento.class));
    }

    @Test
    @DisplayName("Actualizar alojamiento no existente lanza excepción")
    void testActualizarAlojamientoNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        Alojamiento datosActualizados = Alojamiento.builder()
                .titulo("Test")
                .build();

        when(alojamientoRepository.findById(idNoExiste)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> alojamientoService.actualizar(idNoExiste, datosActualizados)
        );
        verify(alojamientoRepository, never()).save(any(Alojamiento.class));
    }

    // ========== PRUEBAS DE ELIMINACIÓN ==========

    @Test
    @DisplayName("Eliminar alojamiento exitosamente")
    void testEliminarAlojamientoExitoso() {
        // Arrange
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(alojamientoId);
        alojamiento.setActivo(true);
        
        when(alojamientoRepository.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));
        when(reservaRepository.existsReservasFuturas(alojamientoId, LocalDate.now())).thenReturn(false);
        when(alojamientoRepository.save(any(Alojamiento.class))).thenReturn(alojamiento);

        // Act
        alojamientoService.eliminar(alojamientoId);

        // Assert
        verify(alojamientoRepository).findById(alojamientoId);
        verify(reservaRepository).existsReservasFuturas(alojamientoId, LocalDate.now());
        verify(alojamientoRepository).save(alojamiento);
        assertFalse(alojamiento.getActivo());
    }

    @Test
    @DisplayName("Eliminar alojamiento no existente lanza excepción")
    void testEliminarAlojamientoNoExistenteLanzaExcepcion() {
        // Arrange
        UUID idNoExiste = UUID.randomUUID();
        when(alojamientoRepository.findById(idNoExiste)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> alojamientoService.eliminar(idNoExiste)
        );
        verify(alojamientoRepository, never()).deleteById(any());
    }

    // ========== PRUEBAS DE BÚSQUEDA ==========

    @Test
    @DisplayName("Buscar sin filtros retorna todos los alojamientos")
    void testBuscarSinFiltros() {
        // Arrange
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(alojamientoRepository.findAllWithFotos(pageable)).thenReturn(pagina);

        // Act
        var resultado = alojamientoService.buscar(null, null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).findAllWithFotos(pageable);
        verify(alojamientoRepository, never()).searchWithFotos(any(), any(), any());
    }

    @Test
    @DisplayName("Buscar con ciudad retorna resultados filtrados")
    void testBuscarConCiudad() {
        // Arrange
        String ciudad = "Armenia";
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(alojamientoRepository.searchWithFotos(ciudad, null, pageable))
                .thenReturn(pagina);

        // Act
        var resultado = alojamientoService.buscar(ciudad, null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).searchWithFotos(ciudad, null, pageable);
    }

    @Test
    @DisplayName("Buscar con capacidad retorna resultados filtrados")
    void testBuscarConCapacidad() {
        // Arrange
        Integer capacidad = 4;
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(alojamientoRepository.searchWithFotos(null, capacidad, pageable))
                .thenReturn(pagina);

        // Act
        var resultado = alojamientoService.buscar(null, capacidad, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).searchWithFotos(null, capacidad, pageable);
    }

    @Test
    @DisplayName("Buscar con ciudad y capacidad retorna resultados filtrados")
    void testBuscarConCiudadYCapacidad() {
        // Arrange
        String ciudad = "Armenia";
        Integer capacidad = 4;
        List<Alojamiento> alojamientos = List.of(alojamientoMock);
        Page<Alojamiento> pagina = new PageImpl<>(alojamientos);
        var pageable = PageRequest.of(0, 10);

        when(alojamientoRepository.searchWithFotos(ciudad, capacidad, pageable))
                .thenReturn(pagina);

        // Act
        var resultado = alojamientoService.buscar(ciudad, capacidad, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(alojamientoRepository).searchWithFotos(ciudad, capacidad, pageable);
    }
}