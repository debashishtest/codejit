# CodeJIT API Gateway (`api-gateway`)

The `api-gateway` serves as the single unified entrypoint for the CodeJIT platform on port `8080`.

## Features

- **Dynamic Reverse Proxy Routing**: Routes incoming requests to backend microservices based on URI prefixes.
- **Stateless JWT Validation**: Custom reactive filter (`JwtAuthenticationGatewayFilter`) verifies bearer tokens and enriches downstream requests with `X-User-Email`, `X-User-Role`, and `X-User-Id`.
- **CORS Support**: Global reactive CORS filter handling preflight OPTIONS and credentials from modern single-page applications.
- **WebSocket Gateway**: Proxies STOMP over WebSocket connections to `interview-service`.

## Route Map

| Path Pattern | Target Service | Default Port | Security |
|---|---|---|---|
| `/api/v1/public/**` | `auth-service` | 8081 | Open (No Auth) |
| `/api/v1/auth/**` | `auth-service` | 8081 | Bearer JWT |
| `/api/v1/assessments/*/questions/*/run` | `execution-service` | 8083 | Bearer JWT |
| `/api/v1/assessments/*/questions/*/submit` | `execution-service` | 8083 | Bearer JWT |
| `/api/v1/submissions/**` | `execution-service` | 8083 | Bearer JWT |
| `/api/v1/assessments/**` | `assessment-service` | 8082 | Bearer JWT |
| `/api/v1/interviews/**` | `interview-service` | 8084 | Bearer JWT |
| `/ws/**` | `interview-service` | 8084 | STOMP Over WS |

## Build & Test

```bash
mvn clean test -pl api-gateway
```

