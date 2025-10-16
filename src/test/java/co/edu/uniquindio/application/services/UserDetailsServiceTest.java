package co.edu.uniquindio.application.services;


import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.model.Rol; // ajusta si tu enum está en otro paquete
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername: retorna UserDetails con ROLE_ según el rol del usuario")
    void loadUserByUsername_success() {
        // Arrange
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        String passwordHash = "$2a$10$abc123...";

        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getPassword()).thenReturn(passwordHash);
        when(usuario.getRol()).thenReturn(Rol.ADMIN); // usa un rol existente en tu proyecto

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails details = userDetailsService.loadUserByUsername(idStr);

        // Assert
        assertNotNull(details);
        assertEquals(idStr, details.getUsername());
        assertEquals(passwordHash, details.getPassword());
        assertTrue(
                details.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                "Debe contener la autoridad ROLE_ADMIN"
        );

        verify(usuarioRepository, times(1)).findById(id);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("loadUserByUsername: lanza UsernameNotFoundException si no existe el usuario")
    void loadUserByUsername_userNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        UsernameNotFoundException ex =
                assertThrows(UsernameNotFoundException.class,
                        () -> userDetailsService.loadUserByUsername(idStr));

        assertTrue(ex.getMessage().contains(idStr));
        verify(usuarioRepository, times(1)).findById(id);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("loadUserByUsername: UUID inválido -> UsernameNotFoundException y NO toca el repo")
    void loadUserByUsername_invalidUUID() {
        // Arrange
        String invalid = "no-es-un-uuid";

        // Act + Assert
        UsernameNotFoundException ex =
                assertThrows(UsernameNotFoundException.class,
                        () -> userDetailsService.loadUserByUsername(invalid));

        assertTrue(ex.getMessage().toLowerCase().contains("formato inválido"),
                "Mensaje debe indicar formato inválido de UUID");

        verifyNoInteractions(usuarioRepository);
    }
}

