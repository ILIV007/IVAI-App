# IVAI — Task 01 UI Skeleton

IVAI Task 01 is a mock-only UI skeleton demonstrating the core user interface, navigation, design tokens, state representations, and RTL/BiDi content rendering for IVAI.

## Architecture & Tech Stack

- **Framework**: Kotlin + Jetpack Compose + Material 3
- **Package**: `dev.iliv007.ivai`
- **Min SDK**: 29
- **Target SDK**: 36
- **Architecture**: Single-activity shell with Compose Navigation and UI state management
- **Backend**: None (Task 01 is a pure mock UI skeleton)
- **Firebase**: None
- **Analytics**: None
- **Provider Calls**: None (all provider, model, agent, and routing flows are simulated mock previews)
- **API Keys**: None required

## Language & Directionality

- **App Shell**: English (LTR)
- **Content Rendering**: Bidirectional (RTL/BiDi) prototype supporting mixed language rendering (Hebrew/Arabic prose with explicit LTR inline code, paths, model IDs, logs, and URLs)

## Local Build & Test Commands

### Prerequisites

Install Android Studio with a **full JDK 17 or later** and the Android SDK platform required by this project (`compileSdk 36.1`), including the corresponding SDK Build Tools. The project uses Android Gradle Plugin 9.1.1 and Gradle 9.3.1.

For command-line builds, configure `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to your Android SDK directory. Alternatively, let Android Studio generate an untracked `local.properties` file, for example:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

`local.properties` is machine-specific and must not be committed.

Build a debug APK:
```bash
./gradlew assembleDebug
```

Run all unit tests:
```bash
./gradlew test
```

Run the debug unit-test task specifically:
```bash
./gradlew :app:testDebugUnitTest
```

Verify the installed Gradle Wrapper before troubleshooting a build:
```bash
./gradlew --version
```

Note: No `.env` file or `GEMINI_API_KEY` configuration is required because Task 01 contains no real provider integration.
