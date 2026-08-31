# CodeJIT Auth Service (`auth-service`)

The `auth-service` manages identity, user registration, credentials authentication, and JWT issuance on port `8081`.

## Endpoints

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/public/register` | Register a new user and generate JWT token | Public |
| `POST` | `/api/v1/public/login` | Authenticate with email/username and password | Public |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile | Authenticated |

## Security Architecture

- Passwords hashed using **BCrypt** (strength 10).
- Stateless JWT tokens signed using HMAC SHA-256 (`io.jsonwebtoken.jjwt`).
- H2 in-memory mode for rapid development; PostgreSQL for production.

## Build & Test

```bash
mvn clean test -pl auth-service
```

