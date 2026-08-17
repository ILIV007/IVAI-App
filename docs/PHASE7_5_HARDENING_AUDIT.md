# Phase 7.5 — Hardening Readiness Audit

> **Status:** Deterministic audit merged to `main` in [PR #37](https://github.com/ILIV007/IVAI-App/pull/37); external UX and physical-device evidence remains pending. This is **not** an Alpha release approval.
>
> **Baseline under review:** `62beef3` (Phase 7.5 deterministic hardening merged main).

## Verified Alignment

| Review area | Evidence | Status |
|---|---|---|
| Build and test baseline | After Phase 7.4, a clean build/test/lint pass and a separate `testDebugUnitTest --rerun-tasks` run executed all **122** baseline tests with 0 failures, 0 errors, and 0 skipped. After the Phase 7.5 hardening change, the final clean build/test/lint run executed **123** tests with 0 failures, 0 errors, and 0 skipped. | Verified |
| No-backup policy — Android 11 and lower | `android:allowBackup="false"` remains present. `android:fullBackupContent` now points to `ivai_full_backup_rules.xml`, which excludes all app-private storage domains. | Verified by resource test and lint |
| No-transfer policy — Android 12+ | `android:dataExtractionRules` points to `ivai_data_extraction_rules.xml`; both cloud backup and device-to-device transfer exclude every app-private storage domain. This closes the Android 12+ configuration ambiguity while preserving the existing local-only data policy. | Verified by resource test and lint |
| No hardcoded secret | Regex scan for Google/OpenAI-style key patterns and literal API-key assignments returned no result. | Verified |
| Transport and TLS trust | Scan for cleartext opt-ins, permissive trust managers, hostname-verifier bypasses, and trust-all code returned no result. | Verified |
| Prohibited execution/storage | Scan for process execution, accessibility automation, unrestricted storage, and background work returned no result. | Verified |
| Provider-neutral execution | Scan for implicit/default provider selection returned no result. | Verified |
| Global RTL direction | No global forced-LTR override exists in the shell or theme. | Verified |
| Narrow LTR exceptions | Four overrides are confined to message footer action/timestamp rows, `TerminalCodeBlock`, and `MarkdownCodeBlock`. | Verified as bounded |
| Launcher-art boundary | No product-UI reuse of launcher artwork was introduced. The adaptive-icon files and bitmap artwork remain launcher resources only. | Verified |
| Data/runtime isolation | The branch changes only manifest/resources, a regression test, and validation documentation; no Provider/Router/Agent/Data code, Room schema, permission, endpoint, or runtime contract changed. | Verified |

## Lint and Static Quality

`lintDebug` completes with **0 Error**, **0 Fatal**, and **16 Warning** findings in the current environment. The extra notice since the previous audit is a remote dependency-version availability result; it does not identify a source, manifest, resource, or runtime regression. The final clean validation run also built `assembleDebug` successfully. The redundant activity label and incomplete Android 12+ data-extraction configuration were eliminated. The adaptive icons were intentionally retained in `mipmap-anydpi-v26`: moving them into unqualified `mipmap-anydpi` created `IconXmlAndPng` resource collisions with their legacy WebP fallback files, so the resource move was reverted rather than changing launcher behavior.

| Remaining warning | Count | Disposition |
|---|---:|---|
| Gradle/AGP/AndroidX/Compose/Kotlin/Roborazzi newer-version notices | 13 | Deferred to a dedicated dependency-upgrade phase. An upgrade can change toolchain/runtime behavior and must not be combined with UX validation/hardening. |
| `ObsoleteSdkInt` for `mipmap-anydpi-v26` | 1 | Deferred. The qualifier preserves the current adaptive XML/legacy WebP fallback separation; a resource reorganization requires a launcher compatibility review, not a cosmetic move. |
| `MonochromeLauncherIcon` | 2 | Deferred P1 for a future launcher-asset increment. A compliant monochrome layer needs an approved distinct monochrome asset; the existing bitmap artwork must not be repurposed or introduced into product UI. |

## Findings Deferred to External Evidence

| Priority | Finding | Why not closed in sandbox | Required evidence |
|---|---|---|---|
| P0 release gate | Fresh install, upgrade, restart, rotation, and offline behavior on physical compact and medium devices. | A JVM/Robolectric environment cannot establish device lifecycle evidence. | Device matrix with build/OS/device details, non-sensitive outcomes, and reproduced defects. |
| P0 release gate | HTTPS loopback/private-LAN cancellation and timeout behavior on a real device. | The sandbox cannot act as the user’s target local network/device configuration. | Explicit HTTPS endpoint, timeout/cancel/offline outcomes, and confirmation that no HTTP/discovery/trust bypass occurred. |
| P0 release gate | Force-RTL, TalkBack, large-font-scale, and touch-target accessibility validation. | Compose semantics tests do not replace assistive-service use on a device. | Annotated non-sensitive screenshots and task checklist results for both device classes. |
| P1 Phase 7 completion | Actual card-sort, tree-test, moderated usability, and heuristic-review findings. | Participant evidence must be voluntary and cannot be fabricated. | De-identified study record in `PHASE7_UIUX_VALIDATION.md`; all P0/P1 findings fixed and retested. |
| P0 release gate | Owner-approved signed release APK, checksum, source tag, and reviewed release notes. | Release signing and publication require owner control. | Approved signed artifact, SHA-256, annotated tag, release notes, and owner decision. |

## Phase 7.5 Decision

The deterministic security and build baseline is suitable for **beginning** Phase 7.5 research and device validation. Phase 7 itself remains **in progress**: it cannot be declared complete, and no public Alpha release can be created, until the de-identified research record and all physical-device/release gates above are completed.
