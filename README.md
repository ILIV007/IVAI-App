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

Build debug APK:
```bash
./gradlew assembleDebug
```

Run unit tests:
```bash
./gradlew test
```

Or run unit tests specifically:
```bash
gradle :app:testDebugUnitTest
```

Note: No `.env` file or `GEMINI_API_KEY` configuration is required.
