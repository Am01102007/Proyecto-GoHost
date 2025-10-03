package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementación de {@link UserDetailsService} que permite a Spring Security
 * cargar un usuario desde la base de datos usando su UUID como identificador.
 * Spring Security por defecto espera un "username" (email/username),
 * aquí se usa el UUID del usuario como principal.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario a partir de su UUID (convertido desde el parámetro "username").
     *
     * @param id UUID del usuario en formato string.
     * @return objeto {@link UserDetails} compatible con Spring Security.
     * @throws UsernameNotFoundException si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));

        // Asignar autoridad basada en el rol
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        // Usamos el id como username, y la contraseña ya encriptada
        return new org.springframework.security.core.userdetails.User(
                usuario.getId().toString(),
                usuario.getPassword(),
                List.of(authority)
        );
    }

    /**
     * Convierte el string recibido a UUID de forma segura.
     */
    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Formato inválido de UUID: " + id);
        }
    }
}
