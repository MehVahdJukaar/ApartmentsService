# Use Gradle to build the project
FROM gradle:7.5-jdk17 as builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle build files
COPY apartment-service/gradle /app/gradle
COPY apartment-service/build.gradle /app/build.gradle
COPY apartment-service/settings.gradle /app/settings.gradle

# Copy the source code
COPY apartment-service/src /app/src

# Build the jar using Gradle
RUN gradle build --no-daemon

# Use a smaller image for running the app
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/build/libs/apartment-service.jar /app/apartment-service.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the app
CMD ["java", "-jar", "apartment-service.jar"]
