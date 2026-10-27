# ===============================================
# Attendex Backend - Production Cloud Dockerfile
# ===============================================

# Stage 1: Build the Spring Boot application using Maven 3.9 & Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# Copy Maven descriptor and source files
COPY pom.xml .
COPY src ./src

# Package application JAR without running tests
RUN mvn clean package -DskipTests

# Stage 2: Secure, minimal JRE 21 runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Ensure data directory exists with write permissions for embedded storage
RUN mkdir -p /app/data && chmod -R 777 /app/data

# Copy built JAR artifact from builder stage
COPY --from=build /workspace/app/target/*.jar /app/app.jar

# Render, Railway, AWS inject dynamic PORT
ENV PORT=8080
EXPOSE 8080

# Shell execution ensures dynamic ${PORT} environment variable is evaluated correctly
CMD ["sh", "-c", "java -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
