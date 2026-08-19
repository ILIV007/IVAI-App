# Compatibility Refresh Plan — 2026-08-19

## Status

> **Status: focused library-refresh candidate.** This plan does not authorize a broad Gradle/AGP/Kotlin/KSP migration and does not alter IVAI runtime, local-first/BYOK, Provider, data, or release contracts.

## Compatibility Decision

The research record identifies a constrained build-system matrix: AGP 9.3 requires Gradle 9.5+, while Kotlin 2.4’s fully documented AGP range ends at 9.1 and the official KSP release stream reviewed for this phase has no matching Kotlin 2.4 tag. Lifecycle 2.11 also declares a newer Compose compile-SDK/AGP requirement. Therefore, those toolchain and Lifecycle changes are deferred to a future dedicated migration.

| Component group | Current | Decision in this branch | Reason |
|---|---:|---|---|
| Gradle / AGP / Kotlin / KSP | 9.3.1 / 9.1.1 / 2.2.10 / 2.3.11 | Keep pinned | No single fully documented upgrade tuple across all four tools was confirmed. |
| Compose BOM | 2024.09.00 | Keep pinned | A large UI library advance is coupled to Compose/AGP compatibility and visual baseline stability. |
| Lifecycle | 2.8.7 | Keep pinned | Lifecycle 2.11 requires a newer Compose compile SDK and AGP 9.2+. |
| Room / DataStore | 2.8.4 / 1.2.1 | Keep pinned | Already current stable in the reviewed release index. |
| Core KTX | 1.18.0 | Candidate: 1.19.0 | Stable AndroidX update; minSdk 29 is above the AndroidX default floor. |
| Activity Compose | 1.10.1 | Candidate: 1.13.0 | Stable release; validate back handling and UI regressions. |
| Navigation Compose | 2.8.9 | Candidate: 2.9.8 | Stable patch/minor release; validate destinations/back/selection regressions. |
| Roborazzi | 1.59.0 | Candidate: 1.72.0 | Test-only upgrade; acceptance requires all Compose/Robolectric tests and recorded visual artifact review. |

## Acceptance Criteria

1. Resolution, clean debug build, minified-release build, all unit tests, lint, and all existing architecture/security guards pass.
2. No Room schema, migration, Provider transport, Vault, endpoint/trust, Agent runtime, Reset, or R8 test-readiness behavior changes.
3. Roborazzi runs succeed in normal verification and record mode; new images are examined before any baseline is accepted.
4. Dependency resolution contains no preview release and no unreviewed dynamic version.
5. Lint warning count does not increase; any remaining warnings are documented as toolchain/launcher policy items.
6. If a candidate causes source, test, visual, dependency-resolution, or release-build regression, remove that candidate from this focused PR rather than expanding scope.

## Deferred Migration

A future toolchain migration must establish an exact supported AGP/Gradle/Kotlin/KSP tuple, verify Foojay resolver compatibility, inspect all dependency resolution changes, test configuration cache and Roborazzi baselines, run release R8, and collect the usual physical-device evidence. It is intentionally not combined with this library refresh.

## References

See [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md) for source links and full compatibility findings.
