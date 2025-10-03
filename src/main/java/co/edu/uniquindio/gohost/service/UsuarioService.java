package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Reglas de negocio para la gestión de usuarios.
 * Define operaciones de creación, autenticación,
 * actualización de datos y consultas adicionales.
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param u entidad Usuario a persistir.
     * @return usuario creado con su ID asignado.
     */
    Usuario crear(Usuario u);

    /**
     * Intenta iniciar sesión validando email y contraseña.
     *
     * @param email    correo del usuario.
     * @param password contraseña en texto plano.
     * @return Optional con el usuario si las credenciales son válidas.
     */
    Optional<Usuario> login(String email, String password);

    /**
     * Actualiza parcialmente los datos de un usuario existente.
     *
     * @param id      identificador del usuario.
     * @param parcial objeto Usuario con los campos a actualizar.
     * @return usuario actualizado.
     */
    Usuario actualizar(UUID id, Usuario parcial);

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param id     identificador del usuario.
     * @param actual contraseña actual.
     * @param nueva  nueva contraseña.
     */
    void cambiarPassword(UUID id, String actual, String nueva);

    /**
     * Restablece la contraseña de un usuario y dispara el flujo de recuperación.
     *
     * @param email correo electrónico del usuario.
     */
    void resetPassword(String email);

    /**
     * Lista los usuarios paginados.
     *
     * @param pageable configuración de paginación.
     * @return página con los usuarios.
     */
    Page<Usuario> listar(Pageable pageable);

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id identificador del usuario.
     * @return usuario encontrado.
     */
    Usuario obtener(UUID id);

    /**
     * Verifica si existe un usuario con el correo dado.
     *
     * @param email correo a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorEmail(String email);

    /**
     * Verifica si existe un usuario con el número de documento dado.
     *
     * @param numeroDocumento número de documento a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorNumeroDocumento(String numeroDocumento);
}