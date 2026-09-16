# Palette Platform

Palette Platform is a Color Hunt-inspired learning and portfolio product. It will provide a production-style backend, a Vue web client, and Android/iOS clients that share Kotlin Multiplatform code.

The repository is a monorepo, but every application remains independently buildable and deployable.

## Current milestone

Milestone 1 establishes the backend foundation:

- Kotlin 2.4.20 and Spring Boot 3.5.16
- Java 17 baseline
- PostgreSQL and Flyway migrations
- Docker Compose development environment
- Actuator health checks
- Automated backend tests and GitHub Actions
- Architecture, product, and test documentation

Web and mobile implementation will begin after the first backend vertical slice is complete.

## Repository layout

```text
palette-platform/
├── backend/                 Kotlin + Spring Boot API
├── web/                     Vue.js + TypeScript client (planned)
├── mobile/                  KMP, Android Compose, SwiftUI (planned)
├── infrastructure/         Docker and deployment configuration
├── docs/                    Product and engineering documentation
└── .github/workflows/       Path-scoped CI pipelines
```

## Run locally

Requirements: Java 17+ and Docker.

```bash
docker compose -f infrastructure/docker/compose.yml up -d postgres
cd backend
./gradlew bootRun
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

Run backend tests:

```bash
cd backend
./gradlew test
```

Run the complete backend stack in containers:

```bash
docker compose -f infrastructure/docker/compose.yml --profile full up --build
```

## Documentation

- [Product specification](docs/PROJECT_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Test strategy](docs/TEST_STRATEGY.md)
- [Roadmap](docs/ROADMAP.md)
