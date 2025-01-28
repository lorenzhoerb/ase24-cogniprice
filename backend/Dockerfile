# Stage 1: Build the Spring Boot app using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven's configuration and dependencies to cache the layer
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application, skipping checkstyle
COPY src ./src
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true  # Skip checkstyle

# Stage 2: Create a minimal runtime container
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/cogniprice-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the Spring Boot app runs on
EXPOSE 8080

# Run the Spring Boot application with the 'prod' profile
CMD ["java", "-jar", "app.jar"]
