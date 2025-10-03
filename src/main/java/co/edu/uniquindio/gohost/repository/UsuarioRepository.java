package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link Usuario}.
 * Incluye consultas adicionales para búsquedas específicas.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario por su email.
     *
     * @param email correo electrónico a buscar.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con el correo dado.
     *
     * @param email correo electrónico a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si ya existe un usuario con el número de documento dado.
     * (Más eficiente que traer el Optional completo cuando solo necesitas saber si existe).
     *
     * @param numeroDocumento número de documento de identificación.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /**
     * Busca un usuario por número de documento.
     *
     * @param numeroDocumento número de documento de identificación.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
}