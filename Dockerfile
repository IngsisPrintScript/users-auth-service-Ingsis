# --- Stage 1: build the application ---
FROM gradle:8.4-jdk21-alpine AS builder

WORKDIR /home/gradle/project

# Cacheo dependencias
COPY build.gradle settings.gradle gradlew gradle /home/gradle/project/
RUN chmod +x gradlew && ./gradlew --no-daemon assemble -x test || true

# Código
COPY . /home/gradle/project

# Build + New Relic
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar unzipNewRelic -x test


# --- Stage 2: runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# App
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# New Relic (paths exactos)
RUN mkdir -p /app/newrelic
COPY --from=builder /home/gradle/project/build/newrelic/newrelic/newrelic.jar /app/newrelic/newrelic.jar
COPY --from=builder /home/gradle/project/build/newrelic/newrelic/newrelic.yml /app/newrelic/newrelic.yml

ENV JAVA_OPTS=""
ENV NEW_RELIC_LOG=stdout

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/newrelic/newrelic.jar $JAVA_OPTS -jar app.jar"]