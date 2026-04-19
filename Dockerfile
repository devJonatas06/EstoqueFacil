FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instala curl para health check
RUN apk add --no-cache curl

# Copiar JAR
COPY target/*.jar app.jar

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]