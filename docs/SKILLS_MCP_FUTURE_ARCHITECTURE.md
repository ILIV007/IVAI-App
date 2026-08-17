# Future Architecture — Skills and MCP

> **Status:** Planning only. This document adds no runtime capability, network connection, credential, process execution, background job, database migration, or release claim.
>
> **Decision:** IVAI will prepare for Skills and Model Context Protocol (MCP) as **separate, user-controlled capability planes**. Their implementation begins only after the current Phase 7.5 evidence gates and Alpha release gates are closed, and each subphase remains independently reviewable.

## 1. Product Intent and Non-Negotiable Boundaries

IVAI remains a Local-first, Backendless, BYOK Agent Harness. Skills and MCP must make user-selected workflows more composable; they must not convert IVAI into a hosted automation service, a marketplace that silently installs code, or a client that grants external tools implicit access. The user continues to own every Provider, account, endpoint, model, Combo, Agent target, project boundary, credential reference, and permission decision.

MCP is relevant because it standardizes JSON-RPC communication between a host, its clients/connectors, and capability-providing servers. Its server features include tools, resources, and prompts; its current specification also notes opt-in extensions, including Skills over MCP.[1] This future architecture treats all remote server metadata, tool descriptions, prompt templates, resource labels, and capability annotations as **untrusted input** until a user reviews and authorizes their use.

| Boundary | Future design decision |
|---|---|
| Local-first | Skill manifests, server profiles, consent decisions, local traces, and enabled scopes are stored only on device. No cloud catalog, account, synchronization, telemetry, session replay, or central relay is introduced. |
| Backendless | IVAI connects directly to a user-selected server only in the foreground after explicit confirmation. It never proxies MCP traffic through an IVAI service. |
| BYOK | A server token, OAuth token, refresh token, or client secret is retained only in the Android Keystore vault. Room stores opaque references and non-secret metadata only. |
| Provider-neutral | A Skill or MCP server never selects or changes a Provider, Model, Combo, Agent target, endpoint trust mode, project, or approval automatically. |
| Explicit execution | Discovery, capability fetch, resource read, prompt selection, tool invocation, scope escalation, retry, and cancellation each have an explicit user-visible state. |
| No hidden autonomy | No background Agent, scheduled run, webhook, silent retry, automatic server reconnection, or marketplace auto-install is part of the initial roadmap. |

## 2. Two Complementary Capability Planes

A **Skill** is an IVAI-local, declarative workflow package. It describes a bounded task recipe, expected inputs, required user-visible capabilities, and safety notes. It does not contain executable shell code, native binaries, arbitrary scripts, hidden network URLs, or an authorization grant. A Skill may reference only local IVAI capabilities that the user has explicitly enabled, or a separately configured MCP capability that remains subject to its own consent policy.

An **MCP Server Profile** is a user-managed connection definition for a capability server. It is not a Provider connection, Agent target, Combo candidate, or implicit tool bundle. The profile describes a chosen HTTPS server endpoint, transport/protocol facts, vault credential reference, declared capabilities, user-approved tool/resource/prompt allowlists, and current local trust state. An Agent may use a server only when the profile, Agent policy, project boundary, tool scope, and per-invocation consent all permit it.

| Dimension | Local Skill | MCP Server Profile |
|---|---|---|
| Primary purpose | Reusable, bounded workflow guidance for an IVAI Agent. | User-managed access path to a remote capability server. |
| Initial content | Local manifest, instruction text, declared inputs/outputs, required capability references, safety notes. | HTTPS endpoint, transport/version metadata, vault reference, declared tools/resources/prompts, approval state. |
| Network behavior | None by itself. | Foreground-only, user-started, direct HTTPS after explicit server/profile approval. |
| Execution authority | Never grants a new capability; it only requests an existing approved one. | Never invokes a tool automatically; each granted capability remains consent-gated. |
| Storage | App-private local files plus Room metadata; no hidden sync. | Room non-secret metadata plus Android Keystore reference; no secret in trace/export. |
| Initial exclusions | Arbitrary code, shell, background triggers, remote package fetch, opaque binaries. | Stdio subprocess launch, embedded server hosting, HTTP/cleartext, `.local` discovery, LAN scan, background sessions. |

## 3. Initial Trust and Consent Model

The trust model must distinguish a static description from a tested connection and a tested connection from authority to act. A valid profile is not a trusted tool, and a trusted read tool is not authority for a mutation. Tool labels and annotations supplied by an MCP server remain advisory; IVAI derives the final consent request from the declared action, arguments, affected project, requested scope, and local policy.

| Local state | Meaning | What IVAI may do | What remains forbidden |
|---|---|---|---|
| Draft | User is editing a Skill or server profile locally. | Validate local schema and show a review summary. | Connect, discover, invoke, store a secret in Room, or modify Agent target. |
| Declared | Profile/manifest is saved but untested. | Display local metadata and allow explicit edit/delete/disable. | Automatic health check, background retry, capability assumption, or execution. |
| Reviewed connection | User explicitly starts a foreground compatibility/discovery action and reviews server identity/capabilities. | Retain reviewed non-secret capability metadata with version/time. | Trust tool descriptions, grant broad scope, or invoke tools automatically. |
| Scoped approval | User chooses an exact resource/prompt/tool allowlist for an Agent or Skill. | Permit only the selected capability through the Agent consent path. | Wildcard server access, implicit scope escalation, cross-project access, or always-allow mutation. |
| Disabled / revoked | User disables profile, Skill, scope, or credential reference. | Keep local audit metadata without secrets. | Reconnect, retry, or invoke until user explicitly re-enables/reviews. |

For remote MCP operations, IVAI will preserve the existing `Allow once` model for mutation-like work and add an explicit preflight for external data sharing. A read request must identify the server, selected tool/resource/prompt, exact project context, proposed arguments or redacted argument summary, and declared data categories. A mutation-like request must additionally show the external consequence and one-time nature of approval. Denial, cancellation, scope failure, disconnect, and expired authorization must terminate safely; no fallback to a different server, Provider, Combo, credential, scope, or tool is allowed.

## 4. Transport and Authorization Policy

The initial MCP connectivity design is deliberately narrower than the full protocol surface. Standard MCP transports include stdio—where the client launches a server subprocess—and Streamable HTTP; Streamable HTTP uses JSON-RPC over POST/GET and has explicit security considerations for origin validation, localhost binding, and authentication.[2] IVAI will not launch subprocesses in the initial architecture, because that would contradict the current prohibition on process execution and would require a distinct Android threat model.

| Transport / authorization capability | Initial policy | Rationale |
|---|---|---|
| HTTPS Streamable HTTP | Candidate for a later, explicit foreground-only phase. | It can be directly user-configured and preserves IVAI’s direct, user-chosen connection model. |
| Stdio | Explicitly deferred. | It launches a server subprocess; Android process/package execution needs a separate threat model and must not be hidden behind “MCP support.” |
| HTTP / cleartext | Rejected. | It violates the existing endpoint policy. |
| `.local`, mDNS, LAN scanning, auto-discovery | Rejected. | Server identity and network boundary must remain explicit; no ambient discovery is allowed. |
| Localhost/private-LAN MCP | Deferred to an independent physical-device security phase, and HTTPS-only if admitted. | Existing HTTPS local-provider evidence does not automatically authorize MCP traffic or tool semantics. |
| OAuth authorization | Deferred to a dedicated client-authorization phase. | HTTP-based MCP authorization uses transport-level OAuth patterns and least-privilege scopes; it requires a dedicated Android browser/PKCE/redirect, token-vault, issuer/audience, revoke, and physical-device review.[3] |
| User-provided static token | Candidate only after vault/profile phase and explicit foreground consent. | It can use the established BYOK vault boundary but still requires redaction and revocation coverage. |

When OAuth is later evaluated, the design must use the protocol’s authorization discovery and resource/audience binding rather than accepting a bearer token from an arbitrary source. The specification calls for protected-resource metadata and authorization-server discovery, uses resource indicators, and states that access tokens must not be passed in URI query strings.[3] IVAI will treat this as a future security gate, not a shortcut for early MCP access.

## 5. Initial Local Data Model

The following entities are **planning shapes**, not an approved Room migration. No schema change is included in this document. The design separates non-secret local metadata from vault-held secrets and makes every cross-entity reference explicit.

| Entity | Local metadata allowed | Vault data / prohibited data |
|---|---|---|
| `SkillDefinition` | Stable ID, display name, version, local description, declared input schema, required capability references, safety notes, enable state, provenance label. | No executable binary, shell command, private key, remote auto-update URL, credential, or hidden endpoint. |
| `SkillBinding` | Agent profile ID, Skill ID, selected local parameters, enabled state, explicitly selected capability references. | No copied secret, implied broad server permission, or autonomous schedule. |
| `McpServerProfile` | Stable ID, user label, canonical HTTPS endpoint, selected transport/version, local trust state, disabled state, capability metadata hash/version, timestamps. | No token in Room, no URL query credential, no unreviewed dynamic instruction as executable policy. |
| `McpCapabilityGrant` | Server profile ID, capability ID/type, allowlist state, Agent/Skill binding, project constraint, declared scope summary, user decision timestamp. | No wildcard default, no permanent write permission, no cross-project grant. |
| `McpInvocationTrace` | Redacted tool identifier, request lifecycle, consent result, timing/status/error class, selected profile/target IDs. | No token, prompt body, file content, raw resource, raw response, credential, or unsafe argument payload. |

Migration and recovery rules must be written before implementation: profile/skill disable is reversible; credential revocation makes an operation unavailable without deleting unrelated local data; server deletion must remove grants/bindings safely; malformed imports fail closed; export/import omits vault material and active authorization sessions; and process death denies unresolved external consent.

## 6. UX Blueprint

The initial UX should extend existing user concepts rather than create a hidden “developer mode.” It must preserve the current five-destination navigation and let users understand the difference between an Agent workflow, a local Skill, a Provider target, and an external MCP capability.

| Surface | User goal | Mandatory UI elements | Explicitly excluded initially |
|---|---|---|---|
| Skills Library | Browse local reusable workflow definitions. | Local-only badge, provenance label, required capability summary, enable/disable, full text review, no-network statement. | Marketplace, remote search, auto-install, executable code import. |
| Skill review/binding | Attach a local Skill to an Agent deliberately. | Required inputs, selected project, required capability references, target unchanged statement, final review. | Skill-driven target/provider changes, silent grant creation, scheduled execution. |
| Connections → MCP Servers | Configure one user-chosen server profile. | HTTPS endpoint review, transport support, credential mode, vault summary, test/discover button with foreground warning, disable/revoke/delete. | URI credential fields, HTTP, scanning, auto-connect, provider preset substitution. |
| MCP capability review | Review what a tested server declares. | Tool/resource/prompt categories, data-sharing warning, explicit allowlist, scope/status, untrusted-description notice. | “Trust all,” wildcard grant, default enablement, silent scope upgrade. |
| Agent final review | Confirm an Agent+Skill+MCP execution context. | Active Provider/Combo target, project boundary, Skill, exact server/tool, data category, one-time approval choice. | Blended/ambiguous target, cross-project access, always-allow mutation. |
| Run trace | Explain external lifecycle safely. | Server/profile ID, tool ID, consent/cancel/disconnect/scope status, redacted timing and error state. | Raw token, prompt, file, response, resource content, telemetry upload. |

The visual language follows Phase 7’s existing indigo/emerald/violet system and semantic accessibility primitives. New screens need TalkBack labels, BiDi coverage, compact/medium adaptive layouts, empty/offline/error/cancel states, and no fabricated connectivity/activity indicators. The launcher-only VA artwork remains outside these product surfaces.

## 7. Proposed Post-Alpha Phases

These phases are ordered dependencies, not a commitment to implement every phase automatically. Each implementation phase requires a focused PR, threat model, tests, visual/semantics review, security scan, and protected merge. No phase may combine a new transport, new execution class, and broad UI redesign.

| Phase | Goal | Explicit scope | Gate before advancing |
|---|---|---|---|
| 8.0 — Capability architecture decision | Freeze terminology, trust tiers, permission vocabulary, data classification, threat model, and UX acceptance matrix. | Documents, test plan, and design prototype only. | Security/design review agrees that Skills do not grant authority and MCP remains opt-in/direct. |
| 8.1 — Local Skill registry | Add schema-validated, app-private declarative Skill definitions and a local library UI. | No execution code, no remote catalog, no network, no MCP, no scheduler. | Import/migration/delete/export/redaction/process-death tests; Skill cannot alter Agent target or grant capabilities. |
| 8.2 — Skill-to-Agent binding | Let a user bind an enabled local Skill to an Agent through final review. | Explicit capability references and local parameter validation only. | Target/project/binding validation, cancellation, recovery, RTL/semantics and screenshot coverage. |
| 8.3 — MCP profile foundation | Add disabled-by-default user-managed MCP server metadata and vault references. | Local schema/editor/revoke/delete/import validation; no connection attempt. | No secret in Room/export/trace; endpoint parsing rejects HTTP/query/fragment/userinfo; migrations and revocation pass. |
| 8.4 — HTTPS foreground MCP handshake | Add a single user-started HTTPS Streamable HTTP compatibility/initialization flow. | Exact user-chosen server; protocol/capability review; explicit cancel/error states. | No background reconnect/fallback/discovery; TLS/redirect/cancellation/timeout tests and physical-device evidence. |
| 8.5 — Capability allowlist and safe reads | Let a user grant exact MCP resource/prompt or read-only tool access to one Agent/Skill/project. | Redacted consent and local trace; no mutation tool. | Least-privilege allowlist, server metadata untrusted, cross-project denial, consent/recovery/redaction tests. |
| 8.6 — One-time external mutation | Permit a narrowly declared mutation-like MCP tool only with preflight, preview where possible, and Allow once. | One explicit tool invocation; no permanent grant or retry. | Threat model, failure/cancel/process-death tests, physical-device review, and fresh usability evidence. |
| 8.7 — OAuth/authorization support | Implement standards-conformant user authorization for a selected HTTPS MCP profile class. | External user-agent authorization, PKCE/state/issuer/resource validation, vault storage, revoke/expiry. | Security review, token redaction, audience/scope tests, redirect attack tests, physical-device evidence. |
| 8.8 — Interoperability review | Evaluate opt-in Skills over MCP and optional extensions only after their specification maturity and IVAI threat review. | Compatibility adapter/prototype behind explicit experimental flag. | Version negotiation, no authority escalation, privacy review, and rollback/disable path. |
| 8.9 — Deferred execution threat model | Decide whether background tasks, external triggers, tasks extension, or long-running operations belong in IVAI at all. | Decision record only before any runtime work. | Explicit owner decision, durable background architecture, user scheduling UI, revocation model, and battery/network/privacy review. |

## 8. Explicit Deferrals and Rejection Criteria

The following items remain out of scope unless a new named phase, threat model, and acceptance gate approve them. Their presence in a server description, online tutorial, or marketplace does not create product scope.

| Deferred / rejected item | Reason |
|---|---|
| Stdio server launch, embedded MCP server hosting, arbitrary APK/plugin/native code, shell, Termux, Shizuku | These introduce process/code execution and require a distinct Android sandbox/threat model. |
| Remote marketplace, auto-update, auto-install, dynamic code loading, unsigned Skill package | They break local review/provenance and create supply-chain risk. |
| HTTP, `.local`, mDNS, LAN scan, automatic endpoint discovery, automatic health checks | They weaken explicit network/trust boundaries. |
| Background Agent/MCP activity, polling, scheduled run, webhook, event trigger, persistent reconnect | They create autonomy and lifecycle/privacy/battery obligations that are not covered by Alpha. |
| Wildcard tool grant, global server trust, automatic OAuth scope escalation, permanent external mutation permission | They violate least privilege and one-time consent. |
| MCP Apps or arbitrary server-rendered UI | They require untrusted UI isolation and a separate rendering/privacy model. |
| Multi-device Skill/profile sync | It conflicts with Local-first until an explicit encrypted sync strategy is chosen. |

## 9. Completion Evidence and Release Rule

A future Skill/MCP phase is complete only when its deterministic quality gate is green, the exact threat-model assertions are tested, all user-visible capability/consent/error states have semantic and visual coverage, the architecture scans remain clean, and relevant physical-device evidence exists. A green test suite does not waive user-comprehension, accessibility, foreground network, cancellation, timeout, OAuth, or external-mutation evidence.

No future phase changes the current Alpha decision. The current Phase 7.5 participant/device evidence, signed release, SHA-256, annotated tag, release notes, and owner approval remain prerequisites for the first public Alpha.

## References

[1] [Model Context Protocol Specification — 2026-07-28](https://modelcontextprotocol.io/specification/2026-07-28)

[2] [Model Context Protocol Transports — 2025-06-18](https://modelcontextprotocol.io/specification/2025-06-18/basic/transports)

[3] [Model Context Protocol Authorization](https://modelcontextprotocol.io/specification/draft/basic/authorization)

[4] [IVAI Provider-Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md)

[5] [IVAI Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
