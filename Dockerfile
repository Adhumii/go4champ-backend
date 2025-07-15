FROM eclipse-temurin:21-jdk as build

WORKDIR /app

# Set locale and encoding
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copy Maven wrapper and pom first for better caching
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn/
COPY pom.xml ./

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies for better caching
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build with explicit encoding
RUN ./mvnw package -DskipTests -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]