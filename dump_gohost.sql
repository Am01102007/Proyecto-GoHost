--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.comentario DROP CONSTRAINT IF EXISTS fkx3yo7h471ojxdy5u8asf1re6;
ALTER TABLE IF EXISTS ONLY public.reserva DROP CONSTRAINT IF EXISTS fkp9qxne23ro42nfkmqg0bb6pj;
ALTER TABLE IF EXISTS ONLY public.notificaciones_recordatorio DROP CONSTRAINT IF EXISTS fkln5axmtl2uj5by7mekfysk5n9;
ALTER TABLE IF EXISTS ONLY public.fotos DROP CONSTRAINT IF EXISTS fkjyy0o49exv77dc2fsqsc2jsq2;
ALTER TABLE IF EXISTS ONLY public.reserva DROP CONSTRAINT IF EXISTS fkh100qw7ej5lbdlqs44lin2wg9;
ALTER TABLE IF EXISTS ONLY public.alojamiento_fotos DROP CONSTRAINT IF EXISTS fkc2uegj0txeurklmx7908lksqi;
ALTER TABLE IF EXISTS ONLY public.password_reset_token DROP CONSTRAINT IF EXISTS fkaehv7qqwsde87cy79hxhy4lke;
ALTER TABLE IF EXISTS ONLY public.alojamiento_servicios DROP CONSTRAINT IF EXISTS fk7or02y5yla00akkwdglvt7g8d;
ALTER TABLE IF EXISTS ONLY public.comentario DROP CONSTRAINT IF EXISTS fk2syg8q6l6q50yags0gmb563wr;
ALTER TABLE IF EXISTS ONLY public.notificaciones_recordatorio DROP CONSTRAINT IF EXISTS fk2cowp3623hel79pid46hrn6ov;
ALTER TABLE IF EXISTS ONLY public.alojamientos DROP CONSTRAINT IF EXISTS fk24js87p4fj4q7pvx2rh62hpyp;
ALTER TABLE IF EXISTS ONLY public.comentario DROP CONSTRAINT IF EXISTS fk1y6cc9drg37g1howu70mod4o2;
ALTER TABLE IF EXISTS ONLY public.comentario DROP CONSTRAINT IF EXISTS fk1gcyoyuhavbjdhqr9hbulwa69;
DROP INDEX IF EXISTS public.idx_reserva_estado;
DROP INDEX IF EXISTS public.idx_reserva_check_out;
DROP INDEX IF EXISTS public.idx_reserva_check_in;
DROP INDEX IF EXISTS public.idx_reserva_alojamiento;
DROP INDEX IF EXISTS public.idx_comentario_autor;
DROP INDEX IF EXISTS public.idx_comentario_alojamiento;
DROP INDEX IF EXISTS public.idx_aloj_ciudad;
DROP INDEX IF EXISTS public.idx_aloj_anfitrion;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_pkey;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_numero_documento_key;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_email_key;
ALTER TABLE IF EXISTS ONLY public.reserva DROP CONSTRAINT IF EXISTS reserva_pkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_token DROP CONSTRAINT IF EXISTS password_reset_token_usuario_id_key;
ALTER TABLE IF EXISTS ONLY public.password_reset_token DROP CONSTRAINT IF EXISTS password_reset_token_token_key;
ALTER TABLE IF EXISTS ONLY public.password_reset_token DROP CONSTRAINT IF EXISTS password_reset_token_pkey;
ALTER TABLE IF EXISTS ONLY public.notificaciones_recordatorio DROP CONSTRAINT IF EXISTS notificaciones_recordatorio_pkey;
ALTER TABLE IF EXISTS ONLY public.fotos DROP CONSTRAINT IF EXISTS fotos_pkey;
ALTER TABLE IF EXISTS ONLY public.comentario DROP CONSTRAINT IF EXISTS comentario_pkey;
ALTER TABLE IF EXISTS ONLY public.alojamientos DROP CONSTRAINT IF EXISTS alojamientos_pkey;
ALTER TABLE IF EXISTS ONLY public.alojamiento_fotos DROP CONSTRAINT IF EXISTS alojamiento_fotos_pkey;
DROP TABLE IF EXISTS public.usuario;
DROP TABLE IF EXISTS public.reserva;
DROP TABLE IF EXISTS public.password_reset_token;
DROP TABLE IF EXISTS public.notificaciones_recordatorio;
DROP TABLE IF EXISTS public.fotos;
DROP TABLE IF EXISTS public.comentario;
DROP TABLE IF EXISTS public.alojamientos;
DROP TABLE IF EXISTS public.alojamiento_servicios;
DROP TABLE IF EXISTS public.alojamiento_fotos;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alojamiento_fotos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alojamiento_fotos (
    orden integer NOT NULL,
    alojamiento_id uuid NOT NULL,
    foto_url character varying(500) NOT NULL
);


--
-- Name: alojamiento_servicios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alojamiento_servicios (
    alojamiento_id uuid NOT NULL,
    servicio character varying(255) NOT NULL,
    CONSTRAINT alojamiento_servicios_servicio_check CHECK (((servicio)::text = ANY ((ARRAY['WIFI'::character varying, 'PISCINA'::character varying, 'MASCOTAS_PERMITIDAS'::character varying, 'AIRE_ACONDICIONADO'::character varying, 'CALEFACCION'::character varying, 'COCINA'::character varying, 'LAVADORA'::character varying, 'SECADORA'::character varying, 'TELEVISION'::character varying, 'NETFLIX'::character varying, 'GIMNASIO'::character varying, 'SPA'::character varying, 'JACUZZI'::character varying, 'BALCON'::character varying, 'TERRAZA'::character varying, 'JARDIN'::character varying, 'PARRILLA'::character varying, 'ESTACIONAMIENTO'::character varying, 'SEGURIDAD_24H'::character varying, 'ASCENSOR'::character varying, 'ACCESO_DISCAPACITADOS'::character varying, 'DESAYUNO_INCLUIDO'::character varying, 'SERVICIO_LIMPIEZA'::character varying, 'RECEPCION_24H'::character varying])::text[])))
);


--
-- Name: alojamientos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alojamientos (
    activo boolean NOT NULL,
    capacidad integer NOT NULL,
    direccion_lat numeric(10,7),
    direccion_lon numeric(10,7),
    precio_noche numeric(10,2) NOT NULL,
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    anfitrion_id uuid NOT NULL,
    id uuid NOT NULL,
    direccion_zip character varying(20),
    direccion_ciudad character varying(120),
    direccion_pais character varying(120),
    direccion_calle character varying(200),
    titulo character varying(200) NOT NULL,
    descripcion text NOT NULL
);


--
-- Name: comentario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.comentario (
    calificacion integer NOT NULL,
    creado_en timestamp(6) without time zone,
    alojamiento_id uuid NOT NULL,
    autor_id uuid NOT NULL,
    id uuid NOT NULL,
    respondido_por_id uuid,
    respuesta character varying(255),
    texto character varying(255) NOT NULL,
    respondido_por uuid,
    CONSTRAINT comentario_calificacion_check CHECK (((calificacion <= 5) AND (calificacion >= 1)))
);


--
-- Name: fotos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fotos (
    alojamiento_id uuid NOT NULL,
    id uuid NOT NULL,
    url character varying(500) NOT NULL
);


--
-- Name: notificaciones_recordatorio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notificaciones_recordatorio (
    intentos_envio integer NOT NULL,
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    fecha_enviado timestamp(6) without time zone,
    fecha_programada timestamp(6) without time zone NOT NULL,
    destinatario_id uuid NOT NULL,
    id uuid NOT NULL,
    reserva_id uuid NOT NULL,
    asunto character varying(200) NOT NULL,
    email_destinatario character varying(255) NOT NULL,
    estado character varying(255) NOT NULL,
    mensaje text NOT NULL,
    mensaje_error text,
    tipo_recordatorio character varying(255) NOT NULL,
    CONSTRAINT notificaciones_recordatorio_estado_check CHECK (((estado)::text = ANY ((ARRAY['PROGRAMADO'::character varying, 'ENVIADO'::character varying, 'ERROR'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT notificaciones_recordatorio_tipo_recordatorio_check CHECK (((tipo_recordatorio)::text = ANY ((ARRAY['RECORDATORIO_HUESPED_CHECKIN'::character varying, 'RECORDATORIO_ANFITRION_LLEGADA'::character varying, 'RECORDATORIO_HUESPED_DIA_CHECKIN'::character varying, 'RECORDATORIO_ANFITRION_DIA_LLEGADA'::character varying])::text[])))
);


--
-- Name: password_reset_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_token (
    expiracion timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: reserva; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reserva (
    check_in date NOT NULL,
    check_out date NOT NULL,
    eliminada boolean NOT NULL,
    numero_huespedes integer NOT NULL,
    alojamiento_id uuid NOT NULL,
    huesped_id uuid NOT NULL,
    id uuid NOT NULL,
    estado character varying(255),
    CONSTRAINT reserva_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'CONFIRMADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT reserva_numero_huespedes_check CHECK ((numero_huespedes >= 1))
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    activo boolean NOT NULL,
    fecha_nacimiento date,
    latitud double precision,
    longitud double precision,
    actualizado_en timestamp(6) without time zone,
    creado_en timestamp(6) without time zone,
    id uuid NOT NULL,
    numero_documento character varying(40),
    nombre character varying(80) NOT NULL,
    foto_perfil character varying(500),
    apellidos character varying(255),
    ciudad character varying(255),
    direccion character varying(255),
    email character varying(255) NOT NULL,
    pais character varying(255),
    password character varying(255) NOT NULL,
    rol character varying(255),
    telefono character varying(255),
    tipo_documento character varying(255),
    CONSTRAINT usuario_rol_check CHECK (((rol)::text = ANY ((ARRAY['HUESPED'::character varying, 'ANFITRION'::character varying, 'ADMIN'::character varying])::text[]))),
    CONSTRAINT usuario_tipo_documento_check CHECK (((tipo_documento)::text = ANY ((ARRAY['CC'::character varying, 'CE'::character varying, 'PASAPORTE'::character varying, 'NIT'::character varying, 'DNI'::character varying])::text[])))
);


--
-- Data for Name: alojamiento_fotos; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.alojamiento_fotos (orden, alojamiento_id, foto_url) FROM stdin;
0	04d47aae-6dec-4bad-baca-20814c470c9c	https://images.unsplash.com/photo-1522708323590-d24dbb6b0267
1	04d47aae-6dec-4bad-baca-20814c470c9c	https://images.unsplash.com/photo-1484154218962-a197022b5858
0	9d9b3b59-8f90-4d90-abd8-0daf2c6732c5	https://picsum.photos/seed/Apartamento%20en%20Cartagena%20-cover/800/500
0	576d2f2a-9b9d-41a0-959a-54c30f429562	https://picsum.photos/seed/Apartamento%20en%20Cartagena%20-cover/800/500
0	caea6cb1-c492-4fd7-ba69-0c8854d845dc	https://picsum.photos/seed/Apartamento%20en%20Cartagena-0/800/500
1	caea6cb1-c492-4fd7-ba69-0c8854d845dc	https://picsum.photos/seed/Apartamento%20en%20Cartagena-1/800/500
2	caea6cb1-c492-4fd7-ba69-0c8854d845dc	https://picsum.photos/seed/Apartamento%20en%20Cartagena-2/800/500
0	8db3deeb-b851-4abf-876d-d6025065ad26	https://picsum.photos/800/600?random=201
0	83240f9f-74d9-4447-95f1-79308f459061	https://picsum.photos/800/600?random=202
0	f409a190-fc6c-4acd-803e-5833e03be601	https://picsum.photos/800/600
0	48014a64-ea37-471d-b15a-7bc27be61470	https://example.com/img1.jpg
1	48014a64-ea37-471d-b15a-7bc27be61470	https://example.com/img1.jpg
0	eca14524-991c-4ed6-a1cf-f8f92a341346	https://example.com/apto.jpg
1	eca14524-991c-4ed6-a1cf-f8f92a341346	https://example.com/apto.jpg
0	6e278da9-9d7c-446c-af44-7d5f19690d73	https://example.com/apto.jpg
1	6e278da9-9d7c-446c-af44-7d5f19690d73	https://example.com/apto.jpg
0	a3810195-30f2-43ce-8db8-9f63c8962786	https://example.com/apto.jpg
1	a3810195-30f2-43ce-8db8-9f63c8962786	https://example.com/apto.jpg
0	227a3c36-d4f0-4b4d-b0db-518a06db58a4	https://example.com/img1.jpg
1	227a3c36-d4f0-4b4d-b0db-518a06db58a4	https://example.com/img1.jpg
0	aa26f2b9-2b4f-4c2b-b38c-04a405b75052	https://example.com/apto.jpg
1	aa26f2b9-2b4f-4c2b-b38c-04a405b75052	https://example.com/apto.jpg
\.


--
-- Data for Name: alojamiento_servicios; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.alojamiento_servicios (alojamiento_id, servicio) FROM stdin;
04d47aae-6dec-4bad-baca-20814c470c9c	WIFI
04d47aae-6dec-4bad-baca-20814c470c9c	AIRE_ACONDICIONADO
04d47aae-6dec-4bad-baca-20814c470c9c	COCINA
04d47aae-6dec-4bad-baca-20814c470c9c	TELEVISION
04d47aae-6dec-4bad-baca-20814c470c9c	NETFLIX
04d47aae-6dec-4bad-baca-20814c470c9c	ESTACIONAMIENTO
04d47aae-6dec-4bad-baca-20814c470c9c	ASCENSOR
48014a64-ea37-471d-b15a-7bc27be61470	WIFI
48014a64-ea37-471d-b15a-7bc27be61470	COCINA
eca14524-991c-4ed6-a1cf-f8f92a341346	WIFI
6e278da9-9d7c-446c-af44-7d5f19690d73	WIFI
a3810195-30f2-43ce-8db8-9f63c8962786	WIFI
227a3c36-d4f0-4b4d-b0db-518a06db58a4	WIFI
227a3c36-d4f0-4b4d-b0db-518a06db58a4	COCINA
aa26f2b9-2b4f-4c2b-b38c-04a405b75052	WIFI
\.


--
-- Data for Name: alojamientos; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.alojamientos (activo, capacidad, direccion_lat, direccion_lon, precio_noche, fecha_actualizacion, fecha_creacion, anfitrion_id, id, direccion_zip, direccion_ciudad, direccion_pais, direccion_calle, titulo, descripcion) FROM stdin;
t	4	\N	\N	160000.00	2025-10-29 11:14:29.908001	2025-10-29 11:13:27.512764	12a80b15-abce-47df-956b-ab2c6878fcb2	04d47aae-6dec-4bad-baca-20814c470c9c	050021	Medellín	Colombia	Carrera 43A #14-15	Apartamento Moderno en Zona Rosa - ACTUALIZADO	Descripción actualizada...
f	1	\N	\N	280.00	2025-11-01 12:53:23.404088	2025-11-01 12:15:33.731866	12a80b15-abce-47df-956b-ab2c6878fcb2	9d9b3b59-8f90-4d90-abd8-0daf2c6732c5	\N	Cartagena 	Colombia	Cra 2 #4-18, Bocagrande	Apartamento en Cartagena 	Apartamento en el sector de boca grande 
f	4	\N	\N	280.00	2025-11-01 12:53:28.55327	2025-11-01 12:16:01.434796	12a80b15-abce-47df-956b-ab2c6878fcb2	576d2f2a-9b9d-41a0-959a-54c30f429562	\N	Cartagena 	Colombia	Cra 2 #4-18, Bocagrande	Apartamento en Cartagena 	Apartamento en el sector de boca grande 
f	2	\N	\N	200.00	2025-11-01 12:59:56.453941	2025-11-01 12:59:42.641568	12a80b15-abce-47df-956b-ab2c6878fcb2	caea6cb1-c492-4fd7-ba69-0c8854d845dc	\N	Cartagena	Colombia	Calle  del Porvenir 6 35-38, Centro, 130001	Apartamento en Cartagena	Hermoso apartamento en Cartagena de Indias 
t	2	\N	\N	120.00	2025-11-02 11:33:32.130138	2025-11-02 11:33:32.130138	eceb1615-a2f9-4641-a985-0c33efd46703	8db3deeb-b851-4abf-876d-d6025065ad26	110111	Bogotá	Colombia	Calle 59 # 7-23	Apartamento en Bogotá Chapinero	Luminoso y cómodo cerca de la Zona G.
t	2	\N	\N	90.00	2025-11-02 11:33:32.281014	2025-11-02 11:33:32.281014	eceb1615-a2f9-4641-a985-0c33efd46703	83240f9f-74d9-4447-95f1-79308f459061	050021	Medellín	Colombia	Cra. 43A # 7-50	Estudio en Medellín El Poblado	Moderno estudio cerca del Parque Lleras.
t	2	\N	\N	100000.00	2025-11-06 12:16:42.48504	2025-11-06 12:16:42.48504	d4e15a91-6f5e-4659-b7d6-2f46ad06b5df	f409a190-fc6c-4acd-803e-5833e03be601	050010	Medellín	Colombia	Calle Falsa 123	E2E Alojamiento 1762449402455	Alojamiento generado por prueba E2E
t	3	\N	\N	120.00	2025-11-10 15:30:01.722327	2025-11-10 15:30:01.722327	f404bf1d-6111-4686-aa94-b71293aec0f8	48014a64-ea37-471d-b15a-7bc27be61470	630001	Armenia	Colombia	Calle 10 #20-30	Casa centro	Cómodo alojamiento en el centro
t	4	\N	\N	150.00	2025-11-10 15:30:07.714827	2025-11-10 15:30:07.714827	5f96dcf8-6918-42b9-b22f-ced1464eaecd	eca14524-991c-4ed6-a1cf-f8f92a341346	630002	Armenia	Colombia	Calle 20 #10-15	Apto moderno	Cerca al parque
t	4	\N	\N	150.00	2025-11-10 15:32:53.362752	2025-11-10 15:32:53.362752	5e76610e-2e97-4644-ab16-1a4316c388d4	6e278da9-9d7c-446c-af44-7d5f19690d73	630002	Armenia	Colombia	Calle 20 #10-15	Apto moderno	Cerca al parque
t	4	\N	\N	150.00	2025-11-10 15:33:40.299733	2025-11-10 15:33:40.299733	2f5fa783-b79c-4941-a266-1a4421d81a6d	a3810195-30f2-43ce-8db8-9f63c8962786	630002	Armenia	Colombia	Calle 20 #10-15	Apto moderno	Cerca al parque
t	3	\N	\N	120.00	2025-11-10 15:36:58.820258	2025-11-10 15:36:58.820258	c411abb5-b816-4931-981f-33d8979f326f	227a3c36-d4f0-4b4d-b0db-518a06db58a4	630001	Armenia	Colombia	Calle 10 #20-30	Casa centro	Cómodo alojamiento en el centro
t	4	\N	\N	150.00	2025-11-10 15:37:08.07392	2025-11-10 15:37:08.07392	2abb4c41-1bf9-4ef4-a3b7-8e40d6ac7b79	aa26f2b9-2b4f-4c2b-b38c-04a405b75052	630002	Armenia	Colombia	Calle 20 #10-15	Apto moderno	Cerca al parque
t	2	\N	\N	100000.00	2025-11-10 16:06:23.56637	2025-11-10 16:06:23.56637	974fe7c7-f98c-4e8f-8096-f1dfe9174c40	43a064e3-f519-41b5-9a46-c874f62497d4	110111	Bogota	CO	Calle 123 #45-67	Alojamiento de prueba	Prueba Cloudinary
t	2	\N	\N	123.45	2025-11-10 16:16:31.044952	2025-11-10 16:16:31.044952	33244249-e4aa-47a0-a7b5-674db69464f6	d83e5c48-d543-4ab8-8e4f-785a7727d64e	630001	Armenia	Colombia	Calle 1 #2-3	Test con imagen 20251110111624	Entrada de prueba
\.


--
-- Data for Name: comentario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.comentario (calificacion, creado_en, alojamiento_id, autor_id, id, respondido_por_id, respuesta, texto, respondido_por) FROM stdin;
5	2025-11-06 12:07:27.186173	9d9b3b59-8f90-4d90-abd8-0daf2c6732c5	12a98de3-3f06-4fc9-86b8-aac81c5fa3ef	0392fdf6-a88f-4832-bb52-8f6abd7859ee	\N	\N	Buen lugar	\N
\.


--
-- Data for Name: fotos; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fotos (alojamiento_id, id, url) FROM stdin;
\.


--
-- Data for Name: notificaciones_recordatorio; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notificaciones_recordatorio (intentos_envio, fecha_actualizacion, fecha_creacion, fecha_enviado, fecha_programada, destinatario_id, id, reserva_id, asunto, email_destinatario, estado, mensaje, mensaje_error, tipo_recordatorio) FROM stdin;
\.


--
-- Data for Name: password_reset_token; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.password_reset_token (expiracion, id, usuario_id, token) FROM stdin;
2025-11-06 09:45:19.20257	c439e7e8-7ddf-44bb-8993-3c6ef399b0ec	6284b73b-0d82-4afd-9ae2-666b3ff53d79	$2a$12$wWGnArDVi0rKbBsjf3oCDueMsPfz1UfzdO/9wP8ofkzbFuB0ZclAK
2025-11-06 12:19:11.131034	41a8cafb-dca1-46ff-8bde-59c9efe22040	12a98de3-3f06-4fc9-86b8-aac81c5fa3ef	$2a$12$BnKWfmWP/U85olNu4vvHeuoMuBYurlCacnJLQXWutNMrSIWXrNdk.
\.


--
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.reserva (check_in, check_out, eliminada, numero_huespedes, alojamiento_id, huesped_id, id, estado) FROM stdin;
2025-11-03	2025-11-08	f	2	04d47aae-6dec-4bad-baca-20814c470c9c	19b8d2fe-68fd-49c6-8397-647df465c0cf	511167b6-b4d4-4a54-8a7a-234174f4a25d	CONFIRMADA
2025-12-20	2025-12-25	f	2	8db3deeb-b851-4abf-876d-d6025065ad26	5374b98d-e0ce-4000-a492-c6098bf0ef30	5c82720a-6873-4f6b-8b61-4d47811b85c3	PENDIENTE
2025-11-15	2025-11-18	t	2	eca14524-991c-4ed6-a1cf-f8f92a341346	6907829f-6f96-4b6b-bb53-191dd281dea9	9c2a5577-c982-4381-a3a0-ce97d234045a	CANCELADA
2025-11-15	2025-11-18	t	2	6e278da9-9d7c-446c-af44-7d5f19690d73	c4769561-211c-42b7-a897-de2cee5554fa	3745dbc5-4b4d-4cbb-8084-3ad45dadb414	CANCELADA
2025-11-15	2025-11-18	t	2	a3810195-30f2-43ce-8db8-9f63c8962786	72b67973-02aa-4651-ab4a-2f731e65fec6	7c5bac9a-72a3-4a1d-9184-507c947ccfab	CANCELADA
2025-11-15	2025-11-18	t	2	aa26f2b9-2b4f-4c2b-b38c-04a405b75052	81ce6060-62f0-4640-aa55-2c59e08bc915	bed1adea-d591-4100-b84d-fd9ff6ac84bc	CANCELADA
\.


--
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.usuario (activo, fecha_nacimiento, latitud, longitud, actualizado_en, creado_en, id, numero_documento, nombre, foto_perfil, apellidos, ciudad, direccion, email, pais, password, rol, telefono, tipo_documento) FROM stdin;
t	\N	\N	\N	2025-10-29 10:55:19.338719	2025-10-29 10:55:19.338719	4857c94e-2294-4c23-a413-29f6cb730808	\N	Usuario Prueba	\N	\N	\N	\N	prueba@test.com	\N	password123	HUESPED	\N	\N
t	1990-08-22	\N	\N	2025-10-29 11:09:32.843245	2025-10-29 11:07:16.886826	12a80b15-abce-47df-956b-ab2c6878fcb2	454321234	María	\N	Rodríguez Pérez	Armenia	\N	anfitrionmariaR@example.com	Colombia	$2a$12$seyDwbTzdpiroiL3r5nmQ.UcLfwY/ycdzVidOPRyub4C2ggbw.SHu	ANFITRION	31094576	CC
t	1990-01-01	\N	\N	2025-11-10 15:33:48.560109	2025-11-10 15:33:47.684465	f8e267dd-ecdf-4020-a0a9-1f35228c2657	057df387	Test	\N	User	Armenia	\N	user.reset+ee9477f2-347e-4eb1-b301-583836b77f7f@example.com	Colombia	$2a$12$2C5QtaLnMBa/qjjrMDzvEOxFS58l1IOtYxfdQS9UxdFR9Kgwv3sSW	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:36:58.000513	c411abb5-b816-4931-981f-33d8979f326f	d9fe8b42	Test	\N	User	Armenia	\N	host.metrics+ec6dfd4f-841b-469c-a90f-e148cf13092f@example.com	Colombia	$2a$12$xZykB9Tju0QGTSnkR9dZ2OLTzEUy3uaFWI0T4gNCg0wpjWSmsLxLS	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-02 11:33:29.259999	eceb1615-a2f9-4641-a985-0c33efd46703	99999901	Test	\N	User	Bogotá	\N	testuser1@example.com	Colombia	$2a$12$qD28lUIxTsPGHVFvLkAaEOWH0IzlQw8akQDF3iuBbISqk68IMpUQO	ANFITRION	3000000000	CC
t	1990-01-01	\N	\N	\N	2025-11-02 11:33:30.712661	5374b98d-e0ce-4000-a492-c6098bf0ef30	99999902	Test	\N	User	Medellín	\N	testuser2@example.com	Colombia	$2a$12$riwbdlsgwpyVp3t13ZoifOJ4gVqfobPAXlSCWYXIRHged1vHlTbGu	HUESPED	3000000000	CC
t	1990-01-01	\N	\N	\N	2025-11-06 09:30:05.341026	6284b73b-0d82-4afd-9ae2-666b3ff53d79	RST000005	Reset User5	\N	Demo	Cali	\N	reset.user5@example.com	CO	$2a$12$gTVH5BUY.u5rw7s6yYslJus7esuef8BNDz9J0nRNQfZUx58dfai4.	HUESPED	123456789	DNI
t	1990-01-01	\N	\N	\N	2025-11-10 15:36:59.387302	2098498e-c3b8-49f8-8243-c1981ca0734f	12ada5e3	Test	\N	User	Armenia	\N	it.huesped+c460719b-a9bb-43e3-bf70-dcca29c3a9a7@example.com	Colombia	$2a$12$jjxsUO1ne6Dn.cBylPY9bOdsAb2agl3vbrMuG/bd.1hQ9udt51wdO	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:37:00.090434	8eb49634-c1de-4773-8232-051b6782e69f	4edc7d38	Test	\N	User	Armenia	\N	it.anfitrion+28164e20-b146-4795-9d57-7a0dce96fe09@example.com	Colombia	$2a$12$/3RXP.xpfAABcARvrOOOeeCYUtfQkM/qrpErjX89tl1JGR/qrczae	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-06 12:16:42.144047	d4e15a91-6f5e-4659-b7d6-2f46ad06b5df	970755433	E2E	\N	Tester	Bogotá	\N	e2e_1762449401773@example.com	Colombia	$2a$12$BGuB2gbN7UuJf3T.jyVMX.grLPSQBdQA94HWE6RaznXy8U7p3zcQW	ANFITRION	3000000000	CC
t	1990-01-01	\N	\N	2025-11-06 12:23:18.027906	2025-11-06 12:01:17.266548	12a98de3-3f06-4fc9-86b8-aac81c5fa3ef	DEL-2f422ce4	ELIMINADO	https://example.com/placeholder.png	USUARIO	-	\N	smoketest.user3@example.com	-	$2a$12$lzwiX4jcw9zDkEOuwdITY.A893FBYSluM/Rl89dTEzpyp/HbpeE16	HUESPED	-	CC
t	1985-03-20	\N	\N	2025-11-10 15:17:22.343439	2025-10-29 11:06:50.386651	19b8d2fe-68fd-49c6-8397-647df465c0cf	87654321	Juan	\N	González Aguirre	Medellín	\N	juanjosediazgiraldo7@gmail.com	Colombia	$2a$12$3qNWb7I16jGCAAmHcZimEuBdVSKlJHDwv9e0j4ib.MDCpiX3Ro7OS	HUESPED	3009876543	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:01.152269	f404bf1d-6111-4686-aa94-b71293aec0f8	ae781324	Test	\N	User	Armenia	\N	host.metrics@example.com	Colombia	$2a$12$M3/aFyDr925A2PzjZzXcaOT3x7Z/T2gT4CEonJJBbG.MgZxRDbSzq	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:02.219562	e0ffdd70-8f20-4b54-9caf-c6d5e23a0cac	86a30eac	Test	\N	User	Armenia	\N	it.huesped@example.com	Colombia	$2a$12$2.k2c.pgst5.PylrUPdvi.97SXFOAQRFbiC94lPErOw1ciEYi4h9q	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:02.895593	013ca786-035d-414b-9705-e72f96e7fa3c	ce64ee40	Test	\N	User	Armenia	\N	it.anfitrion@example.com	Colombia	$2a$12$keiym8lwMSAa82FNGBOUjOe/3Ui833brttPBJ93iAP9sc/oqV3Oaq	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:06.588021	61f3b653-fb7b-4812-bb35-a81dec4296a5	cfcca5e5	Test	\N	User	Armenia	\N	img.user@example.com	Colombia	$2a$12$NMKtZFWdkQjsRlJgQgMUae0M.uLo0iWuyG8CGeJ6iqJBpfZbJT2Oq	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:07.335686	5f96dcf8-6918-42b9-b22f-ced1464eaecd	a1f12395	Test	\N	User	Armenia	\N	host.reserva@example.com	Colombia	$2a$12$MxZ73E8vnJu2ItOyerXNVuvNWWuWpyKBZw1VFwsqUaEktvfN88Z.6	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:30:08.208368	6907829f-6f96-4b6b-bb53-191dd281dea9	283cb1b2	Test	\N	User	Armenia	\N	huesped.reserva@example.com	Colombia	$2a$12$m69uqgRogT1DsOgHD.QYK.UB5xiI.qnGCUQyqwcTzzGkKu38sYntS	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	2025-11-10 15:30:17.694298	2025-11-10 15:30:16.625679	d1c5da26-6ff5-461f-a3e1-5d45cbb9e421	272db46b	Test	\N	User	Armenia	\N	user.reset@example.com	Colombia	$2a$12$Q411x3GuxkFeUSUcS2piTe6dFBHmyjaSz6m/w4cjSfg4.atoIUQHa	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:32:48.957472	97be46ec-3516-468e-8225-f13c2aa130f2	cab7492c	Test	\N	User	Armenia	\N	img.user+cf236471-bc98-44ce-aba3-e6ad80745a3b@example.com	Colombia	$2a$12$S4P2t4DKb/uZZhXS5jAhROmLGF56R3EOGGXNgu3Qin1HyUoorsPPy	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:32:52.868636	5e76610e-2e97-4644-ab16-1a4316c388d4	1b3f375f	Test	\N	User	Armenia	\N	host.reserva+7979a168-fd0e-4066-8f82-dfee06e8454f@example.com	Colombia	$2a$12$69D.IURLwKbBBoctYEG/6.bY5fK9M8ctqsg4d6xqXTTugRwzfgmAC	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:32:53.73261	c4769561-211c-42b7-a897-de2cee5554fa	1aa43023	Test	\N	User	Armenia	\N	huesped.reserva+6397394d-cd08-4968-9641-146ab6332cf1@example.com	Colombia	$2a$12$dtX.fQ07rZBLTAqbwSGR7O7oZos2FnJ4rK7m8N0wxsIYRU6Y/dzwy	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	2025-11-10 15:33:01.573909	2025-11-10 15:33:00.70888	78e18152-047c-4516-b7d1-da1a01999511	f6b521de	Test	\N	User	Armenia	\N	user.reset+23b9850e-321b-4578-b13a-7b4cd937846d@example.com	Colombia	$2a$12$Dit1dIcC26d51VTEKxiYq.Nh6/EMOEME1XE6CQeCq26KTkStXp2iq	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:33:38.943445	ece58037-3147-4b5f-96dd-5b20ddb01c94	45b32364	Test	\N	User	Armenia	\N	img.user+6e24cb4c-25bf-445a-9ffa-edf53813a7a2@example.com	Colombia	$2a$12$Ki9twU.ec494P5s1bS1FY.TUVzd0IlrfSnIqtx7YIQUcDz9rtnv3y	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:33:39.896545	2f5fa783-b79c-4941-a266-1a4421d81a6d	3d0c215d	Test	\N	User	Armenia	\N	host.reserva+0605d4b1-ffde-4854-81cf-95f44ed04db4@example.com	Colombia	$2a$12$zCAPhvTMbH6M.a9AKXx5eukILHrrfr29y5rMwCjmNUjQMbSAroy.2	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:33:40.666942	72b67973-02aa-4651-ab4a-2f731e65fec6	21d0c699	Test	\N	User	Armenia	\N	huesped.reserva+635a4b54-7e83-4ab4-84a3-f88a5c32ba01@example.com	Colombia	$2a$12$6hJ6efRZ1zH6Zcv0FfbnQunPLU2Whh0LSkbhQ6ODS.G2ja92KDhxW	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:37:06.210257	57efd419-2197-4335-bcba-5af0a883a265	a9a6d4a4	Test	\N	User	Armenia	\N	img.user+0ab8459d-18ff-4f5b-bafe-1e92fd47a8f1@example.com	Colombia	$2a$12$puV5S9QwJkK4Wr4vfLWFauV7QZ/xCE7tYEoWUlIjGISqvkyc35TzK	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:37:07.456774	2abb4c41-1bf9-4ef4-a3b7-8e40d6ac7b79	ed04bba2	Test	\N	User	Armenia	\N	host.reserva+a2fba8aa-4b71-4e54-a261-ec8e975db87a@example.com	Colombia	$2a$12$eGr54ivZfM/BVAZ8z1VTj.HickEYd8tpwDZ8sWSHA6Lmbi1P/OuKG	ANFITRION	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 15:37:08.641972	81ce6060-62f0-4640-aa55-2c59e08bc915	55f90560	Test	\N	User	Armenia	\N	huesped.reserva+4fe45ee8-6310-471e-8a7b-8a7d4ed436c4@example.com	Colombia	$2a$12$GXjW8QGYpF3.5wX.lgcPZeWFyQdvw0NxI7iGj9P05YPK7S8JcOBfK	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	2025-11-10 15:37:18.71176	2025-11-10 15:37:17.349773	be15ef51-24c5-429d-a4f7-b9edb1ec3d87	db8ae9aa	Test	\N	User	Armenia	\N	user.reset+c401e766-f240-4ce1-8768-e5aea04c063d@example.com	Colombia	$2a$12$8P4YqfZz8McAOgLvHHVNl.5ACjkRoDonOIs07iUjOe6bHWF9ToorW	HUESPED	3001234567	CC
t	1990-01-01	\N	\N	\N	2025-11-10 16:06:16.016754	974fe7c7-f98c-4e8f-8096-f1dfe9174c40	08012c79-2e04-4abc-9e86-daacd96c0d2e	Cloud Test	\N	Upload	Bogota	\N	cloudinary.it.ce8d23ab-f2cc-4bf5-aa30-bc56179bcd17@example.com	CO	$2a$12$VVF6Ewm.9/tEvUhQaPya/.8CsRNoLvYH5rx2k2dHTwqOTQJRHNmiq	ANFITRION	3000000000	CC
t	1990-01-01	\N	\N	\N	2025-11-10 16:16:26.03505	33244249-e4aa-47a0-a7b5-674db69464f6	959960748	Test	\N	Host	Armenia	\N	host_20251110111624@example.com	Colombia	$2a$12$Naug25c6hlCcanKRHhMZ2.IUVpujzLFIDkpNG3xryjWfaC1WSAtV6	ANFITRION	3000000000	CC
t	1990-01-01	\N	\N	\N	2025-11-10 16:16:56.703313	4acf06d1-d492-44eb-bbd6-c83e146c5bad	999999999	Test	\N	Host	Armenia	\N	host_20251110111656@example.com	Colombia	$2a$12$dJDYzWYcKHO8VglHuFv9NuUckhDqVjNT376X/LGsRm/fSGrLiRMSe	ANFITRION	3000000000	CC
t	1990-01-01	\N	\N	\N	2025-11-10 18:35:09.369461	eb7c3e0e-a6fd-4313-91e9-3fd26b50fd09	HM-123456	Host	\N	Multipart	Medellin	\N	host.multi@example.com	Colombia	$2a$12$seI9OVJVzikJdZ5a1aqCO.6VcBUcyQJ4lkJUjLuMLjwyg2BckauaW	ANFITRION	+57 3000000000	CC
t	1992-02-02	\N	\N	2025-11-10 18:38:07.062365	2025-11-10 18:35:13.923004	4a246efd-8833-45c3-93a4-10137bad6198	GM-654321	Guest Actualizado	\N	Multipart	Cali	\N	guest.multi@example.com	Colombia	$2a$12$BHTtxDsGjVyeRZwKBPJKkekkPZizF9rNWAtLNBbaQa06wiWxO69Si	HUESPED	+57 3111111111	CC
\.


--
-- Name: alojamiento_fotos alojamiento_fotos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamiento_fotos
    ADD CONSTRAINT alojamiento_fotos_pkey PRIMARY KEY (orden, alojamiento_id);


--
-- Name: alojamientos alojamientos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamientos
    ADD CONSTRAINT alojamientos_pkey PRIMARY KEY (id);


--
-- Name: comentario comentario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comentario
    ADD CONSTRAINT comentario_pkey PRIMARY KEY (id);


--
-- Name: fotos fotos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fotos
    ADD CONSTRAINT fotos_pkey PRIMARY KEY (id);


--
-- Name: notificaciones_recordatorio notificaciones_recordatorio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones_recordatorio
    ADD CONSTRAINT notificaciones_recordatorio_pkey PRIMARY KEY (id);


--
-- Name: password_reset_token password_reset_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_pkey PRIMARY KEY (id);


--
-- Name: password_reset_token password_reset_token_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_token_key UNIQUE (token);


--
-- Name: password_reset_token password_reset_token_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_usuario_id_key UNIQUE (usuario_id);


--
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);


--
-- Name: usuario usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


--
-- Name: usuario usuario_numero_documento_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_numero_documento_key UNIQUE (numero_documento);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- Name: idx_aloj_anfitrion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_aloj_anfitrion ON public.alojamientos USING btree (anfitrion_id);


--
-- Name: idx_aloj_ciudad; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_aloj_ciudad ON public.alojamientos USING btree (direccion_ciudad);


--
-- Name: idx_comentario_alojamiento; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comentario_alojamiento ON public.comentario USING btree (alojamiento_id);


--
-- Name: idx_comentario_autor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comentario_autor ON public.comentario USING btree (autor_id);


--
-- Name: idx_reserva_alojamiento; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_alojamiento ON public.reserva USING btree (alojamiento_id);


--
-- Name: idx_reserva_check_in; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_check_in ON public.reserva USING btree (check_in);


--
-- Name: idx_reserva_check_out; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_check_out ON public.reserva USING btree (check_out);


--
-- Name: idx_reserva_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_estado ON public.reserva USING btree (estado);


--
-- Name: comentario fk1gcyoyuhavbjdhqr9hbulwa69; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comentario
    ADD CONSTRAINT fk1gcyoyuhavbjdhqr9hbulwa69 FOREIGN KEY (autor_id) REFERENCES public.usuario(id);


--
-- Name: comentario fk1y6cc9drg37g1howu70mod4o2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comentario
    ADD CONSTRAINT fk1y6cc9drg37g1howu70mod4o2 FOREIGN KEY (respondido_por_id) REFERENCES public.usuario(id);


--
-- Name: alojamientos fk24js87p4fj4q7pvx2rh62hpyp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamientos
    ADD CONSTRAINT fk24js87p4fj4q7pvx2rh62hpyp FOREIGN KEY (anfitrion_id) REFERENCES public.usuario(id);


--
-- Name: notificaciones_recordatorio fk2cowp3623hel79pid46hrn6ov; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones_recordatorio
    ADD CONSTRAINT fk2cowp3623hel79pid46hrn6ov FOREIGN KEY (reserva_id) REFERENCES public.reserva(id);


--
-- Name: comentario fk2syg8q6l6q50yags0gmb563wr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comentario
    ADD CONSTRAINT fk2syg8q6l6q50yags0gmb563wr FOREIGN KEY (alojamiento_id) REFERENCES public.alojamientos(id);


--
-- Name: alojamiento_servicios fk7or02y5yla00akkwdglvt7g8d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamiento_servicios
    ADD CONSTRAINT fk7or02y5yla00akkwdglvt7g8d FOREIGN KEY (alojamiento_id) REFERENCES public.alojamientos(id);


--
-- Name: password_reset_token fkaehv7qqwsde87cy79hxhy4lke; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT fkaehv7qqwsde87cy79hxhy4lke FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);


--
-- Name: alojamiento_fotos fkc2uegj0txeurklmx7908lksqi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamiento_fotos
    ADD CONSTRAINT fkc2uegj0txeurklmx7908lksqi FOREIGN KEY (alojamiento_id) REFERENCES public.alojamientos(id);


--
-- Name: reserva fkh100qw7ej5lbdlqs44lin2wg9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT fkh100qw7ej5lbdlqs44lin2wg9 FOREIGN KEY (huesped_id) REFERENCES public.usuario(id);


--
-- Name: fotos fkjyy0o49exv77dc2fsqsc2jsq2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fotos
    ADD CONSTRAINT fkjyy0o49exv77dc2fsqsc2jsq2 FOREIGN KEY (alojamiento_id) REFERENCES public.alojamientos(id);


--
-- Name: notificaciones_recordatorio fkln5axmtl2uj5by7mekfysk5n9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificaciones_recordatorio
    ADD CONSTRAINT fkln5axmtl2uj5by7mekfysk5n9 FOREIGN KEY (destinatario_id) REFERENCES public.usuario(id);


--
-- Name: reserva fkp9qxne23ro42nfkmqg0bb6pj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT fkp9qxne23ro42nfkmqg0bb6pj FOREIGN KEY (alojamiento_id) REFERENCES public.alojamientos(id);


--
-- Name: comentario fkx3yo7h471ojxdy5u8asf1re6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comentario
    ADD CONSTRAINT fkx3yo7h471ojxdy5u8asf1re6 FOREIGN KEY (respondido_por) REFERENCES public.usuario(id);


--
-- PostgreSQL database dump complete
--

