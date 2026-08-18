# Pre-Alpha Engineering Backlog and ER-24 Handoff

> **Status:** Prepared from clean `main` commit `e3d3bad` after the post-ER-01 global review, then updated through ER-24. ER-01 through ER-05, ER-15, ER-22, and ER-25 are merged; ER-24 passed focused/full deterministic validation locally, with protected merge pending. This document does not close Phase 7.5, authorize an Alpha release, or add any runtime capability.

## Current Decision

The protected `main` baseline at `2265ff4` remains synchronized and green. The ER-24 implementation branch has 139 passing tests and zero lint issues after clean validation. Subject to protected merge, the next engineering increment is **ER-14 — Release-Minification Audit**. ER-24 remains intentionally separate from Phase 7.5 research, release minification implementation, Providers, MCP, and Alpha publication.

| Decision | Status |
|---|---|
| Next implementation increment after ER-24 merge | ER-14 — release-minification audit |
| Approved `main` baseline before ER-24 | `2265ff4` (PR #58) |
| ER-24 branch deterministic quality baseline | 139 tests; 0 failures, errors, skipped tests, and lint issues |
| Phase 7.5 participant/device evidence | Deferred, not complete |
| Public Alpha / signed APK / download | Not approved |
| MCP and Skills runtime | Planned post-Alpha only; no implementation is authorized by this backlog |

## ER-02 — Bounded Local Calculator Contract

### Goal

Make the existing `CALCULATE` Agent tool truthful: a valid arithmetic request returns a bounded local result, while invalid, ambiguous, excessive, and mathematically unsafe input is rejected with a safe reason. The calculation remains in-process, deterministic, local-only, and read-only. It never evaluates code, runs a command, contacts a network endpoint, reads a file, changes a target, or expands Agent authority.

### Proposed Contract

| Contract dimension | Proposed rule |
|---|---|
| Accepted grammar | Decimal numbers, parentheses, unary `+`/`-`, binary `+`, `-`, `*`, and `/` only. |
| Explicit exclusions | No variables, functions, exponent notation, scientific notation, implicit multiplication, unit conversion, locale-dependent separators, assignments, strings, commas, code, file paths, URLs, or provider calls. |
| Numeric model | Standard-library `BigDecimal` with a fixed documented math context; output is finite, normalized, and rendered without a locale-dependent format. |
| Bounds | Maximum 256 input characters, 64 parse nesting levels, 128 numeric/operator tokens, and deterministic work proportional to the bounded input. |
| Failure model | Blank/invalid syntax, division by zero, out-of-bound expression, or non-finite/unsupported result returns `AgentToolResult.Rejected` with an actionable safe message. |
| Trace privacy | The existing trace receives the safe summary only. No input expression, model reasoning, credential, or external data is persisted in the run trace. |
| Authority | Calculation remains a local `Completed` result and never needs approval because it has no side effect. |

### ER-02 Acceptance Tests

The implementation adds a parser/evaluator test suite alongside production code. The suite proves both ordinary behavior and rejection boundaries.

| Test category | Required cases |
|---|---|
| Basic arithmetic | Addition, subtraction, multiplication, division, operator precedence, and nested parentheses. |
| Decimal behavior | Exact expected normalized output for representative finite decimals under the documented math context. |
| Unary behavior | Unary plus/minus at expression start and inside parentheses. |
| Invalid syntax | Empty input, dangling operator, unmatched parentheses, unsupported character, variable/function text, comma, exponent notation, and implicit multiplication. |
| Safety boundaries | Division by zero, expression length limit, nesting limit, token limit, and parser error without stack overflow. |
| Isolation | Evaluating a calculator expression cannot call a Provider, mutate an Agent/Profile/Project, access a workspace file, create a network request, or persist raw expression content. |
| Regression | Existing `calculator rejects non arithmetic input` continues to pass. |

### ER-02 Explicit Non-Goals

This increment must not add an external math engine, JavaScript/ScriptEngine evaluation, dynamic class loading, shell/process execution, network access, function plugins, variables, chained memory, UI redesign, Provider/Router behavior, MCP, new permissions, Room migration, credential handling, background work, analytics, or a default Agent action. Any future unit conversion, advanced mathematics, or tool-extension design is a new named phase with its own threat model.

### ER-02 Exit Gate

ER-02 may be declared complete only when the implementation and focused tests pass locally, the full clean build/unit/lint gate passes, source security/architecture scans are clean, the PR scope is limited to the calculator contract, both protected CI jobs pass, `main` protection is restored after merge, and Roadmap/triage are updated with the actual final behavior.

## Ordered Backlog After ER-24

| Order | Increment | Current evidence and required gate | Why it must remain separate |
|---|---|---|---|
| 1 | ER-02 — calculator contract | Closed in PR #49 with bounded local evaluation and regression coverage. | Changes Agent tool behavior while retaining a precise local-only contract. |
| 2 | ER-03 — approval concurrency | Closed in PR #50 with deterministic approval-lifecycle serialization. | Synchronization is limited to approval lifecycle state so one-time write authority cannot be overwritten by a stale resolution. |
| 3 | ER-04 — partial stream recovery | Closed in PR #51; Room v6 adds an explicit durable incomplete marker with Router, migration/reopen, and UI semantics regressions. | Preserves visible partial text after failure/restart without retrying a provider after content becomes visible. |
| 4 | ER-05 — exception boundary audit | Closed in PR #53; Router, Gemini, and OpenAI-compatible recover only `Exception`, while fatal `Error` propagates. | Exception/cancellation behavior is safety-sensitive and must not become a batch refactor. |
| 5 | ER-15 — safe Router registry lookup | Closed in PR #55; stale connection/account/model state now yields a safe `INVALID_REQUEST` failed trace. | Registry inconsistency must not crash UI or hide a provider-selection error. |
| 6 | ER-22 / ER-25 — archive malformed-input atomicity | Closed in PR #57: checksum-valid malformed collection is rejected before Room/file mutation. Allocation policy is unchanged. | Archive safety requires focused corruption evidence, not speculative OOM refactoring. |
| 7 | ER-24 — vault desynchronization recovery | Deterministically reproduced and locally fixed; only decryptable non-blank credentials are reported usable, while stale envelopes remain for deliberate overwrite/clear; protected merge pending. | Vault recovery must preserve BYOK boundary and never expose or fabricate credentials. |
| 8 | ER-14 — release-minification audit | Next candidate: inspect release variant/R8 configuration and run a release validation matrix before any minification toggle. | Minification is a release-hardening decision, not a safe one-line change. |
| 9 | Remaining P3 hardening | SSE protocol and credential naming tests remain separate increments. | These are distinct behavior/documentation decisions, not a batch refactor. |
| 10 | Phase 7.5 field evidence | Voluntary de-identified usability/heuristic sessions plus compact/medium device evidence. | Sandbox results cannot replace participants, TalkBack, lifecycle, or local HTTPS behavior. |
| 11 | Alpha release decision | Owner signing, signed artifact SHA-256, tag, notes, GitHub Release, independent hash check. | Publication must follow—not substitute for—all P0/P1 gates. |
| 12 | Phase 8.0 Skills/MCP prototype | Threat-model/UX prototype after Alpha, following existing Phase 8 architecture documents. | No MCP runtime, process execution, OAuth, or background capability before its own gates. |

## Gates That Remain Intentionally Open

| Gate | Required real evidence | Current state |
|---|---|---|
| Usability and heuristic review | De-identified participant study and independent reviewer reconciliation; no unresolved P0/P1 finding. | Deferred |
| Device and accessibility matrix | Compact/medium device results for install/upgrade/restart/rotation/offline, Force-RTL, TalkBack, font scale, and launcher behavior. | Deferred |
| HTTPS local endpoint behavior | User-configured HTTPS loopback/private-LAN cancellation, timeout and offline outcomes without HTTP/discovery/scan/trust bypass. | Deferred |
| Release provenance and signing | Owner-controlled signed APK, SHA-256, annotated tag, reviewed notes, owner approval, and independent download/hash verification. | Pending / not approved |

## Repository Preparation Checklist

Before opening the next focused branch, start from a clean synchronized `main`, retain the existing stable protection settings, use a focused `fix/` or `chore/` branch, keep the PR body explicit about unchanged security boundaries, run the clean quality gate, wait for both CI jobs, squash merge with protection restoration, and update the Roadmap, triage, and this backlog only with evidence from the finished increment.

## References

- [External Review Triage](EXTERNAL_REVIEW_TRIAGE_2026-08-17.md)
- [Global Review — Post ER-01](GLOBAL_REVIEW_2026-08-17_POST_ER01.md)
- [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
- [Phase 7.5 UX Validation and Hardening Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)
- [Skills and MCP Future Architecture](SKILLS_MCP_FUTURE_ARCHITECTURE.md)
