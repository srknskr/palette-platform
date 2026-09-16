# AI Contributor Instructions

These instructions apply to Codex, Claude, Gemini, and other coding agents working in this repository.

## Product boundaries

- This product is a color-palette discovery and collection platform.
- Keep the backend, web client, and mobile client independently buildable.
- Do not introduce a second backend runtime. Spring Boot is the only application backend.
- Node.js may be used for web tooling, not for a duplicate API.
- Prefer a modular monolith until measured scale justifies another architecture.

## Backend rules

- Use Kotlin and constructor injection. Field injection is forbidden.
- Keep HTTP DTOs separate from persistence entities.
- Controllers translate HTTP; services own use cases and business rules; repositories own persistence.
- Never expose password hashes, refresh tokens, or internal entities from API responses.
- Validate input at the boundary and enforce important invariants again in the domain/service layer.
- Database changes require a new Flyway migration. Never edit an applied migration.
- PostgreSQL is the production database. Do not rely on H2-specific behavior.
- Authorization must check resource ownership, not only authentication.
- Never commit credentials or production secrets.

## Testing rules

- Every business rule requires a unit test.
- Every persistence query requires a PostgreSQL Testcontainers integration test.
- Every endpoint requires success, validation, authentication, authorization, and not-found coverage where applicable.
- Bug fixes require a regression test that fails before the fix.
- Do not delete or weaken tests to make a build pass.

## Change discipline

- Work in one vertical slice at a time.
- Do not modify unrelated modules.
- Run the smallest relevant test suite during development and the complete affected module suite before finishing.
- Update OpenAPI and relevant documentation when behavior changes.
- Explain non-obvious decisions in code or an ADR, not in tool-specific chat history.

## Definition of done

A change is complete only when it builds, tests pass, validation and errors are handled, documentation is updated, and no secret or generated build output is committed.
