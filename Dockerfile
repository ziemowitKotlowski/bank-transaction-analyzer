# Multi-stage Dockerfile for bank-transaction-analyzer
# Builder: use Eclipse Temurin JDK 21 to build the Spring Boot fat jar
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy only what is needed to build to leverage Docker cache
COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle build.gradle gradle.properties ./
COPY src ./src

# Ensure gradlew is executable
RUN chmod +x ./gradlew || true

# Build the executable jar (skip tests for faster image builds => CI should run tests separately)
RUN ./gradlew bootJar --no-daemon -x test

# Runtime image: small JRE 21
FROM eclipse-temurin:21-jre

# Create non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser || true
WORKDIR /app

# Copy jar from builder stage
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /workspace/${JAR_FILE} app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Use non-root user for running the application
USER appuser

ENTRYPOINT ["java","-jar","/app/app.jar"]
