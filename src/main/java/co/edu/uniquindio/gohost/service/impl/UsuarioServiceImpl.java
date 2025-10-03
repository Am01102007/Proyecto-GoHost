package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de {@link UsuarioService}.
 * Contiene la lógica de negocio para creación, autenticación
 * y mantenimiento de usuarios.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

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
        return repo.save(u);
    }

    /**
     * Login seguro usando PasswordEncoder.
     * Solo permite usuarios activos.
     */
    @Override
    public Optional<Usuario> login(String email, String password) {
        return repo.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()) && u.isActivo());
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
        if (StringUtils.hasText(parcial.getNumeroDocumento())) u.setNumeroDocumento(parcial.getNumeroDocumento());
        if (StringUtils.hasText(parcial.getEmail())) u.setEmail(parcial.getEmail());

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

    /**
     * Restablecimiento básico de contraseña.
     * Genera una clave temporal aleatoria.
     */
    @Override
    public void resetPassword(String email) {
        var u = repo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese correo"));

        String nueva = UUID.randomUUID().toString().substring(0, 8);
        u.setPassword(passwordEncoder.encode(nueva));
        repo.save(u);

        // TODO: reemplazar por lógica de notificación (ej: envío de correo)
        System.out.println("Nueva contraseña temporal para " + email + ": " + nueva);
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
}