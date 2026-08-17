# Global Project Review — 17 August 2026

> **Review baseline:** `62beef3` on protected `main`, followed by the focused corrections in the review branch that accompanies this report.
>
> **Decision:** No P0/P1 architecture, security, or Provider-neutrality drift was found in the deterministic review. IVAI remains a **Local-first, Backendless, BYOK Agent Harness**. Phase 7.5 and the public Alpha release remain **in progress**, because voluntary research, physical-device evidence, and owner-controlled release evidence cannot be fabricated in the sandbox.

## Review Scope and Evidence

The review covered the roadmap, provider-harness boundary, Android manifest, runtime surface, test suite, lint output, UI token usage, release documentation, GitHub state, and the complete deterministic validation gate. The verified command was:

```text
./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
```

| Gate | Result |
|---|---|
| Debug APK | Built successfully at `app/build/outputs/apk/debug/app-debug.apk` |
| Unit and UI regression suite | **123 tests**, 0 failures, 0 errors, 0 skipped |
| Lint | 0 Error, 0 Fatal, 16 Warning |
| Whitespace | Clean |
| Repository state at review start | `main` synchronized with GitHub; no open pull request |
| Recent main workflow | Android quality push workflow for `62beef3` completed successfully |

## Architecture Alignment

| Invariant | Review evidence | Result |
|---|---|---|
| Local-first | Room/DataStore/app-private workspace remain the data boundary. `allowBackup=false`, legacy full-backup exclusions, and Android 12+ cloud/D2D extraction exclusions are present. | Aligned |
| Backendless | No central API, proxy, mandatory account, telemetry, session replay, cloud SDK, or analytics SDK reference was found in `app/src/main`. | Aligned |
| BYOK | Provider connections, accounts, endpoints, models, credential references, Direct Models and ordered Combos remain user-managed; no literal key was found. | Aligned |
| Explicit targeting | Scan found no default provider, implicit provider, or automatic provider-selection path. | Aligned |
| Network restraint | No literal cleartext HTTP endpoint, `.local` discovery literal, mDNS/scanning API, or permissive TLS/trust bypass was found. The manifest requests only `INTERNET`. | Aligned |
| Bounded execution | No process execution, accessibility automation, scheduler, alarm, or background-work API was found. | Aligned |
| RTL | No global forced-LTR exists in the shell or theme. The four LTR overrides remain confined to message footer rows and terminal/Markdown code blocks. | Aligned |
| Launcher-art boundary | The VA image remains confined to launcher resources. No product UI reuse was introduced. | Aligned |

The current provider contract remains consistent with [Provider Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md): Gemini is an installed proof adapter, not a default provider, backend, required account, or implicit target. The only production UI Gemini reference after this review is the provider-family display label; it represents a user-selected supported adapter and is not a selection policy.

## Findings and Corrective Actions

| ID | Priority | Finding | Resolution in this review branch |
|---|---|---|---|
| GR-01 | P2 | `MockDataRepository` was an unused production-source fixture containing synthetic Gemini-heavy chats, Combo data, latency, and Agent examples. It had no runtime consumer other than a single RTL test. | Removed it from `app/src/main`; the eight-message RTL/BiDi fixture now lives only in `RtlBidiCorpusTest`. Runtime behavior and test coverage are preserved. |
| GR-02 | P2 | Chat/Markdown terminal chrome and the user bubble contained direct raw hex colors despite the Phase 7 semantic-token design rule. | Replaced component-level raw hex with IVAI semantic/Material tokens, including theme-aware terminal-control tokens and the approved accent gradient. |
| GR-03 | P2 | Phase 7.5 documents still named the now-deleted feature branch, and the README did not show deterministic Phase 7.5 hardening. | Updated the audit/protocol to reference merged [PR #37](https://github.com/ILIV007/IVAI-App/pull/37), added PR traceability to the roadmap, and synchronized README hardening status. |

> These corrections are constrained to UI fixture placement, UI visual-token consumption, tests, and documentation. They do not alter Provider/Router/Agent/Data runtime behavior, Room schema, credential handling, project isolation, endpoint policy, permission surface, or release semantics.

## Lint and Technical Debt

The 16 remaining warnings are known and non-blocking for the deterministic gate. The count rose by one during this review because lint refreshed a remote dependency-version availability notice; no source, manifest, resource, or runtime finding was added. They are intentionally not mixed into a UI/review correction.

| Warning family | Count | Planned handling |
|---|---:|---|
| Gradle/AGP/AndroidX/Compose/Kotlin/Roborazzi newer-version notices | 13 | Separate dependency-upgrade phase with compatibility regression, not part of Phase 7.5 research. |
| `ObsoleteSdkInt` for `mipmap-anydpi-v26` | 1 | Keep the qualifier because moving adaptive XML into unqualified resources creates a collision with legacy WebP fallbacks; revisit only in a launcher-resource compatibility task. |
| `MonochromeLauncherIcon` | 2 | Requires an approved distinct monochrome launcher asset. The existing VA artwork must not be repurposed as a general UI mark. |

## Roadmap Conformance

Phases 0 through 5 are implemented and protected by regression coverage. The Phase 7 redesign is complete through 7.4; 7.5 has its deterministic protocol/hardening component merged in [PR #37](https://github.com/ILIV007/IVAI-App/pull/37), but its evidence gate has not been claimed as passed. This is a **documented dependency**, not an architecture deviation.

The Phase 6 Alpha delivery gate is also intentionally incomplete. Its physical-device, local-HTTPS cancellation/timeout, Force-RTL/TalkBack, signing, checksum, annotated-tag, and reviewed-release-note requirements remain valid in [ALPHA_RELEASE.md](ALPHA_RELEASE.md). The order of evidence work now follows the Phase 7 UI redesign: validate the final UI with users/devices, then assemble the owner-approved Alpha artifact.

## Remaining Phases and Recommended Sequence

| Order | Named work | Completion condition | Dependency |
|---:|---|---|---|
| 1 | **Phase 7.5 — External UX validation and device hardening** | De-identified card-sort, tree-test, moderated safety-comprehension, heuristic-review, compact/medium device, Force-RTL, TalkBack, font-scale, lifecycle/offline, and allowed local-HTTPS evidence is recorded; all P0/P1 findings are fixed and retested. | 5–8 voluntary representative participants and physical Android devices. |
| 2 | **Phase 6 — Alpha release evidence and packaging** | Owner approves a signed reproducible APK, SHA-256, release notes, annotated tag, known limitations, and GitHub release after all device/UX gates are green. | Owner-controlled signing material and release decision. |
| 3 | **Post-Alpha hardening/maintenance increments** | Separate accepted scope and threat model. Candidate increments are dependency upgrades and an approved monochrome launcher asset. | Must remain isolated from provider/runtime changes. |
| 4 | **Deferred capability phases** | Separate roadmap decision for each: local inference, MCP, voice, Termux/Shell, Shizuku, Accessibility automation, smart routing, multi-device sync, or Play distribution. | Independent threat model, permissions review, architecture design, and acceptance gates. |

Until the first two rows are completed with actual evidence, the correct product status is **Alpha preparation / evidence pending**, not public release readiness.

## References

- [Execution Roadmap](ROADMAP.md)
- [Provider Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md)
- [Phase 7 UI/UX Execution Plan](PHASE7_UIUX_EXECUTION_PLAN.md)
- [Phase 7.5 Hardening Audit](PHASE7_5_HARDENING_AUDIT.md)
- [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md)
- [Alpha Release Checklist](ALPHA_RELEASE.md)
