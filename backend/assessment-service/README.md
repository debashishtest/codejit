# CodeJIT Assessment Service (`assessment-service`)

The `assessment-service` manages assessment authoring, question banks, visible and hidden test case definitions, share code lookups, and Redis caching on port `8082`.

## Endpoints

| Method | Path | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/assessments` | List assessment summaries created by user | Authenticated |
| `POST` | `/api/v1/assessments` | Create assessment with questions and test cases | Authenticated |
| `GET` | `/api/v1/assessments/{id}` | Get full assessment details with visible test cases | Authenticated |
| `POST` | `/api/v1/assessments/{id}/start` | Transition assessment status to `STARTED` | Authenticated |
| `GET` | `/api/v1/assessments/join/{code}` | Retrieve assessment info by share code | Public / Open |
| `POST` | `/api/v1/assessments/join/{code}` | Join an active assessment room | Authenticated / Open |

## Architecture & Redis Integration

- **Share Code Caching**: Assessment share codes are cached in Redis (`assessment:share:<CODE>`) with a 6-hour TTL for low-latency joins.
- **Relational Data Model**: Cascade persistence across `Assessment` -> `CodingQuestion` -> `TestCase`.

## Build & Test

```bash
mvn clean test -pl assessment-service
```

