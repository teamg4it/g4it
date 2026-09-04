# Use an official Maven image as the base image
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn --no-transfer-progress -P SKIP-ALL-TEST clean package

FROM eclipse-temurin:21-jre
ARG JAR_FILE=/app/target/*.jar
WORKDIR /app
COPY --from=build $JAR_FILE application.jar
COPY storage/--INTERNAL-G4IT-- storage/--INTERNAL-G4IT--
RUN mkdir /app/storagetmp && \
    chown -R 1001:1001 /app/*

USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx1g", "-Xms1g", "-jar", "application.jar"]
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:8080/actuator/health | grep UP || exit 1
 