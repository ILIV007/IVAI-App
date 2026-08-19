# Phase 7 R8 — Explicit Provider and Model Test Readiness

## Status and Gate

> **Status: planning/readiness only.** No Provider or Model test action, test button, transport call, probe, discovery request, retry loop, timeout implementation, or network telemetry is introduced by this document.

A runtime R8 implementation is **blocked** until the current Phase 7.5 research/device evidence and the first signed Alpha release gates are truly complete. The current repository record states that those gates remain pending. This readiness phase preserves the user requirement for an explicit Provider/Model test action without bypassing the release-safety decision.

## Product Goal

After the gate opens, a user may deliberately test a single saved Provider Connection or declared Model in the foreground. A test is not a discovery mechanism, target-selection shortcut, periodic health check, automatic retry, router fallback, background worker, or proof that every model capability is available.

| User-visible scope | Permitted future behavior | Explicitly prohibited behavior |
|---|---|---|
| Test Connection | Validate one saved, enabled Connection/Account endpoint using its persisted trust class and the user’s fresh foreground consent. | Scanning hosts, probing saved Connections in bulk, model discovery, testing while typing/saving, auto-enabling a Connection, or silently changing its trust mode. |
| Test Model | Validate one declared Model beneath one saved Connection using the user-declared model identifier and capability context. | Inferring/replacing Model IDs, adding Models, capability discovery, automatic Combo membership, or changing a selected Chat/Agent target. |
| Result | Present a bounded, redacted local result for the single initiated action. | Showing prompt/response content, API key/authorization, raw request/response headers, full endpoint path/query, hidden reasoning, or a fabricated success claim. |

## Preconditions for a Future Test Attempt

All conditions are evaluated immediately before transport construction. A failure is local and visible; it must not open a transport.

1. The action originates from a visible foreground screen and identifies the exact Connection/Model being tested.
2. The saved Connection exists, is enabled, and passes the existing HTTPS/trust-class validation.
3. For a Model test, the Model exists beneath that exact Connection, is enabled, and its declared capabilities remain unchanged from the review screen.
4. The selected Account exists, is enabled, belongs to that Connection, and has a usable stored credential reference or explicit no-auth mode as allowed by the Connection trust class.
5. The user sees the exact endpoint class, target label, whether an API key is used (never its value), and a plain-language statement that a direct request will be sent to this provider.
6. The user explicitly selects **Run one test**. Cancel, dismiss, stale state, missing data, lifecycle stop, or validation failure must leave all records unchanged and must not transmit a request.

## Consent and Lifecycle Contract

| Stage | Required user-visible state | Side effect allowed? |
|---|---|---|
| Entry | A dedicated Test action on a saved Connection/Model, disabled or absent until the runtime gate opens. | No. |
| Review | Exact selected target, endpoint trust class, account label/auth state, redaction notice, and one-shot scope. | No. |
| Consent | Explicit **Run one test** and a distinct **Cancel** action. | Only the explicit confirm may proceed. |
| In progress | Visible foreground-only progress with **Stop test**. | One cancellable request only; no retry, fallback, discovery, or background continuation. |
| Result | Success, failure, cancelled, timeout, or blocked-precondition state with bounded redacted metadata. | No follow-up request or automatic state mutation. |

## Redaction Contract

The future local record/result may contain only the following non-secret, bounded fields: action type (`CONNECTION` or `MODEL`), Connection ID, optional Model ID, endpoint **trust class**, safe host label when it remains non-sensitive, start/end timestamp, elapsed duration rounded to a bounded display unit, outcome category, and normalized safe error category.

It must never contain API keys, credential references, Authorization values, raw URLs including paths/queries, prompts, model output, streaming payloads, headers, response bodies, request bodies, stack traces, IP resolution output, model reasoning, or raw TLS/certificate material. Test history is not approved in this phase; any future persistence requires an independent retention/export/privacy decision.

## Threats and Required Mitigations

| Threat | Required mitigation |
|---|---|
| Hidden remote action after setup/save | Test is a separately reviewed foreground action; setup/save remains transport-free. |
| Endpoint substitution or stale record | Re-read and validate Connection, Account, Model, trust class, enablement, and ownership immediately before URL/transport construction. |
| Credential disclosure | Read secret only at the transport boundary; never place it in Compose state, result copy, trace, logs, history, or exception text. |
| Model discovery or silent mutation | Use the persisted declared Model ID only; do not enumerate, infer, persist, enable, or route anything automatically. |
| Background retry/polling | One user-triggered request, no automatic retry/fallback/discovery; cancel and lifecycle stop close it. |
| Trust/cleartext bypass | Reuse existing HTTPS-only endpoint policy and trust classification; no trust-all, HTTP exception, mDNS, `.local`, LAN scan, or certificate bypass. |
| Misleading success | Report only the observed bounded test outcome; never change Connection/Model status or suggest production readiness automatically. |

## Future Runtime Acceptance Criteria

A later, separately approved R8 runtime phase must provide focused tests proving all of the following:

1. Test UI is absent/disabled until gate activation and is never invoked by Connection/Account/Model save, app start, screen open, refresh, or selection.
2. Review Cancel, dismissal, validation failure, lifecycle stop, and Stop test never send a transport request.
3. Explicit confirmation starts no more than one request; no retry, Combo fallback, discovery, model insertion, status mutation, or background work follows.
4. Every precondition failure occurs before URL or transport construction and returns a safe local result.
5. API-key and no-auth handling preserves the existing vault/header contracts.
6. Transport always uses current HTTPS/trust policy and rejects cleartext, `.local`, scan/discovery, arbitrary host, and trust-bypass paths.
7. Result/exception/trace logging passes secret, endpoint, prompt, response, and header redaction tests.
8. Physical-device evidence covers remote HTTPS, HTTPS loopback, HTTPS private-LAN, timeout, cancellation, rotation, offline, TalkBack, Force-RTL, and font scale, with no fabricated results.

## Deliberately Deferred

| Item | Reason |
|---|---|
| Runtime test button and transport | Blocked by Phase 7.5 and first Alpha gates. |
| Model discovery and endpoint probing | Different capability with enumeration/privacy/network-scan risk. |
| Test history/health status | Requires retention, redaction, export, reset, and UI semantics decisions. |
| Bulk test, scheduled check, auto-retry, fallback, or background work | Violates explicit one-shot foreground consent and lifecycle boundaries. |
| Provider-specific test prompt or output preview | Could leak user/model content and creates provider-dependent behavior. |
| Skills/MCP linkage | Separately gated Phase 8 capability plane; not a Provider test extension. |

## Evidence Gate Before Runtime

| Evidence required | Status now |
|---|---|
| Phase 7.5 participant/device evidence and P0/P1 remediation | Pending |
| Physical HTTPS loopback/private-LAN cancellation/timeout evidence | Pending |
| Signed Alpha APK, SHA-256, release notes, annotated tag, owner approval | Pending |
| R8-specific threat-model review, deterministic redaction/cancellation/consent tests, and semantic/visual review | To be created only after the preceding gates pass |
