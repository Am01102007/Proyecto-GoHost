# syntax=docker/dockerfile:1

# ==============================
# Build stage
# ==============================
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# ==============================
# Runtime stage
# ==============================
FROM eclipse-temurin:17-jre AS runner
WORKDIR /app

# Perfil por defecto en producción
ENV SPRING_PROFILES_ACTIVE=prod

# Copia el JAR construido
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Ejecuta la aplicación
ENTRYPOINT ["java","-jar","/app/app.jar"]

