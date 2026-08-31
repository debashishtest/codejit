# CodeJIT Interview Service (`interview-service`)

The `interview-service` orchestrates live collaborative interview rooms, real-time shared code editor synchronization, collaborative notes/whiteboard, and WebSockets STOMP messaging on port `8084`.

## Endpoints

### REST API

| Method | Path | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/interviews` | List interview rooms created by host | Authenticated |
| `POST` | `/api/v1/interviews` | Schedule a new live interview room | Authenticated |
| `GET` | `/api/v1/interviews/{id}` | Get interview room snapshot & participants | Authenticated |
| `POST` | `/api/v1/interviews/{id}/start` | Start interview session | Authenticated |
| `POST` | `/api/v1/interviews/{id}/end` | End interview session | Authenticated |
| `GET` | `/api/v1/interviews/join/{code}` | Fetch interview room details by share code | Open / Authenticated |
| `POST` | `/api/v1/interviews/join/{code}` | Join live interview room | Open / Authenticated |

### Real-Time WebSockets (STOMP)

- **Handshake Endpoint**: `/ws/interviews`
- **Client Subscription Topic**: `/topic/interviews/{id}`
- **Client Outgoing Destination**: `/app/interviews/{id}/event`
- **Supported Event Types**:
  - `BOARD_UPDATED`: Collaborative whiteboard / notes markdown sync
  - `EDITOR_UPDATED`: Live multi-cursor code editor synchronization
  - `CHAT_MESSAGE`: Real-time room participant chat

### Redis Pub/Sub Scaling

For horizontal scalability across multiple `interview-service` instances, outgoing events are published to Redis channel `codejit-interview-channel` and re-broadcasted to local WebSocket sessions.

## Build & Test

```bash
mvn clean test -pl interview-service
```

