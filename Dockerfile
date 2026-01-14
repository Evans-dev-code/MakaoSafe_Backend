# -------------------------------
# Stage 1: Build
# -------------------------------
FROM gradle:8.5-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY build.gradle settings.gradle ./
COPY gradle gradle/
COPY src src/

# Build the application (skipping tests for speed)
RUN gradle clean build -x test --no-daemon

# -------------------------------
# Stage 2: Runtime
# -------------------------------
# Use Eclipse Temurin (Reliable & Supported)
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run with memory limits (Crucial for Render Free Tier to prevent crashing)
ENTRYPOINT ["java", "-Xmx350m", "-Xms128m", "-jar", "app.jar"]