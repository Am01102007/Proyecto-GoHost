-- Mensajes entre huésped y anfitrión por reserva
CREATE TABLE mensaje (
    id UUID PRIMARY KEY,
    reserva_id UUID NOT NULL,
    remitente_id UUID NOT NULL,
    contenido TEXT NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    CONSTRAINT fk_msg_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_remitente FOREIGN KEY (remitente_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_mensaje_reserva_fecha ON mensaje(reserva_id, creado_en);

