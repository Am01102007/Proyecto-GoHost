/*
 * Manejo global de errores → JSON consistente con ErrorResponse del OpenAPI.
 */
package co.edu.uniquindio.gohost.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    // ---------- Helpers ----------
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(status, message, req.getRequestURI()));
    }

    // ---------- 4xx ----------
    /** 404 - Recurso no encontrado */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> notFound(EntityNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /** 400 - Argumento inválido semánticamente (negocio) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** 409 - Conflicto (estado inconsistente) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> conflict(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** 400 - Error de validación (Bean Validation en body) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Validación fallida");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 400 - Validación a nivel de parámetros (query/path) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst().orElse("Parámetros inválidos");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 400 - JSON mal formado / formato de fecha inválido */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String msg = "Cuerpo de la solicitud inválido o con formato de fecha incorrecto (use yyyy-MM-dd).";
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 400 - Tipo inválido en path/query (UUID/enum/num) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parámetro '" + ex.getName() + "' con tipo inválido";
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 400 - Falta parámetro requerido en query */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Falta parámetro requerido: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 400 - Fechas con parseo inválido explícito */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiError> badDate(DateTimeParseException ex, HttpServletRequest req) {
        String msg = "Fecha inválida. Formato esperado: yyyy-MM-dd";
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** 403 - Acceso denegado por falta de rol o permisos */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado", req);
    }

    /** 401 - Token inválido o expirado */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> jwtError(JwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Token inválido o expirado", req);
    }

    // ---------- 5xx ----------
    /** 500 - LazyInitializationException (proxy sin sesión) */
    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ApiError> lazyInit(LazyInitializationException ex, HttpServletRequest req) {
        String mensaje = "Error de acceso a datos: relación no cargada. Contacte al administrador.";
        log.warn("LazyInitializationException en {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, mensaje, req);
    }

    /** 500 - NullPointerException en lógica de negocio */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiError> nullPointer(NullPointerException ex, HttpServletRequest req) {
        String mensaje = "Error interno: campo obligatorio nulo.";
        log.error("NullPointer en {}: ", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, mensaje, req);
    }

    /** 500 - Cualquier otro error no controlado */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> general(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}: ", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado", req);
    }
}