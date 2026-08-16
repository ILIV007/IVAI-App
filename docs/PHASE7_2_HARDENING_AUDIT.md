# Phase 7.2 Hardening Readiness Audit

> **Scope:** Independent review of protected `main` through Phase 7.2, conducted before Phase 7.3. The review validates Roadmap alignment, Local-first/Backendless/BYOK invariants, security, RTL, contrast, quality gates, visual evidence, and known Alpha-only deferrals.

## Verified alignment

| Review area | Evidence | Status |
|---|---|---|
| Roadmap and phase boundaries | `main` contains Phase 7.0 foundation, Phase 7.1 navigation/Chat, corrective Phase 7.1 screenshot evidence, and Phase 7.2 Provider/Combo UX as separate merged PRs. The corrective review changes only Compose presentation, theme semantics, UI tests and audit evidence. | Verified |
| Local-first and Backendless | Source scans found no central backend, cloud sync, mandatory login, analytics SDK, remote error logging, background worker, scheduler or job implementation. The manifest retains only `INTERNET`. | Verified |
| BYOK and provider neutrality | No hardcoded secret, default provider/model/endpoint/Combo, automatic selection, credential reveal or implicit candidate insertion was found. Phase 7.2 preserves user-managed connection, account, model and ordered Combo selection. | Verified |
| Endpoint trust policy | No HTTP/cleartext opt-in, trust-all manager, hostname bypass, mDNS, `.local` discovery or network scan was found. Existing Remote HTTPS, local-device HTTPS and private-LAN HTTPS policies remain unchanged. | Verified |
| Restricted execution | No `Runtime.exec`, `ProcessBuilder`, accessibility automation, external-storage override, WorkManager, JobScheduler or AlarmManager was found. | Verified |
| Data/runtime boundary | The review correction does not change Provider adapters, endpoint validation, Vault behavior, Router persistence/execution, Agent runtime, Room schema/migrations, permissions or network operations. | Verified |
| RTL direction | Main shell and theme have no global LTR override. The AI response header no longer forces LTR. Remaining narrow overrides are limited to user/assistant footers, `TerminalCodeBlock`, and Markdown code rendering. `RtlBidiCorpusTest` passed. | Verified |
| Launcher-only artwork boundary | Production and test source scans show no launcher artwork reference in product UI. The accessible `IVAI` wordmark remains the in-app identity. | Verified |
| Branch governance | `main` is synchronized and protected with one required approval, linear history, admin enforcement, Secret Scan, and Build/unit-test/lint gates. | Verified |

## Quality and visual evidence

| Gate | Result |
|---|---|
| Clean build | `./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain` passed after the correction. |
| Unit suite | **103 tests** passed with **0 failures, 0 errors and 0 skipped**. The suite was also re-run independently before the corrective change to eliminate stale-cache dependence. |
| Lint | **0 Error/Fatal** findings. The remaining **19 Warnings** are pre-existing informational/version-maintenance findings. |
| Whitespace and policy scans | `git diff --check` plus secret, transport/trust-bypass, prohibited-execution, implicit-selection and global-LTR scans passed. |
| Contrast audit | All reviewed semantic normal-text pairs meet AA. The corrected dark `onSecondaryContainer` pair is **12.77:1** and dark `onError` pair is **5.59:1**. |
| Chat visual review | Compact streaming evidence retains selected target, state banner, readable Persian/Arabic/English mixed content, disabled composer and a visibly labeled **Stop** button with `Stop streaming` semantics. |
| Connections/Combo visual review | Evidence shows local readiness progression, explicit ordered candidates, final review, no implicit provider insertion copy, and no credential value. |

## Corrective findings resolved before Phase 7.3

| Priority | Finding | Resolution | Regression evidence |
|---|---|---|---|
| P1 | Dark `onSecondaryContainer` was 3.96:1 against `secondaryContainer`, below normal-text AA. | Uses `TextPrimary` for the dark semantic foreground. | `IvaiThemeContrastTest`; 12.77:1 audit result. |
| P1 | Dark `onError` was 3.02:1 against `error`, below normal-text AA. | Uses `IvaiBackground` for the dark semantic foreground. | `IvaiThemeContrastTest`; 5.59:1 audit result. |
| P2 | Compact streaming screenshot made the Stop affordance visually ambiguous. | Replaced the fixed circular streaming control with a 76dp text Stop button; cancellation callback and content description remain unchanged. | `ChatFoundationTest` record-mode capture and semantics assertions. |
| P2 | AI response metadata header forced LTR outside the approved code/footer exceptions. | Removed the local LTR wrapper; natural layout direction now applies to the header. | `RtlBidiCorpusTest` and source audit. |

## Deferred to Alpha release hardening

| Priority | Deferred evidence | Why it is deferred | Required gate |
|---|---|---|---|
| P0 for Alpha release | Fresh install, upgrade, restart, rotation and offline behavior on physical compact/medium devices. | Sandbox validation cannot substitute for a real Android device matrix. | Execute and record the Phase 6 physical-device matrix before any GitHub Alpha release. |
| P0 for Alpha release | Local-device/private-LAN HTTPS cancellation and timeout evidence on physical hardware. | Requires real endpoint and device networking conditions. | Run the documented network matrix without introducing discovery or HTTP. |
| P0 for Alpha release | Force-RTL, TalkBack, font-scale and touch exploration evidence. | Compose/Robolectric semantics help but do not replace real accessibility tooling. | Capture device evidence for task-critical controls. |
| P0 for Alpha release | Signed artifact, checksum, release notes and annotated tag. | Requires explicit owner approval and signing material. | Follow `docs/ALPHA_RELEASE.md` during Phase 6 release work. |
| P2 | 19 lint warnings. | They are non-fatal pre-existing version/informational findings and are outside the narrow UI/accessibility correction. | Resolve in a dedicated dependency/manifest maintenance PR after compatibility review. |

## Readiness for Phase 7.3

Phase 7.3 may start as a separate UI-only branch. It may redesign Agent profile cards, progressive profile editing, local run timeline/trace/cancellation, one-time file-write approval review and recovery presentation. It must not add Agent tools, autonomous/background execution, always-allow permission, changed runtime limits, provider defaults, new endpoint behavior, or any data/runtime contract change.
