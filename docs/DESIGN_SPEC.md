# Mobile Design Specification

## Identity & Aesthetics

- **Application Name**: Palette
- **Design Philosophy**: Minimalist, content-focused design inspired by color discovery products. Colors remain the prominent visual element.
- **Palette Representation**: Four stacked horizontal color bars with distinct heights (proportional hierarchy: 70dp, 45dp, 35dp, 30dp) inside 16dp rounded cards.
- **Color Scheme**:
  - Light mode: Neutral warm background (`#F9F7F5`), elevated crisp surfaces (`#FFFFFF`), dark graphite typography (`#1E1E1E`).
  - Dark mode: Deep neutral background (`#121212`), elevated surfaces (`#1E1E1E`), clean off-white typography (`#ECEFF4`).

## Navigation

Bottom tab navigation across both platforms:
1. **Discover**:
   - Real-time search query for name, HEX color, and tags.
   - Filter chips: Newest, Popular, Random.
   - Two-column adaptive grid on mobile screens.
   - Infinite scroll pagination and pull-to-refresh.
   - Empty, loading, and error states.
2. **Create**:
   - Four interactive color slots with reordering capabilities.
   - Integrated hex input and native color picker.
   - Client-side validation: exactly 4 colors, strict 6-digit hex format with `#`, duplicate color prevention.
   - Tag input and clean submission feedback.
3. **Collection**:
   - Favorited palettes feed with optimistic local updates.
   - Offline cached representation.
4. **Profile**:
   - Current user details, published palette counter, and log out.
   - Clean authentication modal / sheet with registration and login.

## Accessibility

- Minimum touch targets (48x48dp on Android, 44x44pt on iOS).
- High-contrast text labels.
- Screen reader accessibility labels (`contentDescription` in Compose, `.accessibilityLabel` in SwiftUI) for each individual color swatch.
