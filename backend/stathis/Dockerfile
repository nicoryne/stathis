# Use an official Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml

# Download dependencies (leverages Docker cache)
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY . .

# Build the application (skip tests)
RUN ./mvnw clean install -DskipTests

# ----
# Use a lightweight JRE image to run the app
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your app runs on (default Spring Boot port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]