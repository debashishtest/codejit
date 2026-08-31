# CodeJIT Frontend Client (`frontend`)

Single-page application (SPA) for CodeJIT built with **React 19**, **TypeScript**, **Tailwind CSS**, and **Vite**.

## Architecture & Directory Structure

```
frontend/
├── src/
│   ├── api/             # Typed API client, REST endpoints, DTO models
│   ├── auth/            # AuthContext, JWT session storage, token refresh
│   ├── components/      # Reusable UI primitives (AppShell, ProtectedRoute, Buttons, Badges)
│   ├── notifications/   # Toast notification provider & event bus
│   ├── pages/           # Application views (Dashboard, LiveInterview, SolvePage, etc.)
│   ├── App.tsx          # React Router v7 routes definition
│   ├── index.css        # Global CSS & Tailwind configuration
│   └── main.tsx         # Application root mount
├── tests/               # Native TypeScript test suites
├── public/              # Static assets and icons
├── Dockerfile           # Multi-stage production Nginx container build
├── nginx.conf           # Reverse proxy configuration
└── vite.config.ts       # Vite build configuration with reverse proxy
```

## Features

- **Candidate Coding Room**: Live in-browser editor with instant visible test execution (`/run`) and comprehensive test judging (`/submit`).
- **Live Collaborative Interview Room**: Real-time STOMP WebSocket synchronization for code editor, shared whiteboard notes, and room chat.
- **Assessment Management**: Screen authoring, question banks, visible/hidden test case configuration, and 8-character share codes.
- **Role-Based Auth & Session Handling**: Protected routes, JWT token decoding, and session expiration redirects.

## Development & Testing

```bash
# Install dependencies
npm install

# Start Vite development server (Proxies /api and /ws to port 8080)
npm run dev

# Run unit tests
npm test

# Build production bundle
npm run build
```

