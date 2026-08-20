# Global Engineering Review — 20 August 2026

**Scope:** This review examined the Android application, deterministic build/release tooling, Provider/credential boundaries, Agent target readiness, chat failure semantics, tests, lint, RTL policy, repository hygiene, and the protected CI baseline. It did not perform real-device, participant, accessibility, network, signing, tag, or publication actions.

> **Review outcome:** Two deterministic runtime defects and one release-tooling documentation drift were confirmed and remediated. The reviewed source passes the complete deterministic quality gate with **158 tests, 0 failures, 0 errors, 0 skipped, and 0 lint issues**. Alpha remains **not approved** because physical-device and owner-controlled release evidence is still absent.

## 1. Baseline and Method

The review started from protected `main` commit `1d08185248f252541eac545788cb8934b14dd805`, which includes the API 37.1 CI SDK provisioning and the Release Candidate `apksigner` path alignment. The review used source-level scan rules, direct contract/code inspection, focused regression tests, a clean debug/release/R8 build, the full unit suite, Android lint, Release Candidate verifier regression, and repository hygiene checks.

| Area | Evidence gathered | Result |
|---|---|---|
| Repository and CI | Clean worktree before remediation; protected `main`; latest post-PR CI had passed. | Pass baseline |
| Build/toolchain | JDK 21; `compileSdk 37.1`; `targetSdk 37`; minified release/R8 build. | Pass deterministic |
| Unit suite | Full `testDebugUnitTest` run after remediation. | 158 total; 0 failed/error/skipped |
| Lint | `lintDebug` HTML report parsed after full build. | 0 issues |
| Local-first/BYOK | Scan for hard-coded credentials, cleartext/trust bypass, prohibited execution, implicit provider selection, global forced-LTR. | No executable violation found |
| RC tooling | Shell syntax plus synthetic verifier/tamper regression. | Pass; baseline now enforced as 158 |

## 2. Confirmed Findings and Remediation

| ID | Severity | Confirmed behavior before remediation | Remediation | Regression evidence |
|---|---|---|---|---|
| GR-01 | P1 | `LocalProviderChatSession` emitted a normalized `ProviderStreamEvent.Failed`, then unconditionally required `Completed`; a valid provider failure became a generic `IllegalStateException`. | A `Failed` or `Cancelled` event is now a valid terminal event. A stream still fails closed if it ends without any terminal event. The existing policy remains: partial direct-provider assistant text is not persisted before completion. | `LocalProviderChatSessionTest` asserts that the exact normalized failure is visible and only the user message persists. |
| GR-02 | P1 | Agent direct-target persistence checked enabled/selectable records but did not reuse Router readiness rules for no-auth account marker and local-endpoint trust. | `validateAgentProfileTarget` now requires the same `isUsableRouterCandidate` policy used by Router candidates. | `LocalEndpointTrustRepositoryTest` simulates an invalid legacy/corrupt remote no-auth account and proves profile save is rejected. |
| GR-03 | P1 | Agent target options could remain visible for API-key accounts whose encrypted vault entry was absent/unreadable. | Agent target observation now combines non-sensitive vault status with provider/router state. API-key targets appear only when the stored credential is usable; explicitly trusted local no-auth targets remain supported. | `WorkspaceAgentReadinessTest` proves an Agent target is hidden before credential storage, appears after storage, and disappears after clear. |
| GR-04 | P1 | The active RC helper/verifier baseline remained 155 after three new regression tests raised the suite to 158. | `prepare_release_candidate.sh`, package verifier, synthetic verifier fixture, active release checklist, RC preparation guide, roadmap current baseline, and P0 monitor active build row now use 158. Historical 155 snapshots are explicitly retained as historical evidence. | Shell syntax, stale-active-tooling scan, and verifier/tamper regression pass. |

## 3. Architecture and Security Review

The reviewed implementation remains a **Local-first, Backendless, BYOK** harness. Room retains local workspace and provider metadata, while credential persistence uses encrypted envelopes and Android Keystore-backed per-reference keys. Provider adapters receive an opaque credential reference; a network gate resolves plaintext transiently only at foreground request construction. There is no central backend, cloud synchronization default, auto-selected provider, hidden provider discovery, cleartext local transport, background worker, or secret committed to source.

Provider setup remains user-controlled. Cloud presets are catalog metadata only; they do not save a connection, choose a model, run discovery, or issue a request. User-managed OpenAI-compatible remote and local endpoints remain HTTPS-only, with explicit trust modes and no `.local`, mDNS, scan, HTTP opt-in, userinfo, query, or fragment bypass. The Gemini adapter remains a proof adapter rather than a universal default.

## 4. Deterministic Validation

```text
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

The command completed successfully after the remediation. R8 produced the unsigned release output and mapping material. The full suite produced 158 tests with no failures, errors, or skips. The debug lint report contained zero issues. Android's build output reported that two prebuilt native dependency libraries could not be stripped and were packaged as supplied; this was informational, did not fail lint/R8/build, and did not introduce app-native code in this review.

The following invariants also passed after the build:

| Invariant | Result |
|---|---|
| Hard-coded credential pattern absent | Pass |
| Cleartext transport/trust-bypass pattern absent | Pass |
| Prohibited execution primitives absent | Pass |
| Implicit provider selection pattern absent | Pass |
| Global forced-LTR shell override absent | Pass |
| Tracked keystore, certificate, local-properties, or `.env` material absent | Pass |
| RC verifier detects checksum tampering | Pass |

## 5. Remaining Release Blockers

The remaining Alpha gates cannot truthfully be closed by sandbox or CI. They have not been converted into Pass states.

| Gate | Required evidence | Current state |
|---|---|---|
| Usability and heuristic review | Voluntary de-identified card-sort/tree-test/safety-comprehension outcomes and independent review reconciliation. | Pending |
| Physical device matrix | Compact and medium devices; fresh install, upgrade, restart, rotation, offline, light/dark, font scale, and OEM launcher observations. | Pending |
| Accessibility/RTL | Force-RTL, mixed-language, TalkBack and semantics observations on a real device. | Pending |
| Local HTTPS network matrix | Explicit loopback/private-LAN HTTPS cancellation, timeout and offline evidence on a real device. | Pending |
| Owner-controlled signing | Existing owner keystore continuity, signed-artifact verification, SHA-256, and retained provenance. | Pending |
| Publication approval | Annotated tag, reviewed notes, owner approval, prerelease creation and independent download/hash check. | Pending |

## 6. Release Boundary

No signing key, certificate, API credential, release tag, uploaded binary, public download, network probe, backend, telemetry, or automatic provider selection was created by this review. The deterministic quality result is not a substitute for field/device evidence and is not approval to publish an Alpha release.

## References

[1]: `docs/RELEASE_READINESS_CHECKLIST.md`
[2]: `docs/RELEASE_CANDIDATE_PREPARATION.md`
[3]: `docs/PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md`
[4]: `docs/PHASE7_5_FIELD_KIT.md`
[5]: `docs/ALPHA_RELEASE.md`
