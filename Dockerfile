# ===============================================
# Attendex Backend - Multi-Stage Production Build
# ===============================================

# Stage 1: Build the Spring Boot application using Maven and Temurin JDK 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src src

# Install maven and package application (skip unit tests for fast reproducible builds)
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Stage 2: Minimal, secure JRE runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as a non-privileged user for enhanced cloud security
RUN groupadd -r attendex && useradd -r -g attendex attendex
USER attendex:attendex

# Copy compiled JAR artifact from builder
COPY --from=build /workspace/app/target/*.jar app.jar

# Dynamic PORT support for Render, Railway, AWS, and Heroku
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.awt.headless=true", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=${PORT}", "-jar", "app.jar"]
