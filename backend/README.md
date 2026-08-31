# CodeJIT Microservices Backend

Enterprise-grade Java Spring Boot 3.3+ microservices architecture powering the CodeJIT technical assessment and live collaborative interview platform.

## System Architecture

```mermaid
graph TD
    Client[Frontend Client / SPA] -->|Port 8080| Gateway[api-gateway]
    
    Gateway -->|/api/v1/public/** & /api/v1/auth/**| Auth[auth-service :8081]
    Gateway -->|/api/v1/assessments/**| Assessment[assessment-service :8082]
    Gateway -->|/api/v1/assessments/*/questions/*/(run|submit)| Exec[execution-service :8083]
    Gateway -->|/api/v1/submissions/**| Exec
    Gateway -->|/api/v1/interviews/** & /ws/**| Interview[interview-service :8084]

    Assessment --- Redis[(Redis Cache :6379)]
    Assessment -->|Publish Task| Kafka[(Kafka Broker :9092)]
    Kafka -->|Consume Task| Exec
    Exec -->|Publish Result| Kafka
    Interview --- RedisPubSub[(Redis Pub/Sub)]
```

## Modules Directory

1. **`common-dto`**: Shared DTOs, Kafka events (`SubmissionTaskEvent`, `SubmissionResultEvent`), security models (`JwtUtils`, `UserPrincipal`), and exception handlers.
2. **`api-gateway` (Port 8080)**: Spring Cloud Gateway managing reverse proxy routing, JWT authentication validation, CORS policies, and rate limits.
3. **`auth-service` (Port 8081)**: User registration, login, BCrypt password hashing, role-based authorization, and JWT issuance.
4. **`assessment-service` (Port 8082)**: Assessment creation, coding questions, test cases, and Redis share-code caching.
5. **`execution-service` (Port 8083)**: Sandboxed code runner (Java 21+, Python, Node), test case evaluation, and asynchronous Kafka submission consumer/producer.
6. **`interview-service` (Port 8084)**: Live collaborative coding room, WebSockets STOMP broker, shared whiteboard/notes, and Redis Pub/Sub cluster synchronization.

## Prerequisites

- Java 21+ (Compatible with OpenJDK 21 - 26)
- Maven 3.9+
- Docker & Docker Compose (Optional for containerized run)
- Redis 7+
- Apache Kafka 3.7+

## Build and Run Tests

```bash
# Set Java 21+ home
export JAVA_HOME=/opt/homebrew/opt/openjdk
export PATH="$JAVA_HOME/bin:$PATH"

# Run tests across all modules
mvn clean test
```

## Local Development Start

Run services independently or via Docker Compose:

```bash
# Spin up infrastructure (PostgreSQL, Redis, Kafka) and services
docker-compose up -d
```

