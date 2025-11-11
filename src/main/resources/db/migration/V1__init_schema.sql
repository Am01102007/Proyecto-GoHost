-- =============================================================================
-- V1__init_schema.sql
-- Esquema inicial para GoHost (PostgreSQL/Neon) con enfoque en Colombia
-- Crea tablas base según entidades JPA y relaciones definidas.
-- =============================================================================

-- Usuarios
CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    tipo_documento VARCHAR(20),
    numero_documento VARCHAR(40) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    apellidos VARCHAR(120),
    fecha_nacimiento DATE,
    telefono VARCHAR(40),
    ciudad VARCHAR(120),
    pais VARCHAR(120),
    foto_perfil VARCHAR(500),
    direccion VARCHAR(255),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP,
    actualizado_en TIMESTAMP
);

-- Alojamiento principal
CREATE TABLE alojamientos (
    id UUID PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    direccion_ciudad VARCHAR(120),
    direccion_pais VARCHAR(120),
    direccion_calle VARCHAR(200),
    direccion_zip VARCHAR(20),
    direccion_lat DECIMAL(10,7),
    direccion_lon DECIMAL(10,7),
    precio_noche NUMERIC(10,2) NOT NULL,
    capacidad INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    anfitrion_id UUID NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_aloj_anfitrion FOREIGN KEY (anfitrion_id) REFERENCES usuario(id)
);

-- Índices de Alojamiento
CREATE INDEX idx_aloj_anfitrion ON alojamientos(anfitrion_id);
CREATE INDEX idx_aloj_ciudad ON alojamientos(direccion_ciudad);

-- Colección: Fotos de alojamiento
CREATE TABLE alojamiento_fotos (
    alojamiento_id UUID NOT NULL,
    orden INTEGER NOT NULL,
    foto_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (alojamiento_id, orden),
    CONSTRAINT fk_af_aloj FOREIGN KEY (alojamiento_id) REFERENCES alojamientos(id) ON DELETE CASCADE
);

-- Colección: Servicios de alojamiento
CREATE TABLE alojamiento_servicios (
    alojamiento_id UUID NOT NULL,
    servicio VARCHAR(50) NOT NULL,
    PRIMARY KEY (alojamiento_id, servicio),
    CONSTRAINT fk_as_aloj FOREIGN KEY (alojamiento_id) REFERENCES alojamientos(id) ON DELETE CASCADE
);

-- Reservas
CREATE TABLE reserva (
    id UUID PRIMARY KEY,
    huesped_id UUID NOT NULL,
    alojamiento_id UUID NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    numero_huespedes INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL,
    eliminada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_res_huesped FOREIGN KEY (huesped_id) REFERENCES usuario(id),
    CONSTRAINT fk_res_aloj FOREIGN KEY (alojamiento_id) REFERENCES alojamientos(id)
);

-- Índices de Reserva
CREATE INDEX idx_reserva_alojamiento ON reserva(alojamiento_id);
CREATE INDEX idx_reserva_check_in ON reserva(check_in);
CREATE INDEX idx_reserva_check_out ON reserva(check_out);
CREATE INDEX idx_reserva_estado ON reserva(estado);

-- Token de reseteo de contraseña (único por usuario)
CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL UNIQUE,
    expiracion TIMESTAMP NOT NULL,
    CONSTRAINT fk_prt_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Notificaciones/Recordatorios
CREATE TABLE notificaciones_recordatorio (
    id UUID PRIMARY KEY,
    reserva_id UUID NOT NULL,
    destinatario_id UUID NOT NULL,
    tipo_recordatorio VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_programada TIMESTAMP NOT NULL,
    fecha_enviado TIMESTAMP,
    asunto VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    email_destinatario VARCHAR(255) NOT NULL,
    intentos_envio INTEGER NOT NULL DEFAULT 0,
    mensaje_error TEXT,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_nr_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id) ON DELETE CASCADE,
    CONSTRAINT fk_nr_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

