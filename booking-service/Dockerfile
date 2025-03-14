FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

# Copy only Gradle-related files first to leverage Docker caching
COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle /app/build.gradle
COPY gradle.properties /app/gradle.properties

# Download dependencies without building the whole project
RUN ./gradlew dependencies --no-daemon || true

# Now copy the actual source code
COPY src/main /app/src/main

# Build the jar using Gradle
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

ARG JAR_NAME="booking-service-1.0-all"
# Copy the jar file from the builder stage
COPY --from=builder /app/build/libs/${JAR_NAME}.jar /app/${JAR_NAME}.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "booking-service-1.0-all.jar"]
