FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

# Copy the Gradle build files and source code
COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle /app/build.gradle
COPY gradle.properties /app/gradle.properties
COPY src/main /app/src/main

# Build the jar using Gradle
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

ARG JAR_NAME="search-service-1.0-all"
# Copy the jar file from the builder stage
COPY --from=builder /app/build/libs/${JAR_NAME}.jar /app/${JAR_NAME}.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the app
CMD ["java", "-jar", "search-service-1.0-all.jar"]
