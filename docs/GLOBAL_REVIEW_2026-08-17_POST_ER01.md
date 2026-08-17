# Global Review — Post ER-01 — 2026-08-17

> **Status:** Deterministic repository, architecture, security, and documentation review completed on `main` commit `6430f52` (`fix(chat): prevent duplicate Router user message (#46)`). This record is not physical-device evidence, participant research evidence, an Alpha approval, or a public-release authorization.

## Review Scope

This review examined the current main head after the focused ER-01 Router message fix. It covers the clean Android build and unit suite, lint, source security/architecture scans, release-gate documentation, protected GitHub configuration, and targeted review of Router/Agent/Vault/migration boundaries. It does not fabricate the physical-device, accessibility, network, participant, signing, or owner-release evidence that remains outside the sandbox.

| Control | Evidence | Result |
|---|---|---|
| Build | `./gradlew clean assembleDebug` under JDK 21 and the configured Android SDK | Pass |
| Unit suite | `testDebugUnitTest`: **125 tests**, **0 failures**, **0 errors**, **0 skipped** | Pass |
| Lint | `lintDebug`: **0 reported issues** | Pass |
| Repository integrity | Clean worktree; `git fsck --no-reflogs`; main and origin synchronized | Pass |
| Protected integration | One required approval, linear history, admin enforcement, force-push/deletion disabled, Secret scan and Build/unit-test/lint required | Pass |
| Main CI | PR #46 and its post-merge main workflow completed Secret scan and Build/unit-test/lint successfully | Pass |

## Architecture and Security Review

| Boundary | Deterministic result | Status |
|---|---|---|
| Local-first / Backendless | No central backend, hosted telemetry SDK, or implicit cloud-sync behavior found in the application source. | Pass |
| BYOK | No hard-coded secrets were found; Room remains an opaque-reference store and the Android Keystore vault remains the secret boundary. | Pass |
| Transport trust | No cleartext opt-in, permissive trust manager, or trust-all implementation found. HTTPS-only endpoint policy remains intact. | Pass |
| Provider neutrality | No default provider, automatic provider selection, or implicit selection marker found. | Pass |
| Prohibited execution | No `Runtime.exec`, `ProcessBuilder`, accessibility automation, or background-work framework found. | Pass |
| RTL boundary | No global forced-LTR override in the application shell or theme. | Pass |
| Data backup | Manifest retains `allowBackup=false` plus Android 11-and-lower and Android 12+ exclusion resources. | Pass |
| Router ER-01 | Stable Router user-message ID is now reused by the `Started` UI event and state is deduplicated by ID; regression coverage protects the invariant. | Pass |

## Targeted Findings and Decision

The previous external-review triage remains the authoritative record of hypotheses that were independently classified. ER-01 was reproduced, fixed, and merged in PR #46. No additional P0 deterministic defect or Local-first/Backendless/BYOK architectural deviation was found by this review.

| Item | Current decision | Rationale |
|---|---|---|
| ER-02 — calculator contract | Confirmed, unaddressed, separate P1 product-contract phase | The current tool validates numeric arithmetic input but does not calculate. A bounded evaluator or capability removal/rename needs a standalone design and regression tests. |
| ER-03 — concurrent write approval | Plausible P1 candidate, not yet reproduced | A one-time-write safety fix must begin with a concurrent resolution/cancel regression, not a speculative locking change. |
| ER-04 — partial-stream recovery | Confirmed P1 data-recovery candidate, not yet implemented | The desired incomplete-message UX/persistence contract must be defined before code changes. |
| Broad exception boundaries, release minification, archive hardening | Deferred P2/P3 work | Each requires targeted evidence/design; none is safe to bundle with the review or Router fix. |
| HTTP local endpoints, mDNS/LAN discovery, auto-discovery, hosted telemetry | Rejected/intentional exclusions | They would violate existing endpoint, privacy, or explicit-selection boundaries. |

## Documentation Consistency

The release-readiness checklist's current deterministic baseline is updated from 124 to **125** tests to include the ER-01 regression. Historical records retain their original counts and commit context. The Phase 7.5 hardening audit wording is clarified so its historical 14-warning statement is not represented as the current main state.

## Non-Deterministic Gates Still Open

The following are release gates, not defects that can be closed from the sandbox. Their correct status remains **Pending/Not approved** until genuine evidence is available.

| Gate | Required evidence |
|---|---|
| Usability and heuristic review | Voluntary, de-identified card-sort, tree-test, safety-comprehension, and independent heuristic-review outcomes. |
| Device/accessibility | Compact and medium Android device records for fresh install, upgrade, restart, rotation, offline, Force-RTL, TalkBack, font scale, and launcher observation. |
| Endpoint behavior | Explicit HTTPS loopback/private-LAN cancellation, timeout, and offline outcomes on a physical device; no HTTP, discovery, scan, or trust bypass. |
| Public Alpha | Owner-controlled signing, signed APK hash, immutable tag, reviewed notes, explicit owner approval, and independent download/hash verification. |

## Decision

The deterministic codebase is healthy for continued, focused development: build, tests, lint, scans, repository hygiene, and protected CI are green. This does **not** approve Alpha release. The next development item remains ER-02 as a separate bounded calculator-contract phase; Phase 7.5 field evidence remains deferred rather than complete.

## References

- [External Review Triage](EXTERNAL_REVIEW_TRIAGE_2026-08-17.md)
- [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
- [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)
- [Phase 7.5 Hardening Audit](PHASE7_5_HARDENING_AUDIT.md)
- [Alpha Release Checklist](ALPHA_RELEASE.md)
