# CodeJIT — Cloud-Native Technical Assessment & Live Interview Platform

[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7.0-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![React 19](https://img.shields.io/badge/React-19.2-61dafb.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-4.x-38bdf8.svg)](https://tailwindcss.com/)

CodeJIT is an enterprise-grade, microservices-powered platform designed for conducting technical screening assessments and real-time collaborative coding interviews.

---

## 🏛️ System Architecture

```mermaid
flowchart TB
    subgraph Client ["Client Layer"]
        UI["CodeJIT Frontend SPA (Vite / React 19)\nhttp://localhost:5173"]
    end

    subgraph Edge ["Edge & API Gateway (:8080)"]
        GW["Spring Cloud API Gateway\n- Stateless JWT Validation\n- Reactive CORS Filter\n- Dynamic Service Reverse Proxy"]
    end

    subgraph CoreServices ["Microservices Layer"]
        AUTH["auth-service (:8081)\n• User Reg & Login\n• BCrypt Hashing\n• JWT Token Issuance"]
        ASSESS["assessment-service (:8082)\n• Assessment Authoring\n• Question Bank & Test Cases\n• Share Code Lookup Cache"]
        EXEC["execution-service (:8083)\n• Sandboxed Process Engine\n• Java & Python Runners\n• Kafka Consumer / Judge"]
        INTV["interview-service (:8084)\n• Real-Time STOMP Broker\n• Editor & Board Live Sync\n• In-Room Chat Stream"]
    end

    subgraph Middleware ["Middleware & Persistence Layer"]
        REDIS[("Redis 7.2\n• Assessment Share Cache\n• WS Pub/Sub Cluster Backplane")]
        KAFKA[("Apache Kafka 3.7 (KRaft)\n• codejit.submissions\n• codejit.submission-results")]
        POSTGRES[("PostgreSQL 16 / H2\n• Microservice Datastores")]
    end

    UI -->|HTTP REST / WebSocket STOMP| GW

    GW -->|/api/v1/public/**, /api/v1/auth/**| AUTH
    GW -->|/api/v1/assessments/**| ASSESS
    GW -->|/api/v1/assessments/*/questions/*/(run|submit)| EXEC
    GW -->|/api/v1/submissions/**| EXEC
    GW -->|/api/v1/interviews/**| INTV
    GW -->|/ws/** (WS STOMP Proxy)| INTV

    AUTH --- POSTGRES
    ASSESS --- POSTGRES
    ASSESS --- REDIS
    EXEC --- POSTGRES
    EXEC --- KAFKA
    INTV --- POSTGRES
    INTV --- REDIS
```

---

## 📦 Microservices Breakdown

| Service | Port | Description | Database / Cache / Messaging |
|---|---|---|---|
| **`api-gateway`** | `8080` | Unified reverse proxy, reactive CORS, JWT authentication filter | Dynamic Routing |
| **`auth-service`** | `8081` | User registration, login, BCrypt password hashing, JWT claims issuance | PostgreSQL / H2 (`codejit_auth`) |
| **`assessment-service`** | `8082` | Assessment lifecycle, question sets, visible/hidden test cases | PostgreSQL / H2 + Redis Cache |
| **`execution-service`** | `8083` | Sandboxed multi-language code runner, timeouts, Kafka judging worker | PostgreSQL / H2 + Apache Kafka |
| **`interview-service`** | `8084` | Live collaborative interview rooms, STOMP WebSockets, chat, notes | PostgreSQL / H2 + Redis Pub/Sub |
| **`common-dto`** | — | Shared DTOs, Kafka event contracts, JWT utilities, exception models | — |
| **`frontend`** | `5173` / `3000` | Single-page application built with React 19, Tailwind CSS, Lucide icons | Nginx / Vite Dev Server |

---

## 🚀 Quickstart Guide

### Option 1: Full Docker Compose (Recommended)

To start the entire platform including PostgreSQL, Redis, Kafka, all 5 microservices, and Nginx frontend:

```bash
# 1. Build and start all services in detached mode
docker-compose up --build -d

# 2. View container status
docker-compose ps

# 3. Access CodeJIT in your browser:
# Frontend UI:   http://localhost:3000
# API Gateway:   http://localhost:8080
```

### Option 2: Local Development Setup

#### Prerequisites
- OpenJDK 21 or higher
- Maven 3.9+
- Node.js 20+

```bash
# 1. Set Java Environment
export JAVA_HOME=/opt/homebrew/opt/openjdk
export PATH="$JAVA_HOME/bin:$PATH"

# 2. Compile and Test Backend Modules
cd backend
mvn clean test

# 3. Start Frontend Dev Server
cd ../frontend
npm install
npm run dev
```

---

## 🧪 Testing Guide

### Backend Unit & Integration Tests

The backend test suite verifies service logic, repository interactions, MockMvc API controllers, and security filters using **JUnit 5**, **Mockito**, and **AssertJ**:

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk
export PATH="$JAVA_HOME/bin:$PATH"

# Run all microservices tests
mvn test

# Run a specific module test suite
mvn test -pl auth-service
mvn test -pl assessment-service
mvn test -pl execution-service
mvn test -pl interview-service
mvn test -pl api-gateway
mvn test -pl common-dto
```

### Frontend Tests

The frontend test suite uses Node's native test runner with zero extra runtime overhead:

```bash
cd frontend

# Run frontend tests
npm test

# Build production bundle
npm run build
```

---

## 📡 API Endpoint Reference

### 🔐 Auth Service (`/api/v1/public`, `/api/v1/auth`)
- `POST /api/v1/public/register`: Register new account with name, email, password, and role (`ROLE_INTERVIEWER` or `ROLE_CANDIDATE`).
- `POST /api/v1/public/login`: Authenticate credentials and receive Bearer JWT.
- `GET /api/v1/auth/me`: Get current authenticated user profile.

### 📋 Assessment Service (`/api/v1/assessments`)
- `GET /api/v1/assessments`: List assessments authored by authenticated user.
- `POST /api/v1/assessments`: Create assessment with questions and visible/hidden test cases.
- `GET /api/v1/assessments/{id}`: Get assessment details with visible test cases.
- `POST /api/v1/assessments/{id}/start`: Start assessment session.
- `GET /api/v1/assessments/join/{code}`: Look up assessment details by 8-character share code.
- `POST /api/v1/assessments/join/{code}`: Join active assessment room.

### ⚡ Execution & Judge Service (`/api/v1/assessments/*/questions/*`, `/api/v1/submissions`)
- `POST /api/v1/assessments/{assessmentId}/questions/{questionId}/run`: Run code against visible test cases with instant runtime metrics.
- `POST /api/v1/assessments/{assessmentId}/questions/{questionId}/submit`: Submit solution for judging against all test cases.
- `GET /api/v1/submissions/{id}`: Retrieve verdict and test case execution details.

### 🎙️ Live Interview Service (`/api/v1/interviews`, `/ws/interviews`)
- `GET /api/v1/interviews`: List interview rooms created by host.
- `POST /api/v1/interviews`: Schedule a new interview room.
- `GET /api/v1/interviews/{id}`: Get room details and participant status.
- `POST /api/v1/interviews/join/{code}`: Join live interview with share code.
- `POST /api/v1/interviews/{id}/start`: Mark interview room as `LIVE`.
- `POST /api/v1/interviews/{id}/end`: End interview session.
- `WS /ws/interviews`: Real-time STOMP WebSocket connection:
  - Subscribe: `/topic/interviews/{id}`
  - Send Events: `/app/interviews/{id}/event`

---

## 📁 Repository Structure

```
codejit/
├── backend/
│   ├── pom.xml                 # Root Maven Parent POM
│   ├── README.md               # Backend Architecture Documentation
│   ├── common-dto/             # Shared DTOs, Kafka Events, JWT Utils
│   ├── api-gateway/            # Spring Cloud Gateway (Port 8080)
│   ├── auth-service/           # User Identity & JWT Service (Port 8081)
│   ├── assessment-service/     # Assessment Engine & Redis Cache (Port 8082)
│   ├── execution-service/      # Sandbox Code Runner & Kafka Worker (Port 8083)
│   └── interview-service/      # Real-Time WebSocket Interview Rooms (Port 8084)
├── frontend/
│   ├── src/                    # React 19 Frontend Application
│   │   ├── api/                # Typed REST API Client & Contracts
│   │   ├── auth/               # AuthContext & Session Management
│   │   ├── components/         # Reusable UI Primitives
│   │   ├── notifications/      # Toast Notification System
│   │   ├── pages/              # Views (Dashboard, SolvePage, LiveInterview)
│   │   └── App.tsx             # Application Routes
│   ├── tests/                  # Frontend Unit Test Suites
│   ├── Dockerfile              # Production Multi-Stage Nginx Container
│   ├── nginx.conf              # Reverse Proxy Config
│   ├── package.json            # Frontend Dependencies & Scripts
│   └── README.md               # Frontend Developer Documentation
├── docker-compose.yml          # Full Multi-Service Docker Orchestration
└── README.md                   # Main Project Developer & Architecture Guide
```

---

## 🔒 Security & Best Practices

- **Stateless Bearer JWT Authentication**: Central token validation at API Gateway with HMAC SHA-256 signature verification.
- **Sandboxed Execution**: Subprocess isolation with strict time limit (3000ms) and memory ceiling (`-Xmx128m`).
- **Decoupled Asynchronous Judging**: Kafka message queues prevent judge worker backpressure from degrading API Gateway throughput.
- **Distributed WebSocket Sync**: Redis Pub/Sub backplane ensures live editor and whiteboard changes reach all room participants across multiple server instances.

