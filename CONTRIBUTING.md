# Contributing to Palette Platform

Thank you for contributing to Palette Platform! This project is a multi-platform color discovery and curation suite comprising a Spring Boot backend, a Vue.js 3 web application, and mobile clients (KMP, Jetpack Compose, SwiftUI).

## Principles & Guidelines

- **Product Focus**: Color palette discovery, community curation, and design inspiration.
- **Independent Modules**: Backend, web, and mobile sub-projects build independently.
- **Vertical Slices**: Complete feature slices end-to-end with unit and integration tests.
- **Code Consistency**:
  - Backend: Kotlin, constructor injection, Flyway migrations, DDD layers.
  - Web: Vue 3 (Composition API), TypeScript strict mode, Pinia, scoped CSS with design tokens.
  - Mobile: Kotlin Multiplatform + Compose & SwiftUI.

## Developing the Web Client

```bash
cd web
npm install
npm run dev        # Starts Vite dev server at http://localhost:3000
npm run test:unit  # Runs Vitest unit tests
npm run build      # Verifies TypeScript and builds production bundles
```

## Developing the Backend

```bash
cd backend
./gradlew test     # Runs JUnit tests
./gradlew detekt   # Runs Detekt static analysis
./gradlew bootRun  # Starts backend server on port 8080
```

## Submitting Pull Requests

1. Keep commits focused using Conventional Commits (e.g. `feat(web): add tag filtering`).
2. Verify all CI pipelines pass before requesting review.
