# Changelog

All notable changes to the Palette Platform project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-09-18

### Added
- **Web Application (`web/`)**:
  - Production-ready responsive Vue 3 application built with TypeScript, Vite, Vue Router, and Pinia.
  - Complete warm ivory design system matching mobile applications (`#F6F4EF`), with WCAG AA compliant light/dark modes and system preference detection.
  - 4-bar stacked vertical palette card hierarchy (70px, 45px, 35px, 30px) with copy-on-click and interactive favorite actions.
  - Discover view featuring sorting (Newest, Popular, Random), tag filters, instant search, and "I'm Feeling Lucky".
  - Interactive Palette Details view with CSS variables code snippet export and Web Share API integration.
  - Authenticated Create Palette view with live 4-color preview, individual HEX pickers, shuffle, and randomize actions.
  - Personal Collection view for managing favorited color schemes.
  - User Profile view with stats, created palettes list, and theme preferences.
  - Unit and component tests using Vitest and Vue Test Utils; E2E smoke tests with Playwright.
- **Packaging & Infrastructure**:
  - Multi-stage unprivileged Nginx Dockerfile for the web application with SPA fallback, gzip compression, and security headers.
  - Updated `infrastructure/docker/compose.yml` with the web client service.
- **CI/CD Automation**:
  - `.github/workflows/web-ci.yml` for automated linting, type-checking, testing, and Docker image build.
  - `.github/workflows/release.yml` automating release packaging, multi-platform Docker images publishing to GitHub Container Registry (GHCR), SHA256 checksums, and GitHub Releases on `v*` tags.
