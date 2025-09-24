
/*
 * RestExceptionHandler — Manejo global de errores
 * intercepta excepciones y mapea a JSON con códigos 400/404/409.
 */
package co.edu.uniquindio.gohost.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

//@RestControllerAdvice aplica a todos los controladores de la app
@RestControllerAdvice
public class RestExceptionHandler {

    //convierte EntityNotFoundException en 404 Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> notFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    //convierte IllegalArgumentException en 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    //convierte IllegalStateException en 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(HttpStatus.CONFLICT, ex.getMessage()));
    }

    //extrae el primer error de validación de Bean Validation y lo devuelve con 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Validación fallida");
        return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST, msg));
    }
}
