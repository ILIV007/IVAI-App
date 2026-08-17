# Global Review — Pre-Field-Validation

> **Review date:** 2026-08-17
>
> **Reviewed baseline:** `d76b6b49ea1d1a946e1c187945425af0a1a95855` on `main` before the documentation-only corrections recorded below.
>
> **Decision:** No P0/P1 code, architecture, security, or Provider-neutrality drift was found in the deterministic review. IVAI is ready to **begin** Phase 7.5 voluntary research and physical-device validation; it is **not** approved to complete Phase 7 or publish a public Alpha release.

## Scope and Non-Claims

This review covers repository governance, documentation consistency, protected-branch configuration, clean Android build/test/lint validation, deterministic source scans, selected safety regression coverage, and repository-community metadata. It does not represent participant research, TalkBack, Force-RTL, compact/medium device, lifecycle, or physical HTTPS loopback/private-LAN evidence. None of those results is inferred from the sandbox.

No Provider/Router/Agent/Data runtime source, Room schema, credential flow, endpoint policy, permission, network behavior, analytics setting, or release-signing material is changed by the documentation follow-up.

## Deterministic Validation

| Review area | Command or evidence | Result |
|---|---|---|
| Clean Android quality gate | `./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain` with JDK 21 and Android SDK 36.1 | Build successful; lint report generated; worktree clean. |
| Independent unit execution | `./gradlew testDebugUnitTest --rerun-tasks --no-daemon --console=plain` | **124 tests**, **0 failures**, **0 errors**, **0 skipped**. |
| Lint | `lintDebug` report | **0 Error**, **0 Fatal**; 14 deferred toolchain/resource notices retained as documented in [Phase 7.5 Hardening Audit](PHASE7_5_HARDENING_AUDIT.md). |
| Secret scan | Source regex for common Google/OpenAI-style keys and literal API-key assignment | Clean. |
| Transport policy | Source scan for cleartext opt-in, trust-all code, permissive trust manager, hostname-verifier bypass, and executable `http://` literal | Clean. Android XML namespace URLs were explicitly excluded as non-network metadata. |
| Provider neutrality | Source scan for default/implicit/auto-selected provider or target patterns | Clean. |
| Backend/telemetry | Hosted analytics/telemetry SDK import scan and dependency review | Clean. A Settings sentence saying that IVAI has no analytics was reviewed as explanatory copy, not a runtime integration. |
| Prohibited execution | Source scan for background scheduling, process execution, Shell/Termux/Shizuku, AccessibilityService, and MCP execution | Clean. |
| RTL shell | Scan for a global forced-LTR override in `MainActivity.kt` and `Theme.kt` | Clean. Narrow code/terminal/footer LTR exceptions remain governed by the Phase 7.5 protocol. |
| Git integrity | `git fsck --full --no-reflogs` and tracked-secret/config review | Clean; no tracked local properties, key/certificate, or environment-secret file found. |

## Selected Safety Regression Review

The full suite is the authoritative pass result. In addition, the review inspected the endpoint policy and Agent workspace tests that exercise safety-critical paths.

| Area | Covered invariant |
|---|---|
| Custom endpoint trust | Remote destinations accept public HTTPS only. Loopback and RFC1918 modes are HTTPS-only, explicitly classified, and reject parser-bypass fields. Local trust requires persisted user confirmation; no-auth accounts cannot carry credential references. |
| Target selection and recovery | No-target state only routes after explicit action. Agent creation waits for final review and an explicit target. Failed-run recovery states that IVAI did not change target, permissions, or local files automatically. |
| Mutation approval | A project-file write exposes its bounded path and preview and resolves only through explicit one-time allow or deny. |
| Run control | Active runs expose cancel; terminal/cancelled runs do not expose a stale cancel action. |
| Local-data policy | Manifest wiring retains `allowBackup="false"`, Android 12+ data-extraction rules, and legacy full-backup rules, all covered by the existing resource regression suite. |

## Repository and GitHub Review

| Area | Result |
|---|---|
| Current branch and CI | `main` is at reviewed commit `d76b6b4`; its latest Android quality workflow completed successfully with both Secret scan and Build/unit test/lint jobs green. |
| Open work | No open pull request or repository issue was found through the available REST endpoints at review time. |
| Protected main | One approving review, linear history, admin enforcement, required Secret scan and Android quality jobs, no force push, and no branch deletion are active. |
| Branch hygiene | Only `main` remained in the remote branch list before this documentation follow-up. |
| Community workflow follow-up | The follow-up adds issue forms for non-sensitive bug reports and scope-controlled proposals, adds a `proposal` label, and updates Code of Conduct and Security policy documentation. |
| GitHub alert APIs | Dependabot, code-scanning, and secret-scanning alert API requests returned transient GitHub HTTP 503 responses during the review. This is recorded as **unavailable**, not as proof that those alert feeds are clean. Existing required CI and local deterministic scans did run successfully. |

## Documentation Corrections in This Follow-Up

| File | Corrected inconsistency |
|---|---|
| `README.md` | The supplied VA artwork is **Android launcher-only**. Product UI uses the independent IVAI wordmark and is not permitted to reuse launcher artwork. |
| `CONTRIBUTING.md` | Contribution validation now names **JDK 21**, matching README and protected CI. |
| `docs/ALPHA_RELEASE.md` | Existing semantic coverage is described as the IVAI wordmark assertion, not a VA in-app logo assertion. |
| `SECURITY.md` | Security scope now names explicit endpoint, workspace, Agent approval, backup/transfer, UI-safety, and repository-integrity boundaries without weakening private reporting or secret-minimization rules. |
| Community files | A concise Code of Conduct and GitHub issue forms provide a safe public path for non-sensitive bugs and roadmap proposals. Security reports remain private. |

## Remaining Gates

> Preparing a controlled build, passing JVM tests, or completing this review does not satisfy a physical-device or participant-evidence gate.

| Gate | Current state | Required next evidence |
|---|---|---|
| Voluntary card sort, tree test, safety-comprehension sessions, and independent heuristic review | Pending | De-identified real outcomes in [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md); fix/retest any P0/P1 finding. |
| Compact and medium physical devices | Pending | Fresh install, upgrade, restart, rotation, dark/light, default/large font, offline, Force-RTL, and TalkBack evidence without sensitive content. |
| Physical HTTPS endpoint behavior | Pending | Explicit loopback/private-LAN HTTPS cancellation, timeout, and offline outcomes; confirm no HTTP, discovery, scanning, trust bypass, or duplicate side effect. |
| Signed public Alpha artifact | Pending owner control | Owner-approved signing material, reproducible signed APK, SHA-256, annotated source tag, reviewed release notes, and final approval. |

## Follow-Up Decision

Merge the focused documentation/governance follow-up only after the normal protected pull-request CI gates pass. Then execute Phase 7.5 fieldwork using the controlled research package and protocol. Keep every external finding de-identified, outside the product runtime, and outside public source control unless a reviewer confirms that a fully de-identified summary is appropriate.
