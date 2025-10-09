/*
 * RestExceptionHandler — Manejo global de errores
 * Intercepta excepciones y mapea a JSON con códigos 400/404/409/403/401.
 */
package co.edu.uniquindio.gohost.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class RestExceptionHandler {

    /** 404 - Recurso no encontrado */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> notFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /** 400 - Argumento inválido */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /** 409 - Conflicto (estado inconsistente) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /** 400 - Error de validación (Bean Validation) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Validación fallida");
        return ResponseEntity.badRequest()
                .body(new ApiError(HttpStatus.BAD_REQUEST, msg));
    }

    /** 403 - Acceso denegado por falta de rol o permisos */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(HttpStatus.FORBIDDEN, "Acceso denegado"));
    }

    /** 401 - Token inválido o expirado */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> jwtError(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(HttpStatus.UNAUTHORIZED, "Token inválido o expirado"));
    }

    /** 500 - Cualquier otro error no controlado */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> general(Exception ex) {
        ex.printStackTrace(); // útil para depuración
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
    /** 500 - LazyInitializationException (proxy sin sesión) */
    @ExceptionHandler(org.hibernate.LazyInitializationException.class)
    public ResponseEntity<ApiError> lazyInit(org.hibernate.LazyInitializationException ex) {
        String mensaje = "Error de acceso a datos: se intentó acceder a una relación no cargada. "
                + "Por favor, contacta al administrador.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, mensaje));
    }

    /** 500 - NullPointerException en lógica de negocio (por ejemplo, toRes) */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiError> nullPointer(NullPointerException ex) {
        String mensaje = "Error interno: campo obligatorio nulo. "
                + "Por favor, verifica los datos o contacta al administrador.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, mensaje));
    }

}