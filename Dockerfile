# Use an official OpenJDK 17 runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the application's jar file to the container
COPY build/libs/*-SNAPSHOT.jar /app/application.jar

# Run the application
ENTRYPOINT ["java", "-jar", "application.jar"]
