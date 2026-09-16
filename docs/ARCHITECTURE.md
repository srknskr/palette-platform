# Architecture

## System context

Palette Platform is a monorepo containing independently deployable clients and a modular-monolith backend.

```mermaid
flowchart TD
    Mobile["Mobile: KMP + native UI"] --> API["Spring Boot REST API"]
    Web["Vue web client"] --> API
    API --> DB[(PostgreSQL)]
    API --> Obs["Logs and monitoring"]
```

## Backend layering

Each feature package follows a vertical-slice structure while preserving clear responsibilities:

```text
feature/
├── api/             controllers and HTTP DTOs
├── application/     use cases and transaction boundaries
├── domain/          business rules and domain types
└── infrastructure/  JPA entities and repository adapters
```

Dependency direction is inward: HTTP and persistence depend on application/domain contracts. Domain code does not depend on Spring MVC.

## Implemented modules

- `identity`: users, credentials, JWT access tokens, opaque refresh-token rotation, SHA-256 database hashing
- `palette`: palettes, 4 ordered colors, tags, draft/published/rejected/archived states, search and discovery
- `favorite`: idempotent favorite and unfavorite operations, personal collections, atomic count management
- `moderation`: administrative review queue (pending palettes), publish, reject, archive actions, audit trail
- `shared`: RFC 9457 problem details, rate limiting, OpenAPI/Swagger configuration, pagination conventions

## Important decisions

### Monorepo

One repository reduces coordination overhead and permits an API change to update clients and contract tests atomically. CI remains path-scoped so unrelated applications are not rebuilt unnecessarily.

### Modular monolith

The domain is too small to justify distributed transactions, network boundaries, or duplicated deployment infrastructure. Feature boundaries are retained so modules can be extracted later if evidence requires it.

### PostgreSQL as the primary database

PostgreSQL is the production database. Flyway manages all migrations (`V1__baseline_schema.sql`, `V2__moderation_audit.sql`). Production uses `ddl-auto: validate`. Testcontainers PostgreSQL is used for integration testing.

### Native mobile UI with KMP sharing

Networking, serialization, domain models, and repositories can be shared. Android uses Compose and iOS uses SwiftUI so the project demonstrates both KMP and platform-native development.

## API conventions

- Base path: `/api/v1`
- JSON uses camelCase
- UUIDs are opaque strings to clients
- Times use ISO-8601 UTC
- Collection endpoints are paginated (`PagedResponse`)
- Idempotency is enforced for favorite, unfavorite, and logout operations
- Errors use RFC 9457 `ProblemDetail` responses
