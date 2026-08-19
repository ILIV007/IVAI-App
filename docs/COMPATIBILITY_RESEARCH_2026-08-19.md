# Compatibility Research — 2026-08-19

## Purpose

This record supports a controlled dependency/toolchain compatibility phase after the post-R8 audit. It separates versions that are merely newer from combinations that are officially compatible with IVAI's Android/KSP/Compose/Roborazzi build.

## Current Baseline

| Component | Current version |
|---|---:|
| Android Gradle Plugin | 9.1.1 |
| Gradle wrapper | 9.3.1 |
| Kotlin Compose plugin | 2.2.10 |
| KSP | 2.3.11 |
| Compose BOM | 2024.09.00 |
| Lifecycle | 2.8.7 |
| Activity Compose | 1.13.0 |
| Navigation Compose | 2.9.8 |
| Roborazzi | 1.72.0 |
| Room / DataStore | 2.8.4 / 1.2.1 |

## Official Compatibility Findings

| Source | Verified finding | Consequence for IVAI |
|---|---|---|
| Android Gradle Plugin 9.3 release notes | AGP 9.3 requires Gradle 9.5.0 or newer and supports API 37. | It is not a drop-in patch for the current wrapper. |
| Kotlin Gradle documentation | Kotlin 2.4.0–2.4.10 is fully supported with Gradle 7.6.3–9.5.0 and AGP 8.5.2–9.1.0. | Upgrading Kotlin alone does not create a fully documented path to AGP 9.3; AGP/Kotlin/KSP must be planned together. |
| Compose compiler compatibility guidance | Kotlin 2.0+ uses the Compose Compiler Gradle plugin instead of a Compose-to-Kotlin version map. | The project’s Compose plugin approach remains correct; no compiler-extension pin is added. |
| KSP official release list | The published KSP release stream currently lists 2.3.x; no matching 2.4.x release tag was found in the official release list reviewed for this phase. | Do not advance Kotlin to 2.4.x until a matching KSP release and project regression evidence are available. |
| AndroidX Activity/Lifecycle/Navigation release notes | Current stable releases are Activity 1.13.0, Lifecycle 2.11.0, and Navigation 2.9.8. Lifecycle 2.11 and Navigation 2.10 preview notes state a minimum AGP 9.2.0 for their updated Compose compile SDK dependency. | Do not upgrade Lifecycle to 2.11 on current AGP 9.1.1. Navigation 2.9.8 and Activity 1.13.0 are candidates only after targeted source/test validation. |
| AndroidX releases | Room 2.8.4 and DataStore 1.2.1 remain stable current releases in the reviewed channel. | Keep Room/DataStore unchanged in this phase. |

## Upgrade Decision

This phase deliberately avoids a broad AGP/Gradle/Kotlin/KSP/Compose stack update. The currently available official compatibility statements do not demonstrate a single fully supported combination that simultaneously upgrades the project’s AGP, Kotlin, Gradle, and KSP pins. Such a change belongs in a dedicated build-system migration with exact KSP availability, post-upgrade dependency-resolution review, full Roborazzi baseline review, release-R8 validation, and physical-device follow-up.

The safe candidate surface is therefore narrowed to dependency versions that do not require an AGP/Kotlin/KSP migration. Each candidate remains subject to compilation, test, lint, screenshot artifact, release build, and CI verification before merge.

## Completed Focused Refresh and CI Maintenance

The focused AndroidX/screenshot refresh is complete in [PR #97](https://github.com/ILIV007/IVAI-App/pull/97). Activity Compose 1.13.0, Navigation Compose 2.9.8, and Roborazzi 1.72.0 passed the project’s targeted unit, screenshot-baseline, minified-release, lint, and protected-CI checks. Core 1.19.0 remains intentionally excluded because it requires `compileSdk 37`; IVAI remains on `compileSdk 36.1`. Room, DataStore, Lifecycle, the Compose BOM, and the AGP/Gradle/Kotlin/KSP tuple are unchanged.

The final runner-runtime warning was removed in [PR #99](https://github.com/ILIV007/IVAI-App/pull/99) by advancing `android-actions/setup-android` from v3 to v4. The official v4 action metadata specifies `using: node24`; the merged-main Android quality run completed successfully without a Node.js 20 deprecation annotation. This CI-only maintenance change does not alter application source, dependencies, Provider behavior, network policy, secrets, or release gates.

> **Current lint snapshot:** the post-#99 local `lintDebug` report records **0 errors, 17 warnings, and 2 hints**. The source/dependency diff for PR #99 is empty: this snapshot is not attributed to the CI action upgrade. The warnings are eight `ModifierParameter`, five `GradleDependency`, two `AndroidGradlePluginVersion`, one `NewerVersionAvailable`, and one deliberate launcher `ObsoleteSdkInt`; the two hints are `AutoboxingStateCreation`. They are non-blocking, but should be reconciled only in dedicated UI-quality or documented toolchain/launcher decisions rather than folded into CI maintenance.

## Sources

1. [Android Gradle Plugin 9.3 release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
2. [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
3. [Kotlin Gradle configuration and compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
4. [Compose compiler compatibility guidance](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)
5. [KSP releases](https://github.com/google/ksp/releases)
6. [AndroidX Activity release notes](https://developer.android.com/jetpack/androidx/releases/activity)
7. [AndroidX Lifecycle release notes](https://developer.android.com/jetpack/androidx/releases/lifecycle)
8. [AndroidX Navigation release notes](https://developer.android.com/jetpack/androidx/releases/navigation)
9. [AndroidX release index](https://developer.android.com/jetpack/androidx/versions/all-channel)
10. [PR #97 — Compatibility refresh](https://github.com/ILIV007/IVAI-App/pull/97)
11. [PR #99 — Node 24 Android setup action](https://github.com/ILIV007/IVAI-App/pull/99)
12. [Merged-main Android quality run after PR #99](https://github.com/ILIV007/IVAI-App/actions/runs/32263321814)
