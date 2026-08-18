# IVAI Development Guide

## Purpose

Use this file as a concise implementation guide for IVAI. It complements the product roadmap and security documents; it does not replace their release, architecture, or provider-neutral requirements.

## Environment

Use JDK 21 and an Android SDK matching `compileSdk 36.1`. Verify the project with:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

## Implementation rules

- Make one focused change set per accepted task packet.
- Preserve the current Compose UI and test-tag contracts unless their change is intentional and covered by tests.
- Treat user content as BiDi content; keep code, URLs, paths, model IDs, timestamps, and tokens LTR.
- Do not add provider calls until the local data and Keystore security phase is complete.
- Do not introduce backend services, telemetry, race/council routing, local model inference, shell/Termux/Shizuku, MCP process execution, or autonomous actions into Alpha work.
- Use clean-room implementations only; external reference code is not a copy source.

## Handoff

Report changed files, commands run and outcomes, data/security/RTL impact, known limitations, and explicit scope deviations. Update relevant docs when an accepted decision changes.
