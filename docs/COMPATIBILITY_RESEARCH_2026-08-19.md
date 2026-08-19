# Compatibility Research — 2026-08-19

## Purpose

This record documents the controlled compatibility work completed after the post-R8 audit. It distinguishes a version that is merely newer from a toolchain and dependency combination that has both an authoritative prerequisite and IVAI-specific regression evidence.

## Current Validated Baseline

| Component | Current version or policy |
|---|---:|
| Android Gradle Plugin | 9.3.1 |
| Gradle wrapper | 9.7.1 |
| `compileSdk` / `targetSdk` | 37 / 37 |
| Kotlin Compose plugin | 2.4.10 |
| KSP | 2.3.11 |
| Compose BOM | 2026.08.00 |
| AndroidX Core | 1.19.0 |
| Lifecycle | 2.11.0 |
| Activity Compose | 1.13.0 |
| Navigation Compose | 2.9.8 |
| Roborazzi | 1.72.0 |
| Room / DataStore | 2.8.4 / 1.2.1 |
| Launcher resources | Adaptive-only `mipmap-anydpi`; `minSdk 29` |

> **Validated quality snapshot:** local `lintDebug` and the protected merged-main CI after [PR #110](https://github.com/ILIV007/IVAI-App/pull/110) report **0 errors, 0 warnings, and 0 hints**. The validated suite contains **155 tests, 0 failures, 0 errors, and 0 skipped**. The local release-candidate helper also passed with an unsigned, checksum-verified package for the launcher-policy commit; it is not a signed or public Alpha artifact.[1]

## Official Constraints and Project Evidence

| Constraint or source | Interpretation for IVAI | Project result |
|---|---|---|
| AGP 9.3 release notes require Gradle 9.5.0 or newer and support API 37.[2] | Upgrade AGP and wrapper together; API 37 removes the prior Core/Lifecycle prerequisite. | [PR #105](https://github.com/ILIV007/IVAI-App/pull/105) validated AGP 9.3.1, Gradle 9.7.1, API 37 and release resource shrinking with debug/release/R8, tests, lint and CI. |
| Kotlin 2.4 documents Gradle 9.5.0 compatibility and metadata-annotation changes.[3] | Kotlin must be tested with KSP/Room and the complete test suite; it is not a cosmetic pin change. | [PR #109](https://github.com/ILIV007/IVAI-App/pull/109) validated Kotlin Compose plugin 2.4.10 with KSP 2.3.11, Room KSP, R8, tests, lint and CI. |
| KSP 2.3.10 fixed AGP 9 built-in Kotlin R-class resolution and Kotlin 2.4 module-name behavior; 2.3.11 is the later current release.[4] | Retain KSP 2.3.11 during the Kotlin migration, then verify generated sources in debug, release and unit-test tasks. | All KSP tasks passed in the controlled migration and protected CI; no KSP pin change was needed. |
| Lifecycle 2.11 and Core 1.19 require the newer Android build/API baseline previously absent from IVAI.[5] [6] | Upgrade only after AGP/API prerequisites are validated. | [PR #106](https://github.com/ILIV007/IVAI-App/pull/106) upgraded Core 1.19.0 and Lifecycle 2.11.0 with clean build, R8, tests, lint, guards and CI. |
| The Compose compiler Gradle plugin is coupled to Kotlin, not to a BOM version map; Compose test rule v1 is deprecated in the current BOM.[7] | Keep the plugin approach, advance the BOM separately, then modernize test API usage in its own regression-focused change. | [PR #107](https://github.com/ILIV007/IVAI-App/pull/107) advanced the BOM; [PR #108](https://github.com/ILIV007/IVAI-App/pull/108) migrated all eight local rules to v2 after 155 tests passed. |
| Android resource lint flags `-v26` launcher resources when `minSdk` is 29. | Adaptive XML may be unqualified only when legacy bitmap fallbacks are removed, avoiding mixed icon resources. | [PR #110](https://github.com/ILIV007/IVAI-App/pull/110) moved both adaptive icons to `mipmap-anydpi`, removed unreachable fallback bitmaps, updated generator/guard, and reached lint zero. |

## Controlled Migration Sequence

The compatibility phase was intentionally split into bounded PRs rather than one stack upgrade. Each change passed local clean debug and minified-release builds, R8, the full unit/Roborazzi coverage, lint, IVAI architecture/safety guards and protected GitHub CI before the next change began.

| PR | Focus | Result |
|---|---|---|
| [#99](https://github.com/ILIV007/IVAI-App/pull/99) | CI Android SDK action | Replaced `setup-android@v3` with v4; the final Node.js 20 runner warning was removed without application-source changes. |
| [#103](https://github.com/ILIV007/IVAI-App/pull/103) | UI lint hygiene | Removed eight `ModifierParameter` warnings and two `AutoboxingStateCreation` hints without changing UI behavior. |
| [#105](https://github.com/ILIV007/IVAI-App/pull/105) | Build system | Validated AGP 9.3.1, Gradle 9.7.1, API 37 and resource shrinking. |
| [#106](https://github.com/ILIV007/IVAI-App/pull/106) | AndroidX baseline | Upgraded Core 1.19.0 and Lifecycle 2.11.0 after their prerequisites were met. |
| [#107](https://github.com/ILIV007/IVAI-App/pull/107) | Compose BOM | Upgraded to 2026.08.00 in an independent change. |
| [#108](https://github.com/ILIV007/IVAI-App/pull/108) | Compose test API | Migrated all eight test-rule imports to the v2 API; assertions and synchronization remained intact. |
| [#109](https://github.com/ILIV007/IVAI-App/pull/109) | Kotlin / source remediation | Advanced Kotlin Compose plugin to 2.4.10 and resolved compiler diagnostics without changing vault, reset, provider or endpoint semantics. |
| [#110](https://github.com/ILIV007/IVAI-App/pull/110) | Launcher policy | Made the minSdk-29 adaptive-only policy explicit and eliminated the final lint warning. |

## Compatibility Decision

The previously deferred toolchain, Core, Lifecycle, Compose and launcher findings are **resolved for the validated baseline above**. The protected `main` workflow for the final launcher commit passed both Android quality and Secret scan gates.[8]

This result is not a claim that all future versions are automatically compatible. Any future upgrade of AGP, Gradle, Kotlin, KSP, Compose, AndroidX, compile/target SDK or launcher policy remains a dedicated compatibility change and must rerun the existing debug/release/R8/test/lint/guard/CI sequence. No Provider/Account/Model/Combo/Agent behavior, credential storage, endpoint networking, telemetry, backend, signing or release policy changed in this phase.

## Remaining Release Gates Outside Compatibility

The toolchain is current and lint is clean, but compatibility evidence does not close device, accessibility, network, participant or release-signing gates. Those P0s remain governed by the canonical UI-quality and P0 monitor.[9]

| Gate | Status |
|---|---|
| Participant usability, card-sort, tree-test and safety-comprehension evidence | Pending real, de-identified sessions. |
| Independent heuristic review and reconciliation | Pending real reviewers and resolved P0/P1 findings. |
| Compact/medium device, Force-RTL, TalkBack, font-scale and launcher checks | Pending physical-device evidence. |
| HTTPS loopback/private-LAN cancellation, timeout and offline checks | Pending controlled physical-device evidence with no secret/raw-log commit. |
| Signed artifact, SHA-256, annotated tag, release notes and owner approval | Pending; Alpha remains not approved. |

## Sources

1. [Final merged-main Android quality run after PR #110](https://github.com/ILIV007/IVAI-App/actions/runs/32294048552)
2. [Android Gradle Plugin 9.3 release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
3. [What’s new in Kotlin 2.4.0](https://kotlinlang.org/docs/whatsnew24.html)
4. [KSP releases](https://github.com/google/ksp/releases)
5. [AndroidX Lifecycle release notes](https://developer.android.com/jetpack/androidx/releases/lifecycle)
6. [AndroidX Core release notes](https://developer.android.com/jetpack/androidx/releases/core)
7. [Compose compiler compatibility guidance](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)
8. [PR #110 — adaptive-only launcher resources](https://github.com/ILIV007/IVAI-App/pull/110)
9. [UI-Quality Lint Triage and Phase 7.5 / Alpha P0 Monitor](UI_QUALITY_AND_P0_MONITOR_2026-08-19.md)
