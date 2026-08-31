# CodeJIT Execution Service (`execution-service`)

The `execution-service` runs untrusted candidate code within an isolated execution sandbox, verifies test cases, and streams judging events via Kafka on port `8083`.

## Endpoints

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/assessments/{assessmentId}/questions/{questionId}/run` | Run code against visible test cases for quick feedback | Authenticated |
| `POST` | `/api/v1/assessments/{assessmentId}/questions/{questionId}/submit` | Submit code for full test suite judging | Authenticated |
| `GET` | `/api/v1/submissions/{id}` | Get submission status and test case breakdown | Authenticated |

## Architecture & Kafka Pipeline

1. **Sandboxed Worker (`SandboxProcessExecutor`)**:
   - Compiles and executes code in isolated temporary directories.
   - Enforces timeout limits (default 3000ms), memory constraints (`-Xmx128m`), and process termination.
2. **Kafka Event Driven Flow**:
   - `codejit.submissions`: Topic receiving submission tasks.
   - `codejit.submission-results`: Topic publishing finalized judging results.

## Build & Test

```bash
mvn clean test -pl execution-service
```

