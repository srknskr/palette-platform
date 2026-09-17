# Palette Mobile Application

Palette Mobile is the cross-platform client for the Palette Platform, built with Kotlin Multiplatform (KMP), Jetpack Compose for Android, and SwiftUI for iOS.

## Modules

- `shared`: Kotlin Multiplatform shared domain, network client, token storage, repositories, and use cases.
- `androidApp`: Native Android application using Jetpack Compose, Material 3, Navigation Compose, and Hilt.
- `iosApp`: Native iOS application using SwiftUI and Keychain Services.

## Build and Run

### Requirements
- Java 17+
- Android SDK 35+
- Xcode 15+ (for iOS)

### Commands

- **Run all shared tests**:
  ```bash
  ./gradlew :shared:allTests
  ```

- **Run Android unit tests**:
  ```bash
  ./gradlew :androidApp:testDebugUnitTest
  ```

- **Build Android debug APK**:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```

- **Compile KMP Framework for iOS**:
  ```bash
  ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
  ```

- **Build iOS Simulator App**:
  ```bash
  xcodebuild -project iosApp/iosApp.xcodeproj \
    -target iosApp \
    -sdk iphonesimulator \
    -arch arm64 \
    CODE_SIGNING_ALLOWED=NO \
    CODE_SIGN_IDENTITY="" \
    CODE_SIGNING_REQUIRED=NO \
    build
  ```
