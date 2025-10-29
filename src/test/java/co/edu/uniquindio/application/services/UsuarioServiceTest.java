package co.edu.uniquindio.application.services;

import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.TipoDocumento;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.impl.UsuarioServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — pruebas unitarias con Mockito")
class UsuarioServiceTest {

    // ——— Constantes reutilizables ———
    private static final String EMAIL_OK = "test@gohost.test";
    private static final String EMAIL_DUP = "duplicado@gohost.test";
    private static final String DOC_OK = "100000001";
    private static final String DOC_NEW = "999999999";
    private static final String RAW_PASS = "Password123!";
    private static final String HASH = "hashedPassword";

    @Mock UsuarioRepository usuarioRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioServiceImpl usuarioService;

    private UUID usuarioId;
    private Usuario usuarioMock;

    @BeforeEach
    void init() {
        usuarioId = UUID.randomUUID();
        usuarioMock = baseUsuarioBuilder()
                .id(usuarioId)
                .email(EMAIL_OK)
                .numeroDocumento(DOC_OK)
                .password(HASH)
                .build();
    }

    // Helper centralizado para crear Usuarios
    private static Usuario.UsuarioBuilder baseUsuarioBuilder() {
        return Usuario.builder()
                .tipoDocumento(TipoDocumento.CC)
                .numeroDocumento(DOC_OK)
                .email(EMAIL_OK)
                .nombre("Test")
                .apellidos("Usuario")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("3001234567")
                .ciudad("Armenia")
                .pais("CO")
                .rol(Rol.HUESPED)
                .activo(true);
    }

    // ========== CREACIÓN ==========
    @Nested
    @DisplayName("Crear usuario")
    class Crear {

        @Test
        @DisplayName("Crea usuario exitosamente")
        void creaUsuario() {
            Usuario nuevo = baseUsuarioBuilder()
                    .email("nuevo@gohost.test")
                    .numeroDocumento(DOC_NEW)
                    .password(RAW_PASS)
                    .build();

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByNumeroDocumento(anyString())).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASS)).thenReturn(HASH);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario res = usuarioService.crear(nuevo);

            assertNotNull(res);
            verify(usuarioRepository).existsByEmail("nuevo@gohost.test");
            verify(usuarioRepository).existsByNumeroDocumento(DOC_NEW);
            verify(passwordEncoder).encode(RAW_PASS);
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Email duplicado → IllegalArgumentException")
        void emailDuplicado() {
            Usuario nuevo = baseUsuarioBuilder()
                    .email(EMAIL_DUP)
                    .numeroDocumento(DOC_NEW)
                    .password(RAW_PASS)
                    .build();

            when(usuarioRepository.existsByEmail(EMAIL_DUP)).thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> usuarioService.crear(nuevo)
            );
            assertEquals("Ya existe un usuario con ese correo", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Documento duplicado → IllegalArgumentException")
        void documentoDuplicado() {
            Usuario nuevo = baseUsuarioBuilder()
                    .email("nuevo@gohost.test")
                    .numeroDocumento(DOC_OK)
                    .password(RAW_PASS)
                    .build();

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByNumeroDocumento(DOC_OK)).thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> usuarioService.crear(nuevo)
            );
            assertEquals("Ya existe un usuario con ese número de documento", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Encripta la contraseña antes de guardar")
        void encriptaPassword() {
            Usuario nuevo = baseUsuarioBuilder()
                    .email("nuevo@gohost.test")
                    .numeroDocumento(DOC_NEW)
                    .password(RAW_PASS)
                    .build();

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByNumeroDocumento(anyString())).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASS)).thenReturn("$2a$10$hash");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            usuarioService.crear(nuevo);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertEquals("$2a$10$hash", captor.getValue().getPassword());
        }
    }

    // ========== LOGIN ==========
    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Éxito con credenciales válidas y activo")
        void ok() {
            when(usuarioRepository.findByEmail(EMAIL_OK)).thenReturn(Optional.of(usuarioMock));
            when(passwordEncoder.matches(RAW_PASS, HASH)).thenReturn(true);

            Optional<Usuario> res = usuarioService.login(EMAIL_OK, RAW_PASS);

            assertTrue(res.isPresent());
            assertEquals(usuarioMock, res.get());
            verify(passwordEncoder).matches(RAW_PASS, HASH);
        }

        @Test
        @DisplayName("Falla: email inexistente")
        void emailInexistente() {
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            Optional<Usuario> res = usuarioService.login("x@gohost.test", RAW_PASS);

            assertTrue(res.isEmpty());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Falla: contraseña incorrecta")
        void passIncorrecta() {
            when(usuarioRepository.findByEmail(EMAIL_OK)).thenReturn(Optional.of(usuarioMock));
            when(passwordEncoder.matches("BAD", HASH)).thenReturn(false);

            Optional<Usuario> res = usuarioService.login(EMAIL_OK, "BAD");
            assertTrue(res.isEmpty());
        }

        @Test
        @DisplayName("Falla: usuario inactivo")
        void inactivo() {
            usuarioMock.setActivo(false);
            when(usuarioRepository.findByEmail(EMAIL_OK)).thenReturn(Optional.of(usuarioMock));
            when(passwordEncoder.matches(RAW_PASS, HASH)).thenReturn(true);

            Optional<Usuario> res = usuarioService.login(EMAIL_OK, RAW_PASS);
            assertTrue(res.isEmpty());
        }
    }

    // ========== OBTENER ==========
    @Nested
    @DisplayName("Obtener usuario")
    class Obtener {

        @Test
        void existente() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
            Usuario res = usuarioService.obtener(usuarioId);
            assertNotNull(res);
            assertEquals(usuarioMock, res);
        }

        @Test
        void noExistente() {
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> usuarioService.obtener(id));
        }
    }

    // ========== ACTUALIZAR ==========
    @Nested
    @DisplayName("Actualizar usuario")
    class Actualizar {

        @Test
        void ok() {
            Usuario cambios = Usuario.builder()
                    .nombre("Nuevo")
                    .telefono("3009999999")
                    .ciudad("Pereira")
                    .build();

            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

            Usuario res = usuarioService.actualizar(usuarioId, cambios);

            assertNotNull(res);
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        void usuarioInactivo() {
            Usuario cambios = Usuario.builder().nombre("Nuevo Nombre").build();
            Usuario usuarioInactivo = Usuario.builder()
                    .id(usuarioId)
                    .activo(false)
                    .build();
            
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioInactivo));

            assertThrows(IllegalStateException.class,
                    () -> usuarioService.actualizar(usuarioId, cambios));
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        void noExistente() {
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class,
                    () -> usuarioService.actualizar(id, Usuario.builder().build()));
        }
    }

    // ========== PASSWORD ==========
    @Nested
    @DisplayName("Cambiar contraseña")
    class Password {

        @Test
        void ok() {
            String actual = RAW_PASS;
            String nueva = "NuevaPassword456!";

            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
            when(passwordEncoder.matches(actual, HASH)).thenReturn(true);
            when(passwordEncoder.encode(nueva)).thenReturn("newHash");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

            usuarioService.cambiarPassword(usuarioId, actual, nueva);

            verify(passwordEncoder).matches(actual, HASH);
            verify(passwordEncoder).encode(nueva);
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        void actualIncorrecta() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
            when(passwordEncoder.matches("BAD", HASH)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.cambiarPassword(usuarioId, "BAD", "new!"));
            verify(usuarioRepository, never()).save(any());
        }
    }

    // ========== LISTAR / EXISTE ==========
    @Nested
    class ListarYExiste {

        @Test
        void listar() {
            Page<Usuario> page = new PageImpl<>(List.of(usuarioMock));
            Pageable pageable = PageRequest.of(0, 10);
            when(usuarioRepository.findAll(pageable)).thenReturn(page);

            Page<Usuario> res = usuarioService.listar(pageable);

            assertEquals(1, res.getTotalElements());
        }

        @Test
        void existeEmail_true() {
            when(usuarioRepository.existsByEmail(EMAIL_OK)).thenReturn(true);
            assertTrue(usuarioService.existePorEmail(EMAIL_OK));
        }

        @Test
        void existeEmail_false() {
            when(usuarioRepository.existsByEmail("no@x.com")).thenReturn(false);
            assertFalse(usuarioService.existePorEmail("no@x.com"));
        }

        @Test
        void existeDocumento_true() {
            when(usuarioRepository.existsByNumeroDocumento(DOC_OK)).thenReturn(true);
            assertTrue(usuarioService.existePorNumeroDocumento(DOC_OK));
        }
    }
}
