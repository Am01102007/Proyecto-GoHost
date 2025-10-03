package co.edu.uniquindio.gohost.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Servicio que define las reglas de negocio para autenticación de usuarios.
 * Este contrato permite a Spring Security cargar usuarios desde la base de datos
 * usando su identificador único (UUID), que se usará como "username" dentro del
 * contexto de autenticación.
 * Implementado en {@link co.edu.uniquindio.gohost.service.impl.UserDetailsServiceImpl}.
 */
public interface UserDetailsService {

    /**
     * Carga un usuario a partir de su identificador único (UUID).
     * Este método es utilizado por Spring Security durante el proceso
     * de autenticación para construir un objeto {@link UserDetails}.
     * @param id UUID del usuario representado como String.
     * @return detalles del usuario autenticado.
     * @throws UsernameNotFoundException si no existe el usuario en la base de datos.
     */
    UserDetails loadUserByUsername(String id) throws UsernameNotFoundException;
}
