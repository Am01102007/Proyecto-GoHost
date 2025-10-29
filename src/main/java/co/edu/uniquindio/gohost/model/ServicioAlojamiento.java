package co.edu.uniquindio.gohost.model;

/**
 * Enum que representa los servicios/amenidades disponibles en un alojamiento.
 */
public enum ServicioAlojamiento {
    WIFI("WiFi"),
    PISCINA("Piscina"),
    MASCOTAS_PERMITIDAS("Mascotas permitidas"),
    AIRE_ACONDICIONADO("Aire acondicionado"),
    CALEFACCION("Calefacción"),
    COCINA("Cocina"),
    LAVADORA("Lavadora"),
    SECADORA("Secadora"),
    TELEVISION("Televisión"),
    NETFLIX("Netflix"),
    GIMNASIO("Gimnasio"),
    SPA("Spa"),
    JACUZZI("Jacuzzi"),
    BALCON("Balcón"),
    TERRAZA("Terraza"),
    JARDIN("Jardín"),
    PARRILLA("Parrilla"),
    ESTACIONAMIENTO("Estacionamiento"),
    SEGURIDAD_24H("Seguridad 24h"),
    ASCENSOR("Ascensor"),
    ACCESO_DISCAPACITADOS("Acceso para discapacitados"),
    DESAYUNO_INCLUIDO("Desayuno incluido"),
    SERVICIO_LIMPIEZA("Servicio de limpieza"),
    RECEPCION_24H("Recepción 24h");

    private final String descripcion;

    ServicioAlojamiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}