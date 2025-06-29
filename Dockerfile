# Use Eclipse Temurin JDK 17 as the base image
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy all files from local project to the container
COPY . .

# Make Maven wrapper executable (in case it's not)
RUN chmod +x mvnw

# Install dependencies and build the application (skip tests)
RUN ./mvnw clean install -DskipTests

# Expose port (must match `application.properties`: 8081)
EXPOSE 8081

# Run the Spring Boot app using Maven wrapper
CMD ["./mvnw", "spring-boot:run"]
