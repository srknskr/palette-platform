# Mobile Test Strategy

## Testing Layers

1. **Shared Domain & Repository Unit Tests (`shared/src/commonTest`)**:
   - `ColorValidatorTest`: Verification of 4-color constraints, uppercase normalization, duplicate prevention, and hex formatting.
   - `DtoSerializationTest`: Exact JSON parsing matching backend REST DTOs and ProblemDetail responses.
   - `AuthRepositoryTest`: Token storage integration, login, logout, and reactive state transitions.
   - `FavoriteRepositoryTest`: Optimistic local state updates and rollback behavior upon server error.

2. **Android Unit & ViewModel Tests (`androidApp/src/test`)**:
   - `DiscoverViewModelTest`: Pagination, sorting (newest/popular/random), search filtering, and favorite actions.
   - `CreatePaletteViewModelTest`: Client-side validation, error mapping, and submission lifecycle.

3. **Android UI & Component Tests (`androidApp/src/androidTest`)**:
   - `PaletteCardTest`: Compose test rule verifying palette card rendering, accessibility semantics, and click triggers.

4. **iOS Tests (`iosApp/iosAppTests`)**:
   - `ColorValidatorTests`: Swift-side validation checks against KMP shared domain models.
   - `DiscoverViewModelTests`: State initialization and sorting defaults.

5. **End-to-End Tests (`mobile/e2e`)**:
   - `user_flow.yaml`: Maestro flow asserting anonymous browsing, account registration, 4-color palette creation, favoriting, and logout.

## Verification Commands

- **Shared Tests**:
  ```bash
  cd mobile
  ./gradlew :shared:allTests
  ```

- **Android Unit Tests**:
  ```bash
  cd mobile
  ./gradlew :androidApp:testDebugUnitTest
  ```

- **Android Debug APK Build**:
  ```bash
  cd mobile
  ./gradlew :androidApp:assembleDebug
  ```

- **iOS KMP Framework Compilation**:
  ```bash
  cd mobile
  ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
  ```

- **iOS Simulator Application Build**:
  ```bash
  cd mobile
  xcodebuild -project iosApp/iosApp.xcodeproj \
    -target iosApp \
    -sdk iphonesimulator \
    -arch arm64 \
    CODE_SIGNING_ALLOWED=NO \
    CODE_SIGN_IDENTITY="" \
    CODE_SIGNING_REQUIRED=NO \
    build
  ```
