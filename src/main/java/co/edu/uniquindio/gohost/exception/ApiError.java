
/*
 * ApiError — Estructura de error REST
 */
package co.edu.uniquindio.gohost.exception;

import org.springframework.http.HttpStatus;

//usamos un record por simpleza y legibilidad
public record ApiError(HttpStatus status, String message) { }

