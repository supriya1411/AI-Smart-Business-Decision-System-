# Stage 1: Build the application using Maven and Java 17
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render assigns the port dynamically via the PORT environment variable
ENV PORT=8080
EXPOSE $PORT

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
