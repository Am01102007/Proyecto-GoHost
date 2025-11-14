-- Índice compuesto para acelerar detección de traslapes por alojamiento
-- Condición: reservas activas (no eliminadas)
CREATE INDEX idx_reserva_active_range
  ON reserva(alojamiento_id, check_in, check_out)
  WHERE eliminada = false;

