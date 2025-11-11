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

## Despliegue en Railway

Railway soporta múltiples servicios en el mismo proyecto (backend y frontend separados).

### Opción A: Auto-detección (Nixpacks)
- Si tu repo tiene este proyecto en la raíz, Railway detectará Gradle/Maven automáticamente.
- En este repo el proyecto vive en `proyectoAvanzada/`, por lo que es más fiable definir comandos:
  - Build Command: `cd proyectoAvanzada && ./gradlew clean bootJar -x test`
  - Start Command: `java -jar $(ls proyectoAvanzada/build/libs/*.jar | head -n 1)`

### Opción B: Dockerfile (recomendada)
- Se incluye `Dockerfile` multi-stage en la raíz del proyecto (`proyectoAvanzada/Dockerfile`).
- Railway detecta el Dockerfile y construye la imagen:
  - No necesitas definir Build/Start commands en el panel.

### Variables de entorno (Service: Backend)
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_DDL_AUTO=validate` (o `update` si lo necesitas)
- `SPRING_FLYWAY_ENABLED=false` si tu Postgres no es compatible
- `JWT_SECRET=<secreto>`
- `CORS_ALLOWED_ORIGINS=https://<frontend-service>.up.railway.app,http://localhost:3000,http://localhost:5173`

### Puerto
- Railway inyecta `PORT`. En producción el backend usa `server.port: ${PORT:${SERVER_PORT:8080}}`.

### Frontend (otro repositorio/servicio)
- Añade el frontend como servicio separado en el mismo proyecto de Railway.
- Apunta al backend con:
  - Vite: `VITE_API_BASE_URL=https://<backend-service>.up.railway.app`
  - CRA: `REACT_APP_API_BASE_URL=https://<backend-service>.up.railway.app`

Más info: https://docs.railway.com/quick-start
