# Stage 1: Bygg JAR
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiera pom.xml separat för att cacha dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Bygg applikationen
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Produktionsimage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Skapa icke-root användare för säkerhet
RUN addgroup -S nextstep && adduser -S nextstep -G nextstep
USER nextstep

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM-flaggor optimerade för container-miljö
ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
