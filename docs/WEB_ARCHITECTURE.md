# Web Application Architecture

The `web/` module is a Single Page Application (SPA) providing a fast, responsive user experience for color palette discovery, curation, and sharing.

## Technology Stack

- **Framework**: Vue 3 (Composition API with `<script setup lang="ts">`)
- **Language**: TypeScript (strict mode enabled)
- **Tooling & Bundler**: Vite
- **Routing**: Vue Router 4 with route-level authentication guards and scroll restoration
- **State Management**: Pinia stores (`auth`, `theme`, `toast`)
- **HTTP Client**: Axios with automatic Bearer token injection and seamless refresh token rotation queue
- **Icons**: Lucide Vue Next
- **Styling**: Vanilla CSS custom properties (design tokens) ensuring zero-runtime overhead, instant theme switching, and alignment with mobile design specifications
- **Testing**: Vitest, Vue Test Utils, Playwright
- **Container**: Multi-stage Docker build targeting `nginxinc/nginx-unprivileged:alpine-slim`

## Design System & Theme Alignment

The web app design strictly mirrors the Android and iOS design language defined in [DESIGN_SPEC.md](DESIGN_SPEC.md):
- **Core Ivory Canvas**: `#F6F4EF`
- **Dark Mode**: Elevated dark tones with contrast ratio >= 4.5:1 (WCAG AA compliant)
- **Palette Card Hierarchy**: 4-bar stacked vertical proportion (`70px`, `45px`, `35px`, `30px`)
- **Navigation Shell**:
  - Desktop: Persistent left sidebar (`260px`)
  - Mobile: Fixed bottom navigation bar with tactile touch targets

## Deployment

The web client runs behind an unprivileged Nginx reverse proxy which:
1. Serves static, cache-busted HTML, CSS, and JS assets with `Cache-Control: public, immutable`.
2. Proxies `/api/` and `/actuator/` requests upstream to the backend service.
3. Implements standard security headers (`X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy`).
