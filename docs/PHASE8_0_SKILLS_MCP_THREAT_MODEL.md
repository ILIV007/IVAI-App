# Phase 8.0 — Skills and MCP Threat Model

> **Status:** Planning-only threat model. It identifies security and safety assertions for future, focused implementation phases. It adds **no** runtime Skill, MCP Server Profile, network connection, endpoint, Room entity, vault record, OAuth flow, tool invocation, process execution, background behavior, or release claim.
>
> **Dependency rule:** This document does not bypass Phase 7.5 participant/device evidence or the first signed Alpha release gates. Any future runtime subphase must satisfy its own deterministic and physical/user evidence in addition to those prerequisites.

## 1. Scope, Security Objective, and Non-goals

The security objective is to preserve IVAI as a Local-first, Backendless, BYOK Agent Harness when a future user chooses to define a local Skill or connect to an external capability server. A Skill never grants authority. A server declaration is untrusted. Every profile, capability, data category, project boundary, and operation must remain explicit and reviewable.

This model covers future local Skill definitions, MCP Server Profiles, capability metadata, consent decisions, local traces, direct foreground HTTPS communication, and later authorization. It does not authorize implementation. It does not cover process-hosted servers, cleartext traffic, remote marketplaces, arbitrary code, background execution, multi-device sync, or permanent external mutation grants; those remain rejected or deferred by the roadmap.[1]

## 2. Assets and Security Properties

| Asset | Required security property | Never acceptable |
|---|---|---|
| User authority | A Skill, server, Agent, or lifecycle event cannot select a Provider/Model/Combo/target/project or grant an external capability automatically. | Implicit target change, wildcard authority, or authority inherited from a profile. |
| Consent decisions | Profile review, allowlist, and one-time invocation decisions are separate, local, visible, and attributable. | A prior review implying scope/mutation approval, always-allow external mutation, or consent replay after restart. |
| Credential material | Tokens, static keys, refresh tokens, secrets, and authorization artifacts remain vault-only when a future phase permits them. | Room/UI/trace/export/log/URI credential storage or copying into a prompt. |
| Project data | Local project boundary and declared data categories constrain every external request. | Cross-project read, hidden file/prompt/resource disclosure, or payload inferred from a server declaration. |
| Execution trace | A local trace explains selected IDs, consent outcome, timing, and normalized state without carrying sensitive payloads. | Raw arguments, file content, prompt, resource, response, token, telemetry upload, or identity mapping. |
| Endpoint identity | One user-chosen canonical HTTPS endpoint is reviewed for one profile. | HTTP, URI credentials, arbitrary redirect/endpoint substitution, `.local`/mDNS, scanning, discovery, or silent fallback. |
| Local availability | Disable/revoke/delete and process-death recovery stop future use safely. | Background reconnect, silent retry, substitute credential/profile, or action continuation after revocation. |

## 3. Trust Boundaries

| Boundary | Data/action crossing it | Required control before any future implementation | Disallowed shortcut |
|---|---|---|---|
| User ↔ IVAI UI | Profile details, selected capability, data category, consent decision. | Explicit review surface, clear state vocabulary, one-time wording, accessibility/RTL semantics, safe cancel/deny. | Hidden default, ambiguous “trust” language, or consent implied by save. |
| Local Skill file ↔ IVAI | Declarative manifest and instruction text. | Schema validation, bounded inert rendering, provenance label, explicit enable/review/import. | Executable code, dynamic URL, auto-install/update, authority encoded as text. |
| MCP server ↔ IVAI | Endpoint metadata, capability declarations, prompt/resource/tool labels, future responses. | Treat all server content as untrusted; exact allowlist; bounded/redacted display; explicit foreground request only. | Trusting server labels as policy, auto-grant, prompt injection as instruction, or broad capability enablement. |
| IVAI ↔ Android vault | Future static/OAuth credential reference and availability/revocation state. | Android Keystore reference, redaction, local availability check, revoke/expiry behavior. | Secret in Room/export/trace/log/UI, URI query, or copied request description. |
| IVAI ↔ project workspace | Project-scoped input/category summary for a future selected operation. | Exact project constraint, least-privilege scope, preview/redacted summary, cross-project denial. | Whole-workspace default, hidden file enumeration, or result persistence into trace. |
| IVAI ↔ network | Later user-started HTTPS compatibility/read/mutation operation. | Canonical endpoint, HTTPS/TLS policy, cancel/timeout/error state, no fallback/retry/discovery. | HTTP, origin/redirect bypass, background session, LAN scan, or alternate server fallback. |

## 4. Abuse-case Matrix

Each row is a required assertion for a named future subphase. A threat description is not a reason to broaden current runtime scope.

| ID | Abuse scenario | Primary assets at risk | Required mitigation | Deterministic evidence before merge | Physical/user evidence before advancing |
|---|---|---|---|---|---|
| TM-01 | A Skill description claims it can choose a stronger Provider, Agent target, project, or tool. | User authority, project data. | Skill is inert/declarative; binding references selected local capabilities only; target/project remain separately reviewed. | Schema and binding rejection tests; no-target-change regression; UI semantics/copy test. | User comprehension review of Skill binding and final-review vocabulary. |
| TM-02 | A server capability label, prompt, or resource contains prompt injection or requests authority expansion. | Consent decisions, project data. | Server metadata is untrusted inert text; local policy derives consent; default deny/explicit allowlist. | Bounded rendering, metadata validation, no-auto-grant, allowlist and redaction tests. | Accessibility and usability review of untrusted-description warning. |
| TM-03 | A profile endpoint uses HTTP, URI credentials, redirect substitution, `.local`, scanning, or automatic discovery. | Endpoint identity, credentials. | Canonical exact HTTPS endpoint; reject insecure/ambiguous forms; no discovery/fallback. | URL parser, redirect/TLS/error/cancel tests; policy scan. | Exact-server physical-device timeout/cancel and network-transition evidence. |
| TM-04 | A secret or authorization artifact leaks through Room, UI state, trace, export, URI, log, or error. | Credential material. | Vault-only secret storage; opaque reference; redact all traces/diagnostics; no URI credential. | Vault/export/import/trace/log redaction and revoke/expiry tests. | Device authorization-flow and screen-reader review where authorization exists. |
| TM-05 | A selected capability reads another project, whole workspace, or hidden prompt/file/resource content. | Project data, user authority. | Exact project constraint, data-category preflight, cross-project denial, bounded payload policy. | Cross-project denial, category mismatch, path/boundary and trace-redaction tests. | User review confirms data-sharing copy is understandable. |
| TM-06 | A reviewed profile gains broad tool access through a wildcard or scope escalation. | Consent decisions, project data. | Declaration, grant, and invocation remain separate; exact capability IDs only; default deny. | Wildcard rejection, grant migration/revoke, scope mismatch, no-escalation tests. | Independent heuristic review of scope affordances. |
| TM-07 | Cancellation, timeout, disconnect, process death, or credential revocation replays an external operation or falls back to another profile. | Consent decisions, execution trace. | Terminal safe state; no retry/fallback/replay; unresolved external consent denies. | Cancel/timeout/process-death/revoke/no-fallback tests and trace lifecycle assertions. | Device cancellation/timeout and lifecycle evidence. |
| TM-08 | A remote package, native binary, shell command, stdio server, or marketplace item executes under the name of a Skill/MCP feature. | Device integrity, user authority. | Declarative local Skill only; no process launch/dynamic code/marketplace/stdin runtime in approved phases. | Architecture scan and import validation reject executable fields/content. | Separate owner-approved threat model before any deferred execution decision. |
| TM-09 | A future external mutation is approved by a past read, profile review, or scoped grant. | Consent decisions, external consequences. | Distinct preflight plus `Allow once`; no permanent mutation grant/retry/replay. | Deny/cancel/process-death/no-retry tests; consequence/preview contract tests. | Fresh moderated usability and device review of mutation consequences. |
| TM-10 | OAuth authorization accepts the wrong issuer/resource/audience, leaks token material, or silently refreshes/escalates scope. | Credential material, endpoint identity, consent. | User-mediated authorization; PKCE/state/issuer/resource/audience validation; vault-only tokens; explicit revoke/expiry. | Redirect attack, PKCE/state, issuer/resource/audience, expiry/revoke/redaction tests. | Real user-agent/device accessibility and authorization-flow evidence. |
| TM-11 | Oversized or malformed server declarations/responses exhaust resources or corrupt local state. | Local availability, trace integrity. | Bounded parsing/allocation, fail-closed schema validation, atomic local updates, redacted normalized error. | Malformed/oversized payload, cancellation, atomicity, and recovery tests. | Device performance/network evidence before widening limits. |
| TM-12 | UI wording or accessibility semantics make an external request look local, automatic, safe-by-default, or permanently connected. | User authority, consent decisions. | Explicit external/one-time/selected wording; no optimistic status; semantic labels, BiDi, contrast, touch target coverage. | Screenshot/semantics/copy regression tests across state matrix. | TalkBack, Force-RTL, compact/medium device and participant comprehension evidence. |

## 5. Required Controls by Future Phase

| Subphase | Threat-model assertion that must remain true | New evidence required before implementation is considered complete |
|---|---|---|
| 8.1 Local Skill registry | TM-01 and TM-08: Skill is declarative/local, cannot grant authority, execute code, network, or alter target. | Import/export/delete/migration/recovery/redaction tests; final UI/device accessibility review. |
| 8.2 Skill binding | TM-01 and TM-12: binding cannot alter target/project/capability without final review. | Validation/cancel/recovery/semantics tests; user comprehension review. |
| 8.3 MCP profile foundation | TM-03, TM-04, TM-06: profile is disabled-by-default, exact-HTTPS, vault-bounded, and does not grant scope. | Parser/vault/migration/revoke/delete/import/export/redaction tests. |
| 8.4 Foreground HTTPS review | TM-02, TM-03, TM-07, TM-11: untrusted metadata, no fallback, bounded foreground HTTPS lifecycle. | Fake transport/TLS/redirect/cancel/timeout tests plus physical-device evidence. |
| 8.5 Allowlist and safe reads | TM-02, TM-05, TM-06: exact selected capability/project/data-category only. | Cross-project/allowlist/redaction/process-death tests and comprehension/accessibility review. |
| 8.6 One-time external mutation | TM-07 and TM-09: exact consequence, Allow once, no replay/retry. | Threat review, preflight/deny/cancel/recovery tests, fresh device/usability evidence. |
| 8.7 Authorization | TM-04 and TM-10: authorization is selected-profile-specific, vault-bounded, validated, revocable. | Security/redirect/revoke/expiry/redaction tests and device/user-agent evidence. |
| 8.8–8.9 | TM-08 and all affected rows remain unresolved until a named owner decision. | New decision record, threat model, rollback/disable model, and relevant physical/user evidence. |

## 6. Residual Risks and Explicit Deferrals

The following risks are intentionally not accepted by architecture documentation alone: real server behavior, TLS/device network interaction, user comprehension, TalkBack/Force-RTL behavior, OAuth browser behavior, mutation consequence clarity, low-memory performance, and any background lifecycle. They require later controlled evidence; a green JVM suite or this threat model cannot replace that evidence.

Stdio subprocesses, embedded server hosting, arbitrary code/native binaries, remote marketplaces, HTTP, discovery/scanning, background tasks/webhooks/polling/reconnect, wildcard grants, automatic OAuth escalation, permanent mutation grants, and multi-device sync remain rejected or deferred. A future request must introduce a named phase, owner decision, threat-model update, deterministic tests, and relevant physical/user evidence before any of those exclusions can change.[1]

## 7. Phase 8.0 Threat-model Exit Criteria

This planning increment is complete only when future implementation work can map every new field, state, network action, consent surface, trace field, error/recovery behavior, and test to an asset, trust boundary, abuse case, mitigation, and evidence row above. It does not authorize a runtime subphase or change the current `Not approved` Alpha/public-release decision.

## References

[1] [Future Architecture — Skills and MCP](SKILLS_MCP_FUTURE_ARCHITECTURE.md)

[2] [Phase 8.0 Capability Consent and Traceability Matrix](PHASE8_0_CAPABILITY_CONSENT_MATRIX.md)

[3] [Skills and MCP — Initial UX Blueprint](SKILLS_MCP_UI_BLUEPRINT.md)

[4] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[5] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
