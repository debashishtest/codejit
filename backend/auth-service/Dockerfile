# Build Stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY common-dto common-dto
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY assessment-service assessment-service
COPY execution-service execution-service
COPY interview-service interview-service

RUN mvn clean package -pl auth-service -am -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

