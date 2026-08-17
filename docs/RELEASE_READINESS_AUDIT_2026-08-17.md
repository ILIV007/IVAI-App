# Release Readiness Audit — 2026-08-17

> **Checklist:** [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
>
> **Audited source baseline:** `ab85e4489f4e7968b77e52a4b3124217477282e9` (`main` after PR #42).
>
> **Decision:** The deterministic implementation is stable enough to continue controlled Phase 7.5 field validation. It is **not approved** for a public GitHub Alpha download, tag, or release artifact.

## Scope and Integrity

The source baseline was reviewed from a clean checkout using JDK 21 and the local Android SDK. The checklist and this audit are documentation-only follow-up files on a focused branch; they do not change Android runtime code, Provider/Router/Agent/Data behavior, Room schema, credential handling, endpoint policy, permissions, network behavior, or signing configuration.

| Control | Evidence | Result |
|---|---|---|
| Candidate source | `main` at `ab85e44`; working tree clean before documentation follow-up | Pass |
| Git integrity | `git fsck --full --no-reflogs` | Pass |
| Tracked sensitive files | Review for `local.properties`, keystore/certificate, PEM/key, and `.env` files | Pass — none tracked |
| Protected main | One required approval; linear history; admin enforcement; force-push and deletion disabled; Secret scan and Android quality checks required | Pass |
| Latest main CI | [Android quality run](https://github.com/ILIV007/IVAI-App/actions/runs/32055067539) on `ab85e44` | Pass — both required jobs successful |
| Repository hygiene | No open pull request, no open issue, and no remote branch other than `main` at review time | Pass |

## Deterministic Build and Regression Gate

The build gate was run locally before this audit. The unit suite was then run again with `--rerun-tasks`, so the test result is execution evidence rather than a Gradle cache-only result.

| Check | Result | Disposition |
|---|---|---|
| `clean assembleDebug testDebugUnitTest lintDebug` | `BUILD SUCCESSFUL`; debug APK produced at `app/build/outputs/apk/debug/app-debug.apk` | Pass |
| Independent `testDebugUnitTest --rerun-tasks` | **124 tests**, **0 failures**, **0 errors**, **0 skipped** | Pass |
| Lint | **0 Error**, **0 Fatal**, 14 Warning notices | Pass with documented deferred notices |
| Warning disposition | 13 dependency/toolchain currency notices and 1 `ObsoleteSdkInt` adaptive-icon-resource notice | Deferred to a dedicated dependency/resource compatibility increment; not a hidden release approval |
| Whitespace | `git diff --check` | Pass |
| Screenshot/semantics | Existing Roborazzi/Compose tests included in successful unit suite | Pass for deterministic coverage only |

> The locally built `app-debug.apk` is a development artifact. It has **not** been published, committed, attached to a GitHub Release, or represented as an Alpha binary.

## Architecture and Security Invariants

All scans below were run against `app/src/main`. Descriptive Settings copy and Android XML namespace URLs were reviewed separately and not treated as executable telemetry or cleartext transport.

| Gate | Result | Evidence summary |
|---|---|---|
| No hardcoded secret | Pass | No Google/OpenAI-style key or literal API-key assignment found. |
| No cleartext/trust bypass | Pass | No cleartext opt-in, trust-all code, permissive trust manager, or hostname-verifier bypass found. |
| No implicit target/provider | Pass | No default/auto/implicit provider, model, Combo, or target-selection pattern found. |
| No hosted telemetry/backend SDK | Pass | No Firebase, Sentry, Amplitude, Mixpanel, PostHog, OpenReplay, Matomo, or Clarity SDK import found. |
| No prohibited execution | Pass | No WorkManager/JobScheduler/AlarmManager, process execution, AccessibilityService, Shizuku, Termux, or MCP execution path found. |
| No cleartext Kotlin URL | Pass | No executable `http://` literal found in Kotlin sources. |
| No global forced LTR | Pass | No forced LTR provider found in `MainActivity.kt` or `Theme.kt`; bounded code/terminal/footer exceptions remain governed by Phase 7.5. |
| Local data policy | Pass | Manifest retains `allowBackup="false"`, Android 12+ data-extraction rules, and legacy full-backup rules; resource regression coverage exists. |
| Endpoint policy | Pass | Tests cover public remote HTTPS, exact HTTPS loopback, RFC1918 HTTPS LAN, parser-bypass rejection, confirmation, and no-auth credential constraints. |
| Agent safety | Pass | Tests cover explicit final target review, cancellation, failed-target recovery without auto-change, and bounded path/preview with one-time approval. |

## Evidence That Cannot Be Claimed Yet

The following rows remain release blockers. They cannot be satisfied by CI, Robolectric, Roborazzi, a sandbox build, or an agent-generated result. No participant, device, endpoint, signing, or Alpha-release outcome has been fabricated.

| Release blocker | Current state | Required evidence |
|---|---|---|
| Card sort, tree test, safety comprehension, heuristic review | Pending | Voluntary de-identified Phase 7.5 findings; thresholds met or focused remediation/retest completed. |
| Compact and medium physical-device matrix | Pending | Fresh install, upgrade, restart, rotation, dark/light, default/large font, offline, Force-RTL, and TalkBack evidence without sensitive content. |
| Physical HTTPS endpoint behavior | Pending | Explicit loopback/private-LAN HTTPS cancellation, timeout, and offline outcomes; no HTTP, discovery, scanning, trust bypass, or duplicate side effect. |
| Themed launcher behavior | Pending | Observation on supported OEM/device launcher; no implication for product UI branding. |
| Release signing | Pending owner control | Owner-approved release keystore workflow, without committing or exposing signing material. |
| Artifact integrity and publication | Pending | Signed APK, SHA-256, annotated tag, reviewed release notes, GitHub Release attachment, and independent post-download hash check. |

## Download Decision

**Public download remains blocked.** Publishing `app-debug.apk` would contradict the documented Alpha boundaries because it is unsigned and the Phase 7.5/device evidence and owner approval are incomplete. The only artifact currently allowed is the controlled, local-only research package described in [Phase 7.5 Field Kit](PHASE7_5_FIELD_KIT.md); it must not be committed or publicly released.

When every blocker is closed, publication must follow the sequence in the [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md) and [GitHub Alpha Release Checklist](ALPHA_RELEASE.md): build the approved signed APK, generate the exact SHA-256, create an annotated tag, prepare reviewed notes, attach only the signed APK plus checksum to the GitHub Release, and independently verify the public download hash.
