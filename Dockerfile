# --- Stage 1: build ---
FROM gradle:8.4-jdk21-alpine AS builder

WORKDIR /home/gradle/project

# Copiamos TODO el proyecto de una
COPY . .

# Build real, sin ocultar errores
RUN gradle --no-daemon clean bootJar unzipNewRelic -x test


# --- Stage 2: runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# App
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Copiamos TODO el build y buscamos New Relic dinámicamente
COPY --from=builder /home/gradle/project/build/ /tmp/build/

RUN find /tmp/build -name "newrelic.jar" -exec cp {} /app/newrelic.jar \; \
 && find /tmp/build -name "newrelic.yml" -exec cp {} /app/newrelic.yml \;

ENV JAVA_OPTS=""
EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/newrelic.jar $JAVA_OPTS -jar app.jar"]