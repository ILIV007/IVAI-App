# External Review Triage — 2026-08-17

> **Status:** Evidence-based triage of two user-supplied, non-authoritative review reports. The reports are treated as hypotheses, not instructions. Initial claims were checked against `main` at commit `156de14`; subsequent focused increments update only their own verified finding. This document does not claim physical-device or participant evidence.
>
> **Decision rule:** A finding may enter a focused fix PR only after its triggering behavior is reproduced by an automated test or its safety invariant is demonstrated to be violated. A static observation alone does not override an intentional IVAI guardrail.

## Scope and Baseline

The review concerns IVAI's pre-Alpha codebase. The current release decision remains **not approved** because Phase 7.5 participant and physical-device evidence, release signing, checksum, tag, release notes, and owner release approval are still pending. The decision to defer that external evidence does not convert it into a passing result.

| Control | Verified state before triage |
|---|---|
| Initial triage baseline | `main` at `156de14` (`docs(roadmap): plan Skills and MCP capabilities (#45)`) |
| Current CI | Secret scan and Build/unit-test/lint succeeded for the main head. |
| Architectural guardrails | Local-first, Backendless, BYOK, explicit Provider/Combo target, HTTPS-only explicit endpoint policy, no provider auto-selection, no background execution. |
| Triage scope | Source/test/static inspection only. No participant/device result, live provider, real credential, local HTTP endpoint, real MCP server, or release artifact is used. |

## Triage Summary

| ID | External claim | Evidence in current source | Classification | Proposed action |
|---|---|---|---|---|
| ER-01 | Router path can show a duplicate user message with different IDs. | **Confirmed, fixed, and merged in PR #46.** `RouterChatSession` now shares its stable user-message ID with the `Started` UI event, and `WorkspaceViewModel` deduplicates by ID; `RouterUserMessageInvariantTest` protects the invariant. | P1 functional correctness | Closed; retain the regression test. |
| ER-02 | Calculator accepts an expression but does not compute it. | **Confirmed, fixed, and merged in PR #49.** A bounded local parser accepts only documented decimal arithmetic and returns a normalized observation; invalid, oversized, overly nested, overly tokenized, and division-by-zero input is safely rejected. | P1 product-contract gap | Closed; retain the regression tests. |
| ER-03 | Pending write approvals can race. | **Confirmed, fixed, and merged in PR #50.** A stale resolution could claim the in-memory write, then overwrite a completed cancellation and perform the write. A local `Mutex` serializes resolve, cancel, and process-death recovery; state is reread under lock. | P1 safety correctness | Closed; retain the deterministic concurrency regression. |
| ER-04 | A stream that emitted partial assistant text then fails loses the answer after restart. | **Confirmed, fixed, and merged in PR #51.** Router persists a non-empty visible partial as an assistant message marked `isIncomplete=true`, Room v6 stores the durable marker, and UI labels it `Incomplete response`. No fallback occurs after visible content. | P1 data-recovery correctness | Closed; retain Router failure-after-delta, migration/reopen, and UI semantics regressions. |
| ER-05 | Broad `catch (_: Throwable)` can swallow fatal VM errors. | **Confirmed, fixed, and merged in PR #53.** Router, Gemini, and OpenAI-compatible boundaries normalize only `Exception`; regressions prove a fatal `AssertionError` propagates. The archive rollback boundary deliberately rethrows its primary `Throwable` after rollback attempt and is unchanged. | P2 resilience | Closed; retain fatal-propagation and cancellation/normalization regressions. |
| ER-06 | A 60-second read timeout is unsuitable for all reasoning streams. | **Configuration confirmed; impact unproven.** A static timeout is present, but a correct value depends on provider/device/network behavior. | Phase 7.5 / P2 evidence | Keep as physical-device/network validation item; do not alter timeout policy solely from speculation. |
| ER-07 | Keystore must require biometric/PIN per access. | **Not a defect as stated.** Existing design uses a non-exportable Keystore vault for BYOK; per-operation user authentication is a product trade-off that may break foreground streaming/rotation/recovery. | Deferred security design | Consider a separate optional high-assurance mode only after user research and Android-device threat analysis. |
| ER-08 | Migration 4→5 can fail from duplicate credential references. | **Contradicted for normal supported upgrade.** The credential-reference unique index was already created in migration 1→2. A duplicate database would require corruption/out-of-contract historic state. | Not a current migration bug | Retain corruption recovery tests; no blind deduplication migration. |
| ER-09 | Automatic model discovery should be added. | **Intentional exclusion.** IVAI requires explicit, user-controlled provider/model configuration and no automatic network action. | Not a bug | Keep manual model-ID path; evaluate a later user-started discovery phase separately. |
| ER-10 | SSE `trimStart()` is not fully literal SSE parsing. | **Needs a protocol regression case.** Static pattern merits a narrow parser test; no failure scenario was provided. | P3 candidate | Add a spec-derived test before code change. |
| ER-11 | OpenAI-compatible event types are ignored. | **Needs compatibility evidence.** The present adapter contract is intentionally narrow; supporting arbitrary events changes protocol behavior. | Deferred compatibility | Add only after an explicitly supported provider/test vector identifies a required event. |
| ER-12 | `substringBeforeLast('-')` corrupts router attempt IDs. | **Contradicted by current construction.** Candidate entries are created as `$attemptId-${position}`, so removing the final suffix restores the known parent attempt ID. | Not a current bug | Prefer explicit parent ID only if future ID formats change; no behavior fix now. |
| ER-13 | Credential presence method name is misleading. | **Minor naming/design observation; needs current call-site audit.** | P3 | Defer until a semantic rename can be made without misleading behavior changes. |
| ER-14 | Release minification is disabled. | **Confirmed configuration.** This is a release-hardening decision, not a safe one-line toggle; R8 rules and full release validation are required. | P2 release hardening | Plan a separate release-build hardening phase before public Alpha, never enable blindly. |
| ER-15 | `first { … }` can crash if the registry changes. | **Confirmed, fixed, and merged in PR #55.** Removing the resolved connection before the candidate opens previously threw `NoSuchElementException`. Router revalidates connection/account/model with `firstOrNull` and emits a safe `INVALID_REQUEST` failure with a failed attempt trace. | P2 resilience | Closed; retain stale-catalog safe-failure regression. |
| ER-16 | `ByteArray` equality in a data class is reference-based. | **Language behavior confirmed, product impact unproven.** | P3 test ergonomics | Add content-equality assertion only if current code/tests compare payload objects. |
| ER-17 | Network exchange may be closed twice. | **Needs exact call-site review; no observed failure.** | P3 cleanup | Do not change resource lifecycle without a targeted test. |
| ER-18 | HTTPS-only local endpoint policy causes Ollama/LM Studio friction. | **Intentional security boundary.** HTTP, mDNS and discovery remain explicitly excluded. | UX research item | Test comprehension of HTTPS-only guidance on a physical device; do not add HTTP exception. |
| ER-19 | Safe diagnostics should be connected to telemetry. | **Rejected as phrased.** Hosted telemetry conflicts with Local-first/Backendless design. | Not a bug | If needed, evaluate a user-exported, fully redacted local diagnostic bundle in a separate privacy phase. |
| ER-20 | ViewModel integration coverage is limited / ViewModel is large. | **Code-quality observation, not a verified runtime defect.** | Deferred architecture/test quality | Split only in a focused feature-boundary phase; avoid a high-risk refactor during release hardening. |
| ER-21 | Markdown parse is necessarily causing main-thread jank. | **Unconfirmed and source names in the report do not match current source paths.** | Physical-device performance evidence | Profile compact/medium devices before refactor; preserve streaming behavior until data exists. |
| ER-22 | Archive import's 16 MiB bounded allocation is a critical OOM. | **Bounded allocation confirmed; criticality unproven.** The archive size is capped and import staging protects state, but low-memory device evidence is absent. | P2 hardening candidate | Add malformed/memory-pressure-safe test coverage and evaluate chunking only with measured need. |
| ER-23 | `BufferedReader.readLine()` blocks cancellation. | **Needs device/network reproduction.** Socket cancellation behavior cannot be inferred solely from source. | Phase 7.5 network evidence | Keep as a real-device timeout/cancel scenario; no HTTP client migration now. |
| ER-24 | Keystore/DataStore desynchronization needs clearer recovery. | **Plausible UX/recovery candidate; exact current handling needs a focused vault test.** | P2 candidate | Test missing/invalidated key reference and user-visible safe recovery before changing deletion behavior. |
| ER-25 | Archive format corruption case needs more tests. | **Reasonable test-gap candidate.** Existing checksum rollback test is present; malformed-format atomicity should be verified. | P3 test hardening | Add only a focused regression test after reviewing current decoder failure paths. |

## Confirmed Work Order

ER-01 through ER-05 and ER-15 were reproduced, fixed, and merged in standalone PRs. Remaining P2 and P3 work remains separate from the completed Router resilience increment.

| Order | Candidate | Why separated |
|---|---|---|
| 1 | ER-01 — Router UI duplicate user message | Closed in PR #46 with a stable-ID regression. |
| 2 | ER-02 — Calculator contract | Closed in PR #49 with bounded arithmetic semantics and parser limits. |
| 3 | ER-03 — Approval concurrency | Closed in PR #50 with deterministic approval-lifecycle serialization. |
| 4 | ER-04 — Partial stream recovery | Closed in PR #51 with a Room v6 incomplete-message marker and recovery regressions. |
| 5 | ER-05 — exception boundaries | Closed in PR #53 with fatal-propagation regressions across Router, Gemini, and OpenAI-compatible boundaries. |
| 6 | ER-15 — safe Router registry lookup | Closed in PR #55 with revalidation and a safe failed trace. |
| 7 | Remaining P2/P3 candidates | Next work resumes with a focused ER-22 archive malformed-input test or another evidence-confirmed candidate; no batch hardening. |

## Non-Negotiable Deferrals

This triage does not authorize marking Phase 7.5 complete, weakening the HTTPS-only endpoint policy, enabling HTTP/local discovery, adding a backend or telemetry, introducing a default provider/model, moving to an unreviewed network client, enabling R8 without release validation, adding a real MCP connection, creating a public Alpha artifact, or accepting simulated participant/device results.
