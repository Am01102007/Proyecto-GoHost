# GoHost — Configuración de entorno

Este proyecto usa Spring Boot 3 y **Neon (PostgreSQL)** como base de datos.
Las credenciales y secretos se toman **exclusivamente desde variables de entorno**.

## Variables de entorno

Consulta `.env.example` para ver todas las variables requeridas. No compartas ni subas credenciales reales.

Variables clave:
- `SPRING_PROFILES_ACTIVE`: `dev`, `test` o `prod`.
- `SPRING_DATASOURCE_URL`: cadena JDBC de Neon (incluye `sslmode=require`).
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`.
- `JWT_SECRET`: secreto para tokens.

## Perfiles

- `application.yml`: configuración base sin secretos.
- `application-dev.yml`: configuración de desarrollo (usa Neon por entorno).
- `application-test.yml`: perfil de pruebas usando Neon (precaución con datos reales).
- `application-prod.yml`: configuración de producción (usa Neon por entorno).

## Arranque (Windows / PowerShell)

1. Exporta variables de entorno:
   ```powershell
   $env:SPRING_PROFILES_ACTIVE="dev"
   $env:SPRING_DATASOURCE_URL="jdbc:postgresql://<host>/<db>?sslmode=require&channel_binding=require"
   $env:SPRING_DATASOURCE_USERNAME="<usuario>"
   $env:SPRING_DATASOURCE_PASSWORD="<password>"
   $env:JWT_SECRET="<secreto>"
   ```

2. Ejecuta el servidor:
   ```powershell
   ./gradlew.bat bootRun --args="--spring.profiles.active=$env:SPRING_PROFILES_ACTIVE --server.port=8081"
   ```

## Notas de seguridad

- Se deshabilitó la consola H2 y se eliminó la dependencia de H2 para asegurar uso exclusivo de Neon.
- No se deben incluir credenciales en `yml` ni en el código fuente.

## Migraciones (opcional)

El proyecto incluye `flyway-core`. Puedes añadir scripts en `src/main/resources/db/migration` (`V1__init.sql`, etc.) y configurar `spring.flyway.*` por entorno.

