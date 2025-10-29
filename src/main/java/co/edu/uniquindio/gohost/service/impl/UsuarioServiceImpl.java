package co.edu.uniquindio.gohost.service.impl;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioPerfilDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioResDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.EditarUsuarioDTO;
import co.edu.uniquindio.gohost.exception.PasswordResetException;
import co.edu.uniquindio.gohost.exception.MailServiceException;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.UsuarioService;
import co.edu.uniquindio.gohost.service.mail.MailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import co.edu.uniquindio.gohost.service.geocoding.GeocodingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import co.edu.uniquindio.gohost.model.PasswordResetToken;
import co.edu.uniquindio.gohost.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;

import co.edu.uniquindio.gohost.repository.PasswordResetTokenRepository;

/**
 * Implementación de {@link UsuarioService}.
 * Contiene la lógica de negocio para creación, autenticación
 * y mantenimiento de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;


    /**
     * Crea un usuario validando unicidad de email y documento.
     * Encripta la contraseña antes de persistir.
     */
    @Override
    public Usuario crear(Usuario u) {
        if (repo.existsByEmail(u.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        if (repo.existsByNumeroDocumento(u.getNumeroDocumento())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese número de documento");
        }

        u.setPassword(passwordEncoder.encode(u.getPassword()));

        // Geocodificar si hay ciudad/país pero no dirección completa
        geocodificarUsuario(u);

        return repo.save(u);
    }

    private void geocodificarUsuario(Usuario u) {
        if (u.getLatitud() != null && u.getLongitud() != null) {
            return; // Ya tiene coordenadas
        }

        try {
            String direccion = u.getDireccion();

            // Si no tiene dirección, construir con ciudad + país
            if (!StringUtils.hasText(direccion)) {
                StringBuilder sb = new StringBuilder();
                if (StringUtils.hasText(u.getCiudad())) sb.append(u.getCiudad());
                if (StringUtils.hasText(u.getPais())) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(u.getPais());
                }
                direccion = sb.toString();
            }

            if (StringUtils.hasText(direccion)) {
                var coordenadas = geocodingService.obtenerCoordenadas(
                        direccion,
                        u.getCiudad(),
                        u.getPais()
                );

                if (coordenadas != null) {
                    u.setLatitud(coordenadas.latitud());
                    u.setLongitud(coordenadas.longitud());
                }
            }
        } catch (Exception e) {
            log.error("Error geocodificando usuario", e);
        }
    }

    /**
     * Login seguro usando PasswordEncoder.
     * Solo permite usuarios activos.
     */
    @Override
    public Optional<Usuario> login(String email, String password) {
        log.info("=== INTENTO DE LOGIN ===");
        log.info("Email recibido: {}", email);

        Optional<Usuario> usuarioOpt = repo.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario u = usuarioOpt.get();

        boolean passwordMatch = passwordEncoder.matches(password, u.getPassword());

        if (!passwordMatch) {
            return Optional.empty();
        }
        if (!u.isActivo()) {
            return Optional.empty();
        }
        return Optional.of(u);
    }

    /**
     * Actualización parcial del perfil de usuario.
     */
    @Override
    public Usuario actualizar(UUID id, Usuario parcial) {
        var u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Validar que el usuario esté activo antes de permitir modificaciones
        if (!u.isActivo()) {
            throw new IllegalStateException("No se pueden modificar datos de un usuario inactivo");
        }

        if (StringUtils.hasText(parcial.getNombre())) u.setNombre(parcial.getNombre());
        if (StringUtils.hasText(parcial.getApellidos())) u.setApellidos(parcial.getApellidos());
        if (StringUtils.hasText(parcial.getTelefono())) u.setTelefono(parcial.getTelefono());
        if (StringUtils.hasText(parcial.getCiudad())) u.setCiudad(parcial.getCiudad());
        if (StringUtils.hasText(parcial.getPais())) u.setPais(parcial.getPais());
        if (parcial.getFechaNacimiento() != null) u.setFechaNacimiento(parcial.getFechaNacimiento());
        if (parcial.getTipoDocumento() != null) u.setTipoDocumento(parcial.getTipoDocumento());
        if (StringUtils.hasText(parcial.getFotoPerfil())) u.setFotoPerfil(parcial.getFotoPerfil());
        if (StringUtils.hasText(parcial.getNumeroDocumento())) {
            // Verificar que no esté cambiando a un documento ya existente
            if (!parcial.getNumeroDocumento().equals(u.getNumeroDocumento())) {
                if (repo.existsByNumeroDocumento(parcial.getNumeroDocumento())) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese número de documento");
                }
            }
            u.setNumeroDocumento(parcial.getNumeroDocumento());
        }
        // Nota: El email ya no se puede modificar por seguridad

        return repo.save(u);
    }

    /**
     * Cambio de contraseña con validación de la contraseña actual.
     */
    @Override
    public void cambiarPassword(UUID id, String actual, String nueva) {
        var u = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(actual, u.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        u.setPassword(passwordEncoder.encode(nueva));
        repo.save(u);
    }

    @Override
    public void resetPassword(String email) {
        // Buscar usuario por correo
        var usuario = repo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese correo"));

        // 🧹 Eliminar cualquier token previo del mismo usuario
        passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());

        // 🔹 Generar un código de 6 dígitos aleatorio
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // � Cifrar el código antes de guardarlo en la base de datos
        String codigoCifrado = passwordEncoder.encode(codigo);

        // � Crear entidad de token de recuperación con código cifrado
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(codigoCifrado)
                .usuario(usuario)
                .expiracion(LocalDateTime.now().plusMinutes(15))
                .build();

        // Guardar el token en la base de datos
        passwordResetTokenRepository.save(resetToken);

        // 🔹 Plantilla HTML del correo
        String html = """
    <h2>Restablecimiento de contraseña</h2>
    <p>Hola %s,</p>
    <p>Recibimos una solicitud para restablecer tu contraseña.</p>
    <p>Tu código de recuperación es:</p>
    <h1 style="color:#007BFF;">%s</h1>
    <p>Este código expirará en 15 minutos.</p>
    <br/>
    <p>Si no solicitaste este cambio, puedes ignorar este correo.</p>
    """.formatted(usuario.getNombre(), codigo);

        // 🔹 Enviar el correo
        try {
            mailService.sendMail(email, "Código de recuperación de contraseña", html);
        } catch (Exception e) {
            throw new MailServiceException("Error al enviar el correo de recuperación: " + e.getMessage(), e);
        }
    }



    /**
     * Lista de usuarios con paginación como DTO.
     * Excluye información sensible y evita lazy loading.
     */
    @Override
    public Page<UsuarioResDTO> listarConDTO(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResDTO);
    }

    /**
     * Lista de usuarios con paginación (método legacy).
     * @deprecated Usar listarConDTO() para evitar exposición de entidades
     */
    @Override
    @Deprecated
    public Page<Usuario> listar(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @Override
    public Usuario obtener(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    /**
     * Verifica existencia por email.
     */
    @Override
    public boolean existePorEmail(String email) {
        return repo.existsByEmail(email);
    }

    /**
     * Verifica existencia por número de documento.
     */
    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return repo.existsByNumeroDocumento(numeroDocumento);
    }
    @Transactional
    @Override
    public void confirmarResetPassword(String codigo, String nuevaPassword) {
        // 🔍 Buscar todos los tokens activos y validar el código cifrado
        List<PasswordResetToken> tokensActivos = passwordResetTokenRepository.findAll()
                .stream()
                .filter(token -> !token.expirado())
                .toList();

        PasswordResetToken resetToken = null;
        for (PasswordResetToken token : tokensActivos) {
            // 🔒 Verificar si el código ingresado coincide con el token cifrado
            if (passwordEncoder.matches(codigo.trim(), token.getToken())) {
                resetToken = token;
                break;
            }
        }

        if (resetToken == null) {
            throw new PasswordResetException("Código inválido o expirado");
        }

        // Verificar nuevamente que no haya expirado (por seguridad)
        if (resetToken.expirado()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new PasswordResetException("El código ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        repo.save(usuario);

        passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());
    }

    @Override
    public UsuarioPerfilDTO obtenerPerfil(UUID id) {
        Usuario usuario = obtener(id);
        
        return new UsuarioPerfilDTO(
                usuario.getId(),
                usuario.getTipoDocumento(),
                usuario.getNumeroDocumento(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getFechaNacimiento(),
                usuario.getTelefono(),
                usuario.getCiudad(),
                usuario.getPais(),
                usuario.getDireccion(),
                usuario.getFotoPerfil(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn()
        );
    }

    /**
     * Actualiza el perfil del usuario usando DTO.
     * Traslada la lógica de mapeo del controlador al servicio.
     */
    @Override
    public UsuarioPerfilDTO actualizarPerfil(UUID id, EditarUsuarioDTO dto) {
        var parcial = new Usuario();
        parcial.setNombre(dto.nombre());
        parcial.setApellidos(dto.apellidos());
        parcial.setTelefono(dto.telefono());
        parcial.setCiudad(dto.ciudad());
        parcial.setPais(dto.pais());
        parcial.setFechaNacimiento(dto.fechaNacimiento());
        parcial.setTipoDocumento(dto.tipoDocumento());
        parcial.setNumeroDocumento(dto.numeroDocumento());
        parcial.setFotoPerfil(dto.fotoPerfil());

        Usuario actualizado = actualizar(id, parcial);
        return obtenerPerfil(actualizado.getId());
    }

    /**
     * Convierte Usuario a UsuarioResDTO excluyendo información sensible.
     */
    private UsuarioResDTO toResDTO(Usuario u) {
        return new UsuarioResDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getTelefono(),
                u.getCiudad(),
                u.getPais(),
                u.getRol(),
                u.isActivo(),
                u.getCreadoEn()
        );
    }

}