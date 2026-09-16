# Test Strategy

The project uses a test pyramid with a small number of high-value end-to-end tests.

| Level | Backend tools | Purpose |
| --- | --- | --- |
| Unit | JUnit 5, MockK | Domain and service rules without Spring |
| Web slice | MockMvc, Spring Security Test | HTTP mapping, validation, and authorization |
| Repository | Data JPA Test, Testcontainers PostgreSQL | Queries, constraints, and mappings |
| Integration | Spring Boot Test, Testcontainers PostgreSQL | Complete use cases across layers |
| Contract | OpenAPI validation | Prevent client/server contract drift |
| End-to-end | Playwright/Maestro | Critical user journeys through a running system |

## Required scenarios per feature

- Happy path
- Boundary and malformed input
- Unauthenticated request
- Authenticated but unauthorized request
- Missing resource
- Duplicate/idempotent request
- Persistence constraint failure where relevant

## Regression policy

Every defect must first be represented by a failing automated test. The fix is accepted when that test and the affected suite pass. CI is the regression gate for merges.

## Test data

- Prefer builders and factories over shared mutable fixtures.
- Use a deterministic clock and ID generator in unit tests.
- Integration tests own their data and must not depend on execution order.
- Never call production services from tests.

## CI stages

1. Compile and static checks
2. Unit tests
3. Integration tests with PostgreSQL
4. Dependency/security scanning
5. Container build
6. End-to-end tests for deployable milestones
