# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src
COPY .env ./

RUN ./mvnw dependency:go-offline
RUN ./mvnw clean package -DskipTests

# Stage 2: Test
FROM eclipse-temurin:21-jdk-jammy AS tester
WORKDIR /app

RUN apt-get update && \
    apt-get install -y netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app .

# Stage 3: Production
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/.env .
COPY --from=builder /app/src/main/resources /app/resources

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]