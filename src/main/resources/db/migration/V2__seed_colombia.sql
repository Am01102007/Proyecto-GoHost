-- =============================================================================
-- V2__seed_colombia.sql
-- Datos semilla orientados a operación en Colombia.
-- NOTA: las contraseñas aquí son de demostración; cámbielas/reháselas en producción.
-- =============================================================================

-- ===== Usuarios =====
INSERT INTO usuario (
    id, tipo_documento, numero_documento, email, nombre, apellidos,
    fecha_nacimiento, telefono, ciudad, pais, foto_perfil,
    direccion, latitud, longitud, password, rol, activo, creado_en
) VALUES
    ('11111111-1111-1111-1111-111111111111', 'CC', '1020304050', 'anfitrion.bogota@gohost.co', 'Camila', 'García',
     '1990-05-12', '+57 3001234567', 'Bogotá', 'Colombia', NULL,
     'Carrera 7 # 72-48, Chapinero', 4.648625, -74.059021, '$2a$10$2b2lBz0d3m6yXqXm3QTPLeJmYHq3L6d5oXbS12o7xAUV7Wc8Ue9Ta', 'ANFITRION', TRUE, NOW()),
    ('22222222-2222-2222-2222-222222222222', 'CC', '5060708090', 'anfitrion.medellin@gohost.co', 'Juan', 'Restrepo',
     '1988-11-03', '+57 3017654321', 'Medellín', 'Colombia', NULL,
     'Calle 10 # 35-80, El Poblado', 6.208763, -75.565443, '$2a$10$2b2lBz0d3m6yXqXm3QTPLeJmYHq3L6d5oXbS12o7xAUV7Wc8Ue9Ta', 'ANFITRION', TRUE, NOW()),
    ('33333333-3333-3333-3333-333333333333', 'CC', '1122334455', 'huesped.cali@gohost.co', 'Valeria', 'Muñoz',
     '1995-02-20', '+57 3025556677', 'Cali', 'Colombia', NULL,
     'Avenida 6N # 34-45, Granada', 3.460033, -76.533349, '$2a$10$2b2lBz0d3m6yXqXm3QTPLeJmYHq3L6d5oXbS12o7xAUV7Wc8Ue9Ta', 'HUESPED', TRUE, NOW()),
    ('44444444-4444-4444-4444-444444444444', 'CC', '9998887776', 'admin@gohost.co', 'Admin', 'GoHost',
     '1985-01-01', '+57 3000000000', 'Bogotá', 'Colombia', NULL,
     'Calle 26 # 85D-55, Fontibón', 4.701940, -74.146782, '$2a$10$2b2lBz0d3m6yXqXm3QTPLeJmYHq3L6d5oXbS12o7xAUV7Wc8Ue9Ta', 'ADMIN', TRUE, NOW());

-- La contraseña seeded usa un hash BCrypt genérico de demostración.
-- Recomiendo forzar cambio de contraseña al primer inicio de sesión.

-- ===== Alojamientos =====
INSERT INTO alojamientos (
    id, titulo, descripcion,
    direccion_ciudad, direccion_pais, direccion_calle, direccion_zip,
    direccion_lat, direccion_lon,
    precio_noche, capacidad, activo,
    anfitrion_id, fecha_creacion
) VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     'Apartamento moderno en Chapinero',
     'Apartamento con vista a los Cerros Orientales, ideal para trabajo remoto. Cerca de Zona G.',
     'Bogotá', 'Colombia', 'Carrera 7 # 72-48, Chapinero', '110231',
     4.648625, -74.059021,
     180000, 3, TRUE,
     '11111111-1111-1111-1111-111111111111', NOW()),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
     'Casa amplia en El Poblado',
     'Casa con jardín y parrilla en sector tranquilo. Ideal para familias y grupos.',
     'Medellín', 'Colombia', 'Calle 10 # 35-80, El Poblado', '050022',
     6.208763, -75.565443,
     350000, 6, TRUE,
     '22222222-2222-2222-2222-222222222222', NOW()),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
     'Apto frente al mar en Bocagrande',
     'Alojamiento con balcón y vista al mar, perfecto para vacaciones en Cartagena.',
     'Cartagena', 'Colombia', 'Avenida San Martín # 8-20, Bocagrande', '130001',
     10.399717, -75.556985,
     500000, 4, TRUE,
     '22222222-2222-2222-2222-222222222222', NOW()),
    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
     'Loft en barrio Granada',
     'Loft con diseño industrial, cerca de restaurantes y vida nocturna en Cali.',
     'Cali', 'Colombia', 'Avenida 6N # 34-45, Granada', '760042',
     3.460033, -76.533349,
     220000, 2, TRUE,
     '11111111-1111-1111-1111-111111111111', NOW());

-- ===== Fotos de alojamientos =====
INSERT INTO alojamiento_fotos (alojamiento_id, orden, foto_url) VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 1, 'https://res.cloudinary.com/gohost/images/bogota-chapinero-1.jpg'),
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 2, 'https://res.cloudinary.com/gohost/images/bogota-chapinero-2.jpg'),
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 3, 'https://res.cloudinary.com/gohost/images/bogota-chapinero-3.jpg'),

    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 1, 'https://res.cloudinary.com/gohost/images/medellin-poblado-1.jpg'),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 2, 'https://res.cloudinary.com/gohost/images/medellin-poblado-2.jpg'),

    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 1, 'https://res.cloudinary.com/gohost/images/cartagena-bocagrande-1.jpg'),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 2, 'https://res.cloudinary.com/gohost/images/cartagena-bocagrande-2.jpg'),

    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 1, 'https://res.cloudinary.com/gohost/images/cali-granada-1.jpg');

-- ===== Servicios por alojamiento =====
INSERT INTO alojamiento_servicios (alojamiento_id, servicio) VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'WIFI'),
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'COCINA'),
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'TELEVISION'),
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'ASCENSOR'),

    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'WIFI'),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'PARRILLA'),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'JARDIN'),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'ESTACIONAMIENTO'),

    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'WIFI'),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'BALCON'),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'AIRE_ACONDICIONADO'),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'DESAYUNO_INCLUIDO'),

    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'WIFI'),
    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'COCINA'),
    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'NETFLIX');

-- ===== Reservas =====
INSERT INTO reserva (
    id, huesped_id, alojamiento_id,
    check_in, check_out, numero_huespedes, estado, eliminada
) VALUES
    ('bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '33333333-3333-3333-3333-333333333333', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
     '2025-12-20', '2025-12-27', 2, 'CONFIRMADA', FALSE),
    ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '33333333-3333-3333-3333-333333333333', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     '2025-11-15', '2025-11-18', 1, 'PENDIENTE', FALSE);

-- ===== Recordatorios (ejemplos) =====
INSERT INTO notificaciones_recordatorio (
    id, reserva_id, destinatario_id, tipo_recordatorio, estado,
    fecha_programada, fecha_enviado, asunto, mensaje, email_destinatario,
    intentos_envio, mensaje_error, fecha_creacion
) VALUES
    ('ccccccc1-cccc-cccc-cccc-ccccccccccc1', 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '33333333-3333-3333-3333-333333333333',
     'RECORDATORIO_HUESPED_CHECKIN', 'PROGRAMADO',
     NOW() + INTERVAL '3 days', NULL,
     'Tu viaje a Cartagena está cerca',
     'Hola Valeria, recuerda tu check-in en Bocagrande el 20 de diciembre.',
     'huesped.cali@gohost.co', 0, NULL, NOW()),
    ('ccccccc2-cccc-cccc-cccc-ccccccccccc2', 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '22222222-2222-2222-2222-222222222222',
     'RECORDATORIO_ANFITRION_LLEGADA', 'PROGRAMADO',
     NOW() + INTERVAL '2 days', NULL,
     'Llega huésped a Bocagrande',
     'Juan, tu huésped llega el 20 de diciembre. Verifica limpieza y entrega de llaves.',
     'anfitrion.medellin@gohost.co', 0, NULL, NOW());

-- ===== Token de reseteo (demo) =====
-- Sólo para validar tablas; no usar en producción.
INSERT INTO password_reset_token (id, token, usuario_id, expiracion) VALUES (
    'ddddddd1-dddd-dddd-dddd-ddddddddddd1',
    '$2a$10$yJjwqYw8YQ0k9XJmG3QzUO3fT8oHnO4Ww0eYkK5JwGz1ZbS9O5pCu',
    '33333333-3333-3333-3333-333333333333',
    NOW() + INTERVAL '15 minutes'
);

