
package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Repositorio de usuarios, incluye búsquedas por email y documento **/
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /** Busca por email. @param email correo a buscar @return Optional con el usuario si existe */
    Optional<Usuario> findByEmail(String email);

    /** Busca por número de documento. @param numeroDocumento documento @return Optional con el usuario si existe */
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
}
