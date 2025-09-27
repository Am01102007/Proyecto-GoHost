
package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/** Reglas de negocio de Usuario **/
public interface UsuarioService {
    Usuario crear(Usuario u);
    Optional<Usuario> login(String email, String password);
    Usuario actualizar(UUID id, Usuario parcial);
    void cambiarPassword(UUID id, String actual, String nueva);
    void resetPassword(String email);
    Page<Usuario> listar(Pageable pageable);
    Usuario obtener(UUID id);
}
