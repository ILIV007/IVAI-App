# Phase 8.0 — Capability Consent and Traceability Matrix

> **Status:** Architecture-only preparation. This matrix defines terminology, consent boundaries, traceability expectations, and test evidence for a future Skills/MCP implementation. It creates **no** screen, navigation route, Room migration, credential entry, endpoint, network request, protocol parser, execution path, background work, or release approval.
>
> **Dependency rule:** Phase 8 runtime implementation remains blocked until Phase 7.5 participant/device evidence and the first signed Alpha release gates are closed. This document may clarify future decisions; it cannot convert any pending Phase 7.5, signing, tag, checksum, release-note, owner-approval, or public-release row to Pass.

## 1. Purpose and Architecture Boundary

IVAI is a Local-first, Backendless, BYOK Agent Harness. A future Skill or MCP Server Profile is a **separate capability plane**, never an implicit Provider, Model, Combo, Agent target, project selection, credential grant, or execution authority. This matrix is the canonical Phase 8.0 cross-reference for the future architecture and UX blueprint.[1] [2]

| Term | Canonical meaning | Does not mean |
|---|---|---|
| **Skill** | A local, declarative workflow definition with reviewed instructions, declared inputs, capability references, and safety notes. | Executable code, a remote package, an automatic run, a target override, or a permission grant. |
| **MCP Server Profile** | A user-managed local record of one explicitly chosen capability endpoint and its non-secret metadata. | A Provider connection, an Agent target, an online presence signal, or a trusted tool bundle. |
| **Capability declaration** | Untrusted metadata a chosen server reports about a tool, resource, or prompt. | A verified behavior, a safe action, or approval to access data. |
| **Capability grant** | A local, explicit allowlist binding one selected capability to a specific Agent or Skill and project boundary. | Wildcard server authority, a cross-project grant, a background grant, or permanent external mutation permission. |
| **Per-invocation consent** | A visible, one-time decision for one exact external operation after a redacted preflight. | Profile creation, discovery approval, scope approval, a retry, or an always-allow setting. |
| **Trace** | A local, redacted lifecycle record explaining selected IDs, consent result, timing, and normalized status. | A copy of a token, prompt, file, raw argument, resource value, server response, or telemetry event. |

## 2. State and Transition Matrix

No state may advance because of an app launch, process recovery, network availability, retry timer, server declaration, or an Agent decision. Every upward transition is user-initiated and visibly attributable.

| State | Local meaning | Entry action | Permitted local behavior | Explicitly forbidden |
|---|---|---|---|---|
| **Draft** | The user is editing unsaved local Skill or profile material. | User opens create/edit flow. | Local validation and review summary. | Network, discovery, invocation, secret persistence in Room, or target mutation. |
| **Declared** | Non-secret local metadata is saved but has not been tested. | User completes explicit final save. | Edit, delete, disable, and display local metadata. | Automatic health check, capability assumption, scope grant, execution, or retry. |
| **Reviewed** | User started a foreground compatibility review and inspected non-secret server identity/capability metadata. | User presses a visible `Start review` control. | Display reviewed metadata, failure/cancel state, and timestamp/version/hash. | Trust-all, invocation, wildcard scope, background reconnect, or automatic target change. |
| **Scoped** | The user selected exact capability IDs, an Agent/Skill binding, project boundary, and data-category scope. | User confirms a visible allowlist review. | Offer only the selected capability through the normal Agent consent path. | Cross-project access, unreviewed capability use, implicit scope escalation, or permanent mutation permission. |
| **Invocation pending** | An exact operation has reached its visible one-time consent preflight. | User explicitly starts a selected action. | Show the selected target, profile, capability, project, redacted arguments, data category, and consequence. | Hidden request, automatic retry/fallback, secondary server/provider choice, or unredacted payload rendering. |
| **Disabled / revoked** | A profile, Skill, grant, or credential reference is unavailable by user choice or invalidation. | User disables/revokes/deletes, or local credential validity check fails closed. | Retain safe local audit metadata and offer deliberate edit/remove/re-enable flow. | Reconnect, retry, invoke, silently substitute another profile/grant, or reveal secret material. |

A process death, cancellation, timeout, authorization expiry, scope mismatch, malformed declaration, TLS error, or disconnect must resolve to a safe terminal state. It must not transition itself to `Reviewed`, `Scoped`, or a new invocation.

## 3. Consent Matrix

Each consent surface answers a different question. A later consent cannot be inferred from an earlier one.

| Consent layer | User question | Minimum preflight detail | Decision | Effect of denial/cancel |
|---|---|---|---|---|
| **Profile review** | “Do I want IVAI to contact this exact server now?” | Canonical HTTPS endpoint, trust class, auth-method summary, initialization data categories, foreground-only statement, cancel control. | `Start review` / `Cancel` | No connection, no retry, no profile state escalation. |
| **Capability allowlist** | “Which declared abilities may this Agent or Skill request in this local project?” | Exact server/profile, capability type and ID, Agent/Skill binding, project boundary, declared data category, disabled-by-default statement. | `Allow selected` / `Keep disabled` | Capability remains unavailable; no substitute capability or broader grant. |
| **External-read invocation** | “May this exact external read happen now?” | Agent/Skill, selected target, profile, resource/prompt/read-only tool, project, redacted argument summary, data categories, one-time statement. | `Allow once` / `Deny` / `Cancel` | No request, no fallback, no replay after restart. |
| **External-mutation invocation** | “May this exact external consequence happen now?” | All external-read detail plus declared consequence, affected local/external boundary, preview when feasible, one-time statement. | `Allow once` / `Deny` / `Cancel` | No request, no permanent grant, no automatic retry or replay. |
| **Authorization lifecycle** | “May IVAI use or revoke this selected authorization for this exact profile?” | Selected profile, issuer/resource/audience facts, scopes, expiry/revoke behavior, vault-only storage boundary. | Future explicit user-mediated authorization controls only. | No token use, no refresh/retry, no alternate issuer or scope escalation. |

## 4. Data Classification and Redaction Matrix

Future implementation must derive consent copy and trace behavior from local policy, not from a server-supplied label. Server metadata, tool descriptions, prompts, and resource names are untrusted input until rendered as inert, bounded text and reviewed by the user.

| Data category | May appear in consent summary | May appear in local trace | Must never appear in Room trace/export/screenshot/log |
|---|---|---|---|
| Selected local IDs | Profile, Agent, Skill, capability, project, target IDs or labels. | Redacted stable IDs, lifecycle/status, timing. | Implicit alternate target/provider/profile selection. |
| Endpoint identity | Canonical HTTPS authority/path summary without URI credentials. | Profile ID and normalized status/error class only. | URI userinfo, query credential, fragment secret, raw redirect URL with sensitive parameters. |
| Credential/authentication | Auth method class and vault-reference availability state. | Safe availability/expiry/revocation status. | Token, API key, refresh token, client secret, authorization code, PKCE verifier, bearer header, raw OAuth error payload. |
| Prompt/file/resource content | Category and size/scope summary only when necessary. | No raw content; only approved category and normalized result state. | Prompt body, file contents, workspace content, resource values, raw response, copied arguments. |
| External operation | Capability ID/type, declared consequence, one-time decision. | Request lifecycle, consent result, timing, normalized failure class. | Server-provided hidden instruction, unbounded arguments, raw tool result, identity mapping, telemetry upload. |

## 5. Required Deterministic Evidence Before Runtime Subphases

The following evidence defines the entry gate for each later implementation increment. It does not waive Phase 7.5 or Alpha prerequisites.

| Future subphase | Architecture-only proof required first | Deterministic implementation evidence later | Evidence that remains physical/user-dependent |
|---|---|---|---|
| **8.1 Local Skill registry** | Skill cannot imply authority, network, target change, or executable code. | Schema/import/export/delete/recovery/redaction tests; semantic and screenshot coverage. | Accessibility/device review of final UI. |
| **8.2 Skill-to-Agent binding** | Binding and target/project/capability references are separate concepts. | Invalid binding, cancellation, recovery, no-target-change, BiDi/semantics regression tests. | Task comprehension and accessibility review. |
| **8.3 MCP profile foundation** | Profile is disabled-by-default and separate from a Provider. | Endpoint parsing, vault-reference, migration, revoke/delete/import/export/redaction tests. | Physical-device review before any foreground connection phase. |
| **8.4 Foreground HTTPS compatibility review** | No background, discovery, fallback, or stdio process may be introduced. | TLS/redirect/cancel/timeout/error normalization tests with fake transport. | Exact-server/device cancellation, timeout, accessibility, and network evidence. |
| **8.5 Allowlist and safe reads** | Declaration, grant, and invocation are distinct; server metadata is untrusted. | Least-privilege, cross-project denial, trace redaction, consent/process-death tests. | User comprehension and assistive-technology review. |
| **8.6 One-time external mutation** | Mutation cannot inherit an earlier read/profile/scope approval. | Preflight, deny/cancel/process-death/no-retry tests and reviewable consequence model. | Fresh usability and physical-device evidence. |
| **8.7 Authorization** | Authorization is profile-specific, user-mediated, and vault-bounded. | PKCE/state/issuer/resource/audience/revoke/expiry/redirect-attack/redaction tests. | Real user-agent/device accessibility and authorization-flow evidence. |

## 6. Phase 8.0 Completion Boundary

Phase 8.0 may be marked **planning-ready** only when architecture/design review agrees that the vocabulary, state transitions, consent layers, data categories, redaction rules, explicit deferrals, and later acceptance criteria do not conflict with IVAI’s Local-first, Backendless, BYOK, explicit-target, project-isolation, or `Allow once` policies.

Planning-ready does **not** mean Phase 8 runtime-ready. It does not authorize a Room migration, Compose route, local Skill import, MCP profile editor, foreground connection, OAuth flow, read, mutation, transport, background behavior, telemetry, signed release, or Alpha/public release. The authoritative public-release blockers remain in the release checklist and Phase 7.5 protocol.[3] [4]

## References

[1] [Future Architecture — Skills and MCP](SKILLS_MCP_FUTURE_ARCHITECTURE.md)

[2] [Skills and MCP — Initial UX Blueprint](SKILLS_MCP_UI_BLUEPRINT.md)

[3] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[4] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
