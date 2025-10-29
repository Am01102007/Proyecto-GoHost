package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioPerfilDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioResDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.EditarUsuarioDTO;
import co.edu.uniquindio.gohost.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * UsuarioService — Reglas de negocio para la gestión de usuarios
 * ============================================================================
 *
 * Responsabilidades:
 *  - Crear usuarios (encriptar contraseña, validar unicidad, saneo de datos).
 *  - Autenticar (validar credenciales contra hash BCrypt).
 *  - Actualizar datos con validaciones de dominio.
 *  - Cambiar/Restablecer contraseña (flujo de seguridad).
 *  - Consultas paginadas y por identificadores.
 *  - Validaciones de existencia por email y número de documento.
 *
 * Notas de implementación (para UsuarioServiceImpl):
 *  - Usar PasswordEncoder (BCrypt) para hashear contraseñas.
 *  - Aplicar validaciones con jakarta.validation (en DTOs) y reglas en negocio.
 *  - Lanza excepciones de dominio claras (p.ej. IllegalArgumentException, IllegalStateException,
 *    o tus propias: ConflictException, NotFoundException, BadRequestException).
 *  - Marcar métodos de escritura con @Transactional.
 *  - No exponer entidades JPA directamente en controladores públicos (usar DTOs). Este servicio
 *    puede seguir devolviendo Usuario si así está diseñado, pero el controller debería mapear a DTOs.
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * Reglas de negocio:
     *  - Email único (case-insensitive).
     *  - Número de documento único (si aplica).
     *  - Contraseña se almacena encriptada (BCrypt).
     *  - Rol por defecto puede ser HUESPED (decisión de capa superior o de implementación).
     *
     * Validaciones mínimas:
     *  - email no nulo y con formato válido.
     *  - password no nula, con política mínima (longitud, complejidad) definida por negocio.
     *
     * Errores esperados:
     *  - Conflicto (409) si email o documento ya existen.
     *  - Bad Request (400) si datos no cumplen reglas.
     *
     * @param u entidad Usuario a persistir (con password en texto plano).
     * @return usuario creado con su ID asignado (y password ya encriptada).
     */
    Usuario crear(Usuario u);

    /**
     * Intenta iniciar sesión validando email y contraseña.
     *
     * Comportamiento:
     *  - Busca el usuario por email (case-insensitive).
     *  - Compara password en texto plano contra hash BCrypt almacenado.
     *  - Retorna Optional vacío si credenciales inválidas o usuario inactivo/bloqueado (si aplica).
     *
     * Seguridad:
     *  - NO filtrar si el email existe o no (evitar enumeración de usuarios).
     *
     * @param email    correo del usuario.
     * @param password contraseña en texto plano.
     * @return Optional con el usuario si las credenciales son válidas; vacío en caso contrario.
     */
    Optional<Usuario> login(String email, String password);

    /**
     * Actualiza parcialmente los datos de un usuario existente.
     *
     * Reglas:
     *  - Solo campos permitidos (nombre, teléfono, dirección, etc.).
     *  - Si cambia el email o número de documento, validar unicidad.
     *  - No debe permitir cambiar contraseña aquí (flujo separado).
     *
     * Errores esperados:
     *  - Not Found (404) si no existe el usuario.
     *  - Conflict (409) si rompe unicidad (email/documento).
     *  - Bad Request (400) si datos inválidos.
     *
     * @param id      identificador del usuario.
     * @param parcial objeto Usuario con los campos a actualizar (campos null no se tocan).
     * @return usuario actualizado.
     */
    Usuario actualizar(UUID id, Usuario parcial);

    /**
     * Cambia la contraseña de un usuario.
     *
     * Reglas:
     *  - Verificar que 'actual' coincida con el hash presente.
     *  - Validar política de 'nueva' (longitud/fortaleza).
     *  - Guardar nueva contraseña encriptada (BCrypt).
     *
     * Errores esperados:
     *  - Not Found (404) si no existe el usuario.
     *  - Unauthorized/Forbidden (401/403) si 'actual' no coincide o reglas de acceso.
     *  - Bad Request (400) por política de contraseña no cumplida.
     *
     * @param id     identificador del usuario.
     * @param actual contraseña actual (texto plano).
     * @param nueva  nueva contraseña (texto plano).
     */
    void cambiarPassword(UUID id, String actual, String nueva);

    /**
     * Restablece la contraseña de un usuario y dispara el flujo de recuperación.
     *
     * Flujo recomendado:
     *  - Generar token de recuperación (único, con expiración) y persistirlo.
     *  - Enviar correo con enlace/indicaciones (JavaMail).
     *  - Alternativamente, generar una contraseña temporal aleatoria y enviarla.
     *
     * Seguridad:
     *  - No revelar si el email existe (respuesta genérica).
     *
     * @param email correo electrónico del usuario (insensible a mayúsculas).
     */
    void resetPassword(String email);

    /**
     * Lista los usuarios paginados.
     *
     * Consideraciones:
     *  - Permitir filtros en capas superiores si se requieren (rol, estado, etc.).
     *
     * @param pageable configuración de paginación.
     * @return página con los usuarios.
     */
    /**
     * Lista usuarios paginados como DTO.
     * Excluye información sensible y evita problemas de lazy loading.
     *
     * @param pageable configuración de paginación
     * @return página de usuarios como DTO
     */
    Page<UsuarioResDTO> listarConDTO(Pageable pageable);

    /**
     * Lista usuarios paginados (método legacy).
     * @deprecated Usar listarConDTO() para evitar exposición de entidades
     */
    @Deprecated
    Page<Usuario> listar(Pageable pageable);

    /**
     * Obtiene un usuario por su ID.
     *
     * Errores esperados:
     *  - Not Found (404) si no existe.
     *
     * @param id identificador del usuario.
     * @return usuario encontrado.
     */
    Usuario obtener(UUID id);

    /**
     * Verifica si existe un usuario con el correo dado (case-insensitive).
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
    void confirmarResetPassword(String token, String nuevaPassword);

    /**
     * Obtiene el perfil del usuario autenticado como DTO.
     *
     * @param id identificador del usuario autenticado.
     * @return DTO con los datos del perfil del usuario.
     */
    /**
     * Actualiza el perfil del usuario usando DTO.
     * Traslada la lógica de mapeo del controlador al servicio.
     *
     * @param id identificador del usuario
     * @param dto datos a actualizar
     * @return DTO del perfil actualizado
     */
    UsuarioPerfilDTO actualizarPerfil(UUID id, EditarUsuarioDTO dto);

    UsuarioPerfilDTO obtenerPerfil(UUID id);

}