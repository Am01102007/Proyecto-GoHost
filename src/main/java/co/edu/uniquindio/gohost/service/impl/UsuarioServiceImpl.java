package co.edu.uniquindio.gohost.service.impl;
import org.springframework.transaction.annotation.Transactional;
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

        if (StringUtils.hasText(parcial.getNombre())) u.setNombre(parcial.getNombre());
        if (StringUtils.hasText(parcial.getApellidos())) u.setApellidos(parcial.getApellidos());
        if (StringUtils.hasText(parcial.getTelefono())) u.setTelefono(parcial.getTelefono());
        if (StringUtils.hasText(parcial.getCiudad())) u.setCiudad(parcial.getCiudad());
        if (StringUtils.hasText(parcial.getPais())) u.setPais(parcial.getPais());
        if (parcial.getFechaNacimiento() != null) u.setFechaNacimiento(parcial.getFechaNacimiento());
        if (parcial.getTipoDocumento() != null) u.setTipoDocumento(parcial.getTipoDocumento());
        if (StringUtils.hasText(parcial.getNumeroDocumento())) {
            // Verificar que no esté cambiando a un documento ya existente
            if (!parcial.getNumeroDocumento().equals(u.getNumeroDocumento())) {
                if (repo.existsByNumeroDocumento(parcial.getNumeroDocumento())) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese número de documento");
                }
            }
            u.setNumeroDocumento(parcial.getNumeroDocumento());
        }
        // Validar email
        if (StringUtils.hasText(parcial.getEmail())) {
            if (!parcial.getEmail().equals(u.getEmail())) {
                if (repo.existsByEmail(parcial.getEmail())) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese correo");
                }
            }
            u.setEmail(parcial.getEmail());
        }

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

        // 🔹 Crear entidad de token de recuperación
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(codigo)
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
            throw new RuntimeException("Error al enviar el correo de recuperación: " + e.getMessage(), e);
        }
    }



    /**
     * Lista de usuarios con paginación.
     */
    @Override
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
        var resetToken = passwordResetTokenRepository.findByToken(codigo.trim())
                .orElseThrow(() -> new EntityNotFoundException("Código inválido"));

        if (resetToken.expirado()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalStateException("El código ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        repo.save(usuario);

        passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());
    }



}