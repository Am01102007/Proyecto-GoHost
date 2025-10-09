/*
 * ApiError — Estructura de error REST (compatible con OpenAPI)
 */
package co.edu.uniquindio.gohost.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

public record ApiError(
        int status,               // código HTTP
        String error,             // texto del estado
        String message,           // detalle del error
        String path,              // endpoint solicitado
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime timestamp  // ISO-8601
) {

    /** Ctor de conveniencia: status + message + path */
    public ApiError(HttpStatus httpStatus, String message, String path) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), message, path, OffsetDateTime.now());
    }

    /** Ctor de conveniencia: status + message (path nulo) — mantiene compatibilidad con tu código actual */
    public ApiError(HttpStatus httpStatus, String message) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), message, null, OffsetDateTime.now());
    }
}
