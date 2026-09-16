# Delivery Roadmap

## Milestone 1 — Foundation

- [x] Monorepo structure and contributor rules
- [x] Spring Boot application
- [x] PostgreSQL Docker Compose service
- [x] Flyway baseline schema
- [x] Health endpoint and context smoke test
- [x] Backend CI workflow

## Milestone 2 — First vertical slice

- [ ] Palette domain model
- [ ] Create palette use case
- [ ] List newest palettes with pagination
- [ ] DTO validation for exactly four HEX colors
- [ ] Unit, repository, API, and integration tests
- [ ] OpenAPI documentation

## Milestone 3 — Identity and security

- [ ] Registration and password hashing
- [ ] Login and short-lived JWT access token
- [ ] Refresh-token rotation and logout
- [ ] Ownership authorization
- [ ] Security regression tests

## Milestone 4 — Discovery and collections

- [ ] Popular and random feeds
- [ ] Tags and color search
- [ ] Favorite/unfavorite
- [ ] Personal collection
- [ ] Moderation workflow

## Milestone 5 — Mobile

- [ ] KMP shared networking and repositories
- [ ] Android Compose application
- [ ] iOS SwiftUI application
- [ ] Offline cache and synchronization
- [ ] Unit, UI, and Maestro tests

## Milestone 6 — Web and release

- [ ] Vue 3 + TypeScript application
- [ ] Vitest and Playwright tests
- [ ] Observability and security hardening
- [ ] Production deployment
- [ ] Play Store and App Store release preparation
