# CodeJIT Common DTO Module (`common-dto`)

The `common-dto` module contains shared models, data transfer objects, security utilities, Kafka event schemas, and common exception types used across all CodeJIT microservices.

## Key Components

- **DTOs (`com.codejit.common.dto`)**:
  - `auth`: `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserDto`
  - `assessment`: `AssessmentRequest`, `AssessmentResponse`, `AssessmentSummaryDto`, `CodingQuestionDto`, `TestCaseDto`, `AssessmentStatus`
  - `execution`: `CodeRequest`, `RunResponse`, `SubmissionResponse`, `TestResult`, `SubmissionStatus`
  - `interview`: `InterviewRequest`, `InterviewResponse`, `InterviewParticipantDto`, `LiveInterviewEvent`, `InterviewStatus`
- **Kafka Events (`com.codejit.common.event`)**:
  - `SubmissionTaskEvent`: Event published when a candidate submits code for asynchronous test evaluation.
  - `SubmissionResultEvent`: Event published after the execution engine evaluates all test cases.
- **Security (`com.codejit.common.security`)**:
  - `JwtUtils`: Centralized token creation, parsing, verification, and claims extraction.
  - `UserPrincipal`: Authenticated principal representation.
- **Exceptions (`com.codejit.common.exception`)**:
  - `ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`, `GlobalErrorResponse`

## Build & Test

```bash
mvn clean test -pl common-dto
```

