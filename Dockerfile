# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper files to the container
COPY gradlew /app/
COPY gradle /app/gradle

# Copy the build configuration files to the container
COPY build.gradle /app/
COPY settings.gradle /app/

# Copy the source code to the container
COPY src /app/src

# Build the project
RUN ./gradlew clean shadowJar

# Copy the application JAR file into the container at /app
COPY build/libs/wlhiv-ucs-hiv-enrollment-service-1.0.0.jar /app/wlhiv-ucs-hiv-enrollment-service-1.0.0.jar

# Copy the mediator.properties file to the container at /app
COPY src/main/resources/mediator.properties /app/mediator.properties

# Expose environment variables for configuration
ENV MEDIATOR_CONFIG_FILE=/app/mediator.properties

# Specify the default command to run on boot
CMD ["java", "-jar", "-Dmediator.config.file=${MEDIATOR_CONFIG_FILE}", "wlhiv-ucs-hiv-enrollment-service-1.0.0.jar"]
