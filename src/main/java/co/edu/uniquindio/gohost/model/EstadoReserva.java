
/*
 * EstadoReserva — Ciclo de vida de una reserva
 * PENDIENTE -> CONFIRMADA -> CANCELADA (flujo simple para el ejemplo).
 */
package co.edu.uniquindio.gohost.model;

//usado para controlar reglas de negocio (cancelación, reportes)
public enum EstadoReserva { PENDIENTE, CONFIRMADA, CANCELADA }
