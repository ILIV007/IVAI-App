# Terminal Stream Hardening Audit — 21 August 2026

> **Scope:** This decision record covers a deterministic provider-stream protocol remediation only. It records no network request, device result, participant result, signing action, or public-release approval.

## Confirmed Defect

`LocalProviderChatSession` and `RouterChatSession` accepted events until the upstream flow completed naturally. A malformed adapter could therefore emit a `Delta` after a terminal `Completed` or `Failed` event. In direct chat this could expose an event after a terminal outcome; in router chat it could introduce visible partial content after a candidate failure and alter persistence/fallback handling. The stream contract already defined `Completed`, `Failed`, and `Cancelled` as terminal outcomes, but consumers did not enforce that contract by stopping collection.[1]

## Remediation

A shared internal terminal-event predicate and stackless control signal now stop foreground collection immediately after the terminal event is handled. Direct chat forwards the terminal event once and prevents later adapter emissions. Router chat treats the handled terminal event as authoritative before deciding success, failure, or safe fallback. Cancellation remains a coroutine cancellation path and is not converted into a generic failure.[2] [3]

| Check | Deterministic result | Scope boundary |
|---|---|---|
| Direct provider failure followed by a delta | Regression proves the post-failure delta is not collected or persisted. | Does not exercise a real endpoint. |
| Router provider failure followed by a delta | Regression proves no later delta or incomplete assistant message is persisted and the attempt finishes Failed. | Does not exercise physical-device UX. |
| Existing partial-stream policy | A delta emitted **before** a failure remains intentionally visible, persisted as incomplete, and never falls back. | Product behavior is unchanged for a valid ordering of events. |
| Build quality | Debug/release-R8 build, full unit suite, and lint passed with **160 tests, 0 failures, 0 errors, and 0 skipped**. | Does not close field/device/accessibility/network gates. |
| Release candidate contract | Active helper, verifier, synthetic fixture, and active release docs now enforce/report the 160-test baseline. | Candidate remains unsigned and non-public. |

## Deferred Gates

The remediation does not add a Provider runtime test, automatic retry, background work, telemetry, backend, credential, signing material, or publication. Real HTTPS cancellation/timeout, compact/medium device behavior, Force-RTL, TalkBack, usability/heuristic evidence, signing, and owner approval remain **Pending** under the Phase 7.5 and Alpha release gates.[4] [5]

## References

[1]: ../app/src/main/java/dev/iliv007/ivai/provider/ProviderContract.kt "Provider stream contract"
[2]: ../app/src/main/java/dev/iliv007/ivai/chat/LocalProviderChatSession.kt "Direct-provider chat consumer"
[3]: ../app/src/main/java/dev/iliv007/ivai/router/RouterChatSession.kt "Router chat consumer"
[4]: PHASE7_5_UX_VALIDATION_PROTOCOL.md "Phase 7.5 validation protocol"
[5]: RELEASE_READINESS_CHECKLIST.md "Release Readiness Checklist"
