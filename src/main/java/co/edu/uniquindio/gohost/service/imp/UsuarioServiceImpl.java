
package co.edu.uniquindio.gohost.service.imp;

import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/** Implementación JPA de UsuarioService **/
@Service @RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;

    public Usuario crear(Usuario u) { return repo.save(u); }

    public Optional<Usuario> login(String email, String password) {
        return repo.findByEmail(email).filter(u -> u.getPassword().equals(password) && u.isActivo());
    }

    public Usuario actualizar(UUID id, Usuario parcial) {
        var u = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (parcial.getNombre() != null) u.setNombre(parcial.getNombre());
        if (parcial.getApellidos() != null) u.setApellidos(parcial.getApellidos());
        if (parcial.getTelefono() != null) u.setTelefono(parcial.getTelefono());
        if (parcial.getCiudad() != null) u.setCiudad(parcial.getCiudad());
        if (parcial.getPais() != null) u.setPais(parcial.getPais());
        if (parcial.getFechaNacimiento() != null) u.setFechaNacimiento(parcial.getFechaNacimiento());
        if (parcial.getTipoDocumento() != null) u.setTipoDocumento(parcial.getTipoDocumento());
        if (parcial.getNumeroDocumento() != null) u.setNumeroDocumento(parcial.getNumeroDocumento());
        if (parcial.getEmail() != null) u.setEmail(parcial.getEmail());
        return repo.save(u);
    }

    public void cambiarPassword(UUID id, String actual, String nueva) {
        var u = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!u.getPassword().equals(actual)) throw new IllegalArgumentException("Password actual incorrecto");
        u.setPassword(nueva); repo.save(u);
    }

    public void resetPassword(String email) { }

    public Page<Usuario> listar(Pageable pageable) { return repo.findAll(pageable); }

    public Usuario obtener(UUID id) { return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado")); }
}
