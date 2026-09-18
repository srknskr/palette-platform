# Deployment & Release Guide

Palette Platform supports automated release packaging and multi-container deployment.

## Production Container Images (GHCR)

Container images are automatically built and published to GitHub Container Registry upon pushing a semantic version tag (e.g. `v1.0.0`):

- **Backend**: `ghcr.io/srknskr/palette-platform-backend:latest` (and tagged `v1.0.0`)
- **Web**: `ghcr.io/srknskr/palette-platform-web:latest` (and tagged `v1.0.0`)

## Running the Complete Stack

Use Docker Compose to run the full stack (PostgreSQL 17, Spring Boot backend, and Vue.js web client):

```bash
docker compose -f infrastructure/docker/compose.yml --profile full up -d
```

Service endpoints:
- Web Application: `http://localhost:3000`
- Backend REST API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Probe: `http://localhost:8080/actuator/health`

## Release Process

1. Ensure all CI tests pass across backend, mobile, and web modules.
2. Update `CHANGELOG.md` with release highlights.
3. Tag the repository:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
4. The `.github/workflows/release.yml` pipeline will automatically:
   - Build the backend Spring Boot JAR.
   - Build and compress the web application dist bundle (`tar.gz`).
   - Build and push container images to GHCR.
   - Compute SHA256 checksums for all distribution artifacts.
   - Create a published GitHub Release with release notes and downloadable assets.
