# Mobile Architecture

## Overview

Palette Mobile is built using Kotlin Multiplatform (KMP) to maximize cross-platform code reuse while delivering native, uncompromised user experiences on both Android (Jetpack Compose, Material 3) and iOS (SwiftUI).

## Layered Structure

```text
mobile/
├── shared/         KMP module (domain, network, storage, repositories, use cases)
├── androidApp/     Android Application (Jetpack Compose, Navigation Compose, Hilt)
└── iosApp/         iOS Application (SwiftUI, NavigationStack, Keychain)
```

## Data Flow

```text
UI (Compose / SwiftUI)
   ↓
ViewModel (StateFlow / ObservableObject)
   ↓
Use Cases (Domain rules, validations)
   ↓
Repository (Single source of truth)
   ↓
Remote API (Ktor Client) / Local Cache (StateFlow / In-memory / Keychain / Keystore)
```

## Shared Components

1. **Networking (`com.palette.mobile.network`)**:
   - Built on Ktor Client 3.x with content negotiation and Kotlinx Serialization.
   - Centralized RFC 9457 `ProblemDetail` parsing and mapping to typed domain errors.
   - Interceptors for bearer authentication and automatic refresh token rotation.

2. **Authentication & Storage (`com.palette.mobile.auth`)**:
   - Clean `TokenStorage` contract with platform-native secure persistence:
     - Android: `EncryptedSharedPreferences` backed by Android Keystore (AES256-GCM / AES256-SIV).
     - iOS: iOS Keychain Services API.
   - `AuthRepository` managing reactive authentication state (`AuthState.Authenticated`, `AuthState.Unauthenticated`, `AuthState.Loading`).

3. **Palette Discovery & Collection (`com.palette.mobile.palette`, `com.palette.mobile.favorite`)**:
   - Domain models: `Palette`, `PaletteFilter`, `PaletteSort` (New, Popular, Random).
   - Optimistic favorite toggling with immediate UI feedback and rollback on failure.
   - Offline cached fallback for feeds and user collections.

## Native Applications

### Android (Jetpack Compose)
- **Framework**: Jetpack Compose with Material 3 theming.
- **Dependency Injection**: Hilt.
- **Navigation**: Navigation Compose with bottom navigation bar.
- **State Management**: Kotlin StateFlow collected lifecycle-aware in Compose.

### iOS (SwiftUI)
- **Framework**: SwiftUI on iOS 17+.
- **Architecture**: MVVM with `ObservableObject` and `@Published` properties.
- **Navigation**: Native `TabView` with `NavigationStack`.
- **KMP Interop**: Direct static framework integration with Swift async/await concurrency.
