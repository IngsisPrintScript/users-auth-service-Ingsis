# --- Stage 1: build the application ---
FROM gradle:8.4-jdk21-alpine AS builder

WORKDIR /home/gradle/project

COPY build.gradle settings.gradle gradlew gradle /home/gradle/project/

RUN gradle --no-daemon build -x test || return 0

COPY . /home/gradle/project

RUN gradle --no-daemon clean bootJar unzipNewRelic

# --- Stage 2: run the application ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
COPY --from=builder /home/gradle/project/build/newrelic/newrelic.jar /app/newrelic.jar
COPY --from=builder /home/gradle/project/build/newrelic/newrelic.yml /app/newrelic.yml

ENV JAVA_OPTS=""

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/newrelic.jar $JAVA_OPTS -jar app.jar"]
