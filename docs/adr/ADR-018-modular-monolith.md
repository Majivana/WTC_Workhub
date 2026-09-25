# ADR-018: Modular monolith and JSON API

- **Status:** Accepted for the MVP
- **Date:** 2026-09-25
- **Decision owners:** Project owner
- **Related issues:** None recorded

## Context

The MVP is a solo-developed Spring Boot client-server application with a short delivery window.
It needs clear boundaries without requiring a distributed deployment and its operational cost.

## Decision

Keep a single Spring Boot modular monolith with JSON REST controllers, application services,
repositories, and storage adapters. The current delivery is an API; a browser frontend is not
included.

### Implementation follow-up

The original decision described the API-only implementation at the time of writing. A separate
React/TypeScript browser client was later added under `frontend/`; the modular monolith remains its
API and authorization boundary. The client is currently served by Vite in development and is not
packaged into the backend Docker image.

## Alternatives considered

### Microservices

Deferred because independent scaling and deployment boundaries have not been demonstrated and
would add networking and operational complexity.

### Server-rendered frontend

Deferred; client presentation is outside the implemented API scope.

## Consequences

### Positive consequences

- One local process is straightforward to build, run, and test.
- Service and adapter boundaries leave room for later deployment changes.

### Negative consequences and trade-offs

- Modules share one release and runtime failure boundary.
- No user-facing browser UI is delivered.

## Security and privacy impact

Authorization remains enforced at the server boundary. Sensitive information must not be exposed
through API responses, logs, or storage URLs.

## Operational impact

The local run and container package one application. AWS ECS remains a target design, not an
operational environment.

## Validation

The Spring Boot application, REST integration tests, and local Docker build are the validation
path. See the release evidence page for the latest run.
