# Palette Platform

Palette Platform is a color-palette discovery and collection platform backend. It provides a production-grade REST API built with Kotlin and Spring Boot, designed to serve web and mobile clients independently.

## Architecture & Features

The backend is built as a modular monolith:
- `identity`: User registration, authentication, JWT access tokens, opaque refresh tokens with rotation and SHA-256 database hashing, logout, and current user retrieval.
- `palette`: Palette creation, editing, deletion, detail lookup, random palette discovery, and feed listing (newest, popular, tag filtering, name search, hex color filtering, pagination).
- `favorite`: Idempotent palette favoriting/unfavoriting, personal collections, atomic count updates preventing negative likes.
- `moderation`: Admin review queue (pending palettes), publish, reject, archive operations with an audit trail (`moderation_logs`).
- `shared`: RFC 9457 ProblemDetail error handling, rate limiting filter, OpenAPI / Swagger documentation, Actuator health and metrics probes.

## Technology Stack

- Language: Kotlin 2.0.21 on Java 17+
- Framework: Spring Boot 3.4.3
- Data & Persistence: Spring Data JPA, Hibernate, PostgreSQL, Flyway
- Security: Spring Security, JJWT, BCrypt
- Documentation: SpringDoc OpenAPI 3 / Swagger UI
- Testing: JUnit 5, MockK, MockMvc, Testcontainers PostgreSQL
- Quality: Detekt static analysis
- Containers: Docker, Docker Compose

## Repository Layout

```text
palette-platform/
├── backend/                 Kotlin + Spring Boot REST API
├── web/                     Vue.js 3 + TypeScript Client (Vite, Pinia)
├── mobile/                  KMP, Android Compose, SwiftUI
├── infrastructure/          Docker and deployment configuration
├── docs/                    Product and engineering documentation
└── .github/workflows/       Path-scoped CI and Release pipelines
```

## Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8080` | Server HTTP listening port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/palette` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | `palette` | Database username |
| `DB_PASSWORD` | `palette` | Database password |
| `JWT_SECRET` | Development default string (32+ bytes) | Secret key for signing HMAC-SHA256 JWT tokens |
| `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES` | `15` | Expiration lifetime for access tokens in minutes |
| `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` | `7` | Expiration lifetime for refresh tokens in days |

## Run Locally

Requirements: Java 17+ (JDK 17) and Docker (or local PostgreSQL).

### Run with Local PostgreSQL

1. Start PostgreSQL:
```bash
docker compose -f infrastructure/docker/compose.yml up -d postgres
```

2. Start Spring Boot backend:
```bash
cd backend
./gradlew bootRun
```

3. Access Health and Swagger UI:
- Health probe: `curl http://localhost:8080/actuator/health`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Run Complete Stack with Docker Compose

```bash
docker compose -f infrastructure/docker/compose.yml --profile full up --build
```

## Testing & Quality

Run unit and web tests:
```bash
cd backend
./gradlew test
```

Run static analysis with Detekt:
```bash
cd backend
./gradlew detekt
```

Build executable JAR:
```bash
cd backend
./gradlew bootJar
```

Run Testcontainers PostgreSQL integration tests:
```bash
cd backend
TESTCONTAINERS_ENABLED=true ./gradlew test
```

## API Overview & Curl Examples

Base path: `/api/v1`

### 1. Register User

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "designer@example.com",
    "password": "Password123!",
    "displayName": "ColorCrafter"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "xP93nK...",
  "tokenType": "Bearer",
  "user": {
    "id": "c3f8e6c4-...",
    "email": "designer@example.com",
    "displayName": "ColorCrafter",
    "role": "USER",
    "createdAt": "2026-09-16T19:00:00Z"
  }
}
```

### 2. Log In

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "designer@example.com",
    "password": "Password123!"
  }'
```

### 3. Refresh Access Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "xP93nK..."
  }'
```

### 4. Create Palette

Palettes require exactly 4 valid, uppercase, non-duplicate HEX colors:

```bash
curl -X POST http://localhost:8080/api/v1/palettes \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nordic Autumn",
    "colors": ["#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"],
    "tags": ["nordic", "cool", "minimal"],
    "publish": true
  }'
```

### 5. List Palettes

Publicly query newest or popular palettes with optional search query, tag, or exact HEX color:

```bash
curl "http://localhost:8080/api/v1/palettes?sort=popular&tag=minimal&page=0&size=20"
```

### 6. Favorite and Unfavorite Palette

```bash
# Favorite
curl -X POST http://localhost:8080/api/v1/palettes/<PALETTE_ID>/favorite \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Unfavorite
curl -X DELETE http://localhost:8080/api/v1/palettes/<PALETTE_ID>/favorite \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### 7. Moderation (Admin Only)

```bash
# List pending palettes
curl http://localhost:8080/api/v1/admin/palettes/pending \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Publish pending palette
curl -X POST http://localhost:8080/api/v1/admin/palettes/<PALETTE_ID>/publish \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Meets quality standards"}'
```

## Troubleshooting

- **Port in use**: Change `PORT` in environment variables or command line (`-DPORT=8081`).
- **Database connection error**: Verify Docker is running and PostgreSQL is ready (`pg_isready -U palette -d palette`).
- **Authentication failure**: Ensure the Authorization header starts with `Bearer `.
- **Invalid palette submission**: Ensure all four colors are distinct 6-character hex strings with `#` prefix.

## Documentation

- [Product specification](docs/PROJECT_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Test strategy](docs/TEST_STRATEGY.md)
- [Mobile architecture](docs/MOBILE_ARCHITECTURE.md)
- [Mobile test strategy](docs/MOBILE_TEST_STRATEGY.md)
- [Mobile design specification](docs/DESIGN_SPEC.md)
- [Roadmap](docs/ROADMAP.md)
