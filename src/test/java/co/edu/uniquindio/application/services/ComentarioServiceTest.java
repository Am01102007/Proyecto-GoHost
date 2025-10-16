package co.edu.uniquindio.application.services;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Comentario;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ComentarioRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.impl.ComentarioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceImplTest {

    @Mock ComentarioRepository repo;
    @Mock AlojamientoRepository alojRepo;
    @Mock UsuarioRepository usuarioRepo;

    @InjectMocks
    ComentarioServiceImpl service;

    // ========= crear(...) =========
    @Nested
    class CrearTests {

        @Test
        @DisplayName("crear: éxito con texto válido y calificación 1..5")
        void crear_ok() {
            // Arrange
            UUID alojamientoId = UUID.randomUUID();
            UUID autorId = UUID.randomUUID();
            String texto = "  Muy buen lugar  ";
            int calificacion = 5;

            var alojamiento = mock(Alojamiento.class);
            var autor = mock(Usuario.class);
            var comentarioGuardado = mock(Comentario.class);

            when(alojRepo.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));
            when(usuarioRepo.findById(autorId)).thenReturn(Optional.of(autor));
            // devolvemos el "persistido"
            when(repo.save(any(Comentario.class))).thenReturn(comentarioGuardado);

            // Act
            Comentario res = service.crear(alojamientoId, autorId, texto, calificacion);

            // Assert
            assertSame(comentarioGuardado, res);
            verify(alojRepo, times(1)).findById(alojamientoId);
            verify(usuarioRepo, times(1)).findById(autorId);
            verify(repo, times(1)).save(any(Comentario.class));
            verifyNoMoreInteractions(repo, alojRepo, usuarioRepo);
        }

        @Test
        @DisplayName("crear: falla si el texto está vacío o solo espacios")
        void crear_textoVacio() {
            UUID alojamientoId = UUID.randomUUID();
            UUID autorId = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.crear(alojamientoId, autorId, "   ", 4));
            assertTrue(ex.getMessage().toLowerCase().contains("no puede estar vacío"));
            verifyNoInteractions(repo, alojRepo, usuarioRepo);
        }

        @Test
        @DisplayName("crear: falla si calificación < 1 o > 5")
        void crear_calificacionInvalida() {
            UUID alojamientoId = UUID.randomUUID();
            UUID autorId = UUID.randomUUID();

            assertThrows(IllegalArgumentException.class,
                    () -> service.crear(alojamientoId, autorId, "ok", 0));
            assertThrows(IllegalArgumentException.class,
                    () -> service.crear(alojamientoId, autorId, "ok", 6));
            verifyNoInteractions(repo, alojRepo, usuarioRepo);
        }

        @Test
        @DisplayName("crear: falla si no existe el alojamiento")
        void crear_alojamientoNoExiste() {
            UUID alojamientoId = UUID.randomUUID();
            UUID autorId = UUID.randomUUID();

            when(alojRepo.findById(alojamientoId)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.crear(alojamientoId, autorId, "ok", 4));
            assertTrue(ex.getMessage().toLowerCase().contains("alojamiento"));
            verify(alojRepo, times(1)).findById(alojamientoId);
            verifyNoMoreInteractions(alojRepo);
            verifyNoInteractions(usuarioRepo, repo);
        }

        @Test
        @DisplayName("crear: falla si no existe el autor")
        void crear_autorNoExiste() {
            UUID alojamientoId = UUID.randomUUID();
            UUID autorId = UUID.randomUUID();

            when(alojRepo.findById(alojamientoId)).thenReturn(Optional.of(mock(Alojamiento.class)));
            when(usuarioRepo.findById(autorId)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.crear(alojamientoId, autorId, "ok", 4));
            assertTrue(ex.getMessage().toLowerCase().contains("autor"));
            verify(alojRepo, times(1)).findById(alojamientoId);
            verify(usuarioRepo, times(1)).findById(autorId);
            verifyNoMoreInteractions(alojRepo, usuarioRepo);
            verifyNoInteractions(repo);
        }
    }

    // ========= listarPorAlojamiento(...) =========
    @Test
    @DisplayName("listarPorAlojamiento: delega en el repositorio y retorna la página")
    void listarPorAlojamiento_ok() {
        UUID alojamientoId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        Page<Comentario> pageMock = new PageImpl<>(List.of(mock(Comentario.class)));

        when(repo.findByAlojamientoId(alojamientoId, pageable)).thenReturn(pageMock);

        Page<Comentario> res = service.listarPorAlojamiento(alojamientoId, pageable);

        assertSame(pageMock, res);
        verify(repo, times(1)).findByAlojamientoId(alojamientoId, pageable);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(alojRepo, usuarioRepo);
    }

    // ========= responder(...) =========
    @Nested
    class ResponderTests {

        @Test
        @DisplayName("responder: éxito cuando el anfitrión es el dueño del alojamiento")
        void responder_ok() {
            UUID comentarioId = UUID.randomUUID();
            UUID anfitrionId = UUID.randomUUID();
            String respuesta = "  Gracias por tu comentario  ";

            var comentario = mock(Comentario.class);
            var alojamiento = mock(Alojamiento.class);
            var anfitrion = mock(Usuario.class);
            var duenio = mock(Usuario.class);

            when(comentario.getAlojamiento()).thenReturn(alojamiento);
            when(alojamiento.getAnfitrion()).thenReturn(duenio);
            when(duenio.getId()).thenReturn(anfitrionId);
            when(anfitrion.getId()).thenReturn(anfitrionId);

            when(repo.findById(comentarioId)).thenReturn(Optional.of(comentario));
            when(usuarioRepo.findById(anfitrionId)).thenReturn(Optional.of(anfitrion));
            when(repo.save(comentario)).thenReturn(comentario);

            Comentario res = service.responder(comentarioId, anfitrionId, respuesta);

            assertSame(comentario, res);
            verify(repo, times(1)).findById(comentarioId);
            verify(usuarioRepo, times(1)).findById(anfitrionId);

            // Se espera que el Service haya "trimmeado" y seteado respuesta + respondidoPor
            verify(comentario, times(1)).setRespuesta("Gracias por tu comentario");
            verify(comentario, times(1)).setRespondidoPor(anfitrion);

            verify(repo, times(1)).save(comentario);
            verifyNoMoreInteractions(repo, usuarioRepo);
        }

        @Test
        @DisplayName("responder: falla si la respuesta está vacía")
        void responder_respuestaVacia() {
            UUID comentarioId = UUID.randomUUID();
            UUID anfitrionId = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.responder(comentarioId, anfitrionId, "   "));
            assertTrue(ex.getMessage().toLowerCase().contains("vacía"));
            verifyNoInteractions(repo, usuarioRepo, alojRepo);
        }

        @Test
        @DisplayName("responder: falla si el comentario no existe")
        void responder_comentarioNoExiste() {
            UUID comentarioId = UUID.randomUUID();
            UUID anfitrionId = UUID.randomUUID();

            when(repo.findById(comentarioId)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.responder(comentarioId, anfitrionId, "gracias"));
            assertTrue(ex.getMessage().toLowerCase().contains("comentario"));
            verify(repo, times(1)).findById(comentarioId);
            verifyNoMoreInteractions(repo);
            verifyNoInteractions(usuarioRepo, alojRepo);
        }

        @Test
        @DisplayName("responder: falla si el anfitrión no existe")
        void responder_anfitrionNoExiste() {
            UUID comentarioId = UUID.randomUUID();
            UUID anfitrionId = UUID.randomUUID();

            var comentario = mock(Comentario.class);
            when(repo.findById(comentarioId)).thenReturn(Optional.of(comentario));
            when(usuarioRepo.findById(anfitrionId)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> service.responder(comentarioId, anfitrionId, "ok"));
            assertTrue(ex.getMessage().toLowerCase().contains("anfitrión"));
            verify(repo, times(1)).findById(comentarioId);
            verify(usuarioRepo, times(1)).findById(anfitrionId);
            verifyNoMoreInteractions(repo, usuarioRepo);
            verifyNoInteractions(alojRepo);
        }

        @Test
        @DisplayName("responder: falla si el anfitrión NO es dueño del alojamiento")
        void responder_noAutorizado() {
            UUID comentarioId = UUID.randomUUID();
            UUID anfitrionId = UUID.randomUUID();

            var comentario = mock(Comentario.class);
            var alojamiento = mock(Alojamiento.class);
            var duenio = mock(Usuario.class);
            var otroAnfitrion = mock(Usuario.class);

            // Dueño real ≠ anfitrionId
            when(duenio.getId()).thenReturn(UUID.randomUUID());
            when(otroAnfitrion.getId()).thenReturn(anfitrionId);

            when(comentario.getAlojamiento()).thenReturn(alojamiento);
            when(alojamiento.getAnfitrion()).thenReturn(duenio);

            when(repo.findById(comentarioId)).thenReturn(Optional.of(comentario));
            when(usuarioRepo.findById(anfitrionId)).thenReturn(Optional.of(otroAnfitrion));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.responder(comentarioId, anfitrionId, "ok"));
            assertTrue(ex.getMessage().toLowerCase().contains("autorizado"));

            verify(repo, times(1)).findById(comentarioId);
            verify(usuarioRepo, times(1)).findById(anfitrionId);
            verifyNoMoreInteractions(repo, usuarioRepo);
        }
    }
}
