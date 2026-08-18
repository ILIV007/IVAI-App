# Skills and MCP — Initial UX Blueprint

> **Status:** Planning only. No screen, navigation route, runtime capability, server connection, credential, or execution behavior is added by this document.
>
> **Design rule:** The existing five primary destinations remain intact. Skills and MCP extend existing mental models; they do not introduce an ambiguous sixth destination, a hidden developer mode, or a global “connect everything” switch.

## 1. Information Architecture

Skills are local Agent workflow definitions, so their primary home is **Agents**. MCP Server Profiles are external capability connections, so their primary home is **Connections**. The existing Agent final-review path joins a selected Agent, target, project, local Skill, and exact external capability only when the user has deliberately chosen each piece.

| Existing destination | Future entry | User mental model | No-go behavior |
|---|---|---|---|
| Agents | `Skills library` secondary route beside the local profile library. | “Reusable local instructions that can be attached to an Agent.” | Skill cannot silently change target, project, tools, provider, or approval. |
| Agents | `Bind skill` in the existing Agent final-review step. | “This Agent will follow this reviewed workflow.” | Bind does not execute, grant MCP access, or schedule a run. |
| Connections | `MCP servers` secondary route beside Provider/Account/Model/Combo management. | “An explicit external capability connection I own and can disable.” | No auto-connect, discovery, server preset, HTTP, URI credential, or default server. |
| Connections | `Capabilities` after explicit user-started compatibility review. | “What this chosen server declares; nothing is trusted automatically.” | No global trust, no auto-grant, no automatic scope upgrade. |
| Agent final review | `Capability bundle` summary. | “The exact Skill, server/tool/resource and project this run may use.” | No blended targets, hidden external data sharing, or permanent mutation grant. |
| Run trace | `External step` rows. | “Why a selected server/tool was requested, approved, canceled, or failed.” | No raw prompt, file, resource, response, token, or telemetry upload. |

## 2. Primary Screen Concepts

### 2.1 Skills Library

The Skills Library is a local-only list within Agents. Its first screen deliberately leads with an empty state rather than a marketplace: “No local Skills yet. Create or import a reviewed local definition.” Each list row shows a plain-language name, a local-only badge, provenance (`Created on this device` or `Imported local file`), enabled/disabled state, and a concise declared capability summary. A user can open details, enable/disable, duplicate, export without secrets, or delete after a local confirmation.

The Skill detail/review screen presents full instruction text, declared inputs, expected output type, required capability references, safety notes, and an explicit statement: “This Skill grants no new authority and does not run by itself.” A `Bind to Agent` action routes into the existing Agent final review; it never creates a target or a connection.

| State | Required user-visible copy/action |
|---|---|
| Empty | “No local Skills yet” + `Create local Skill` / `Import reviewed file`; no remote search. |
| Draft | `Review before enabling`; show missing inputs/capability references. |
| Enabled, unbound | “Available for explicit Agent binding.” |
| Enabled, bound | Show local Agent name and selected project summary; `Review binding`. |
| Invalid or migrated | “Needs review before use”; disable run/bind until local schema validation passes. |
| Deleted/revoked capability | “A required capability is unavailable”; offer safe edit/remove, never substitute another capability. |

### 2.2 MCP Server Profiles

MCP Servers live under Connections because the user already expects to configure endpoint, trust, account/authentication, and connection boundaries there. The initial list distinguishes **Disabled**, **Untested**, **Reviewed**, **Scoped**, and **Revoked** locally; it never implies online presence or real-time server health without a user-started foreground operation.

The profile editor has a deliberate sequence: `Identity → HTTPS endpoint/trust → credential method → local review → save disabled`. The initial saved state remains disabled. The next action, `Review compatibility`, is not an automatic test: it shows a foreground-network warning, the exact canonical endpoint, data that may be sent during initialization, the cancel control, and an explicit `Start review` action. On success, capability metadata appears as untrusted declarations that need separate allowlisting.

| Step | Required controls | Validation / failure state |
|---|---|---|
| Identity | User label, optional local note. | Duplicate/blank label handled locally. |
| Endpoint and trust | Exact HTTPS endpoint, explicit trust class, canonical preview. | Reject HTTP, query/fragment/userinfo, invalid host, `.local`, discovery request, or unsupported local class. |
| Credential method | `No auth`, `vault reference`, and later approved OAuth route. | No token in URI or Room/UI state; no auth creates no vault entry. |
| Save review | Exact endpoint, trust, credential summary, disabled-by-default statement. | No network call until a later explicit action. |
| Compatibility review | Start/cancel control, redacted lifecycle status, capability count/type summary. | Timeout, cancel, TLS, auth, protocol, disconnect, unsupported version, and malformed declaration each explain safe recovery. |
| Capability allowlist | Select exact tool/resource/prompt, project boundary, Agent/Skill binding and data category notice. | Default deny; no `Trust all`, wildcard or silent scope upgrade. |

### 2.3 Agent Final Review and Run Trace

The Agent final-review sheet adds a compact **Capability bundle** section only when a Skill or MCP grant is selected. It preserves the established order: active Provider/Combo target, project boundary, bounded local tools, selected local Skill, selected MCP profile/capability, then final confirmation. The summary must state that an MCP profile is external and that each operation remains consent-gated.

The run trace displays only a redacted `External capability requested`, `Approved once`, `Denied`, `Canceled`, `Timeout`, `Disconnected`, `Scope unavailable`, or `Completed` lifecycle. It never logs external payloads, raw arguments, file content, prompt text, resource values, server responses, tokens, or copied OAuth errors that may contain sensitive parameters.

## 3. Consent Surfaces and Copy Principles

A capability declaration is not a permission. Consent appears at three distinct points: profile review (connection), capability allowlist (scope), and action execution (one-time use). The [Phase 8.0 Capability Consent and Traceability Matrix](PHASE8_0_CAPABILITY_CONSENT_MATRIX.md) is canonical for the state, data-category, denial, redaction, and later-evidence rules. The user must be able to distinguish the three consent layers in language, color, iconography, and trace history.

| Consent surface | User question answered | Required detail | Primary actions |
|---|---|---|---|
| Profile review | “Do I want IVAI to contact this exact server?” | Canonical HTTPS endpoint, trust class, auth method summary, data categories in initialization, cancel behavior. | `Start review` / `Cancel` |
| Capability allowlist | “Which declared abilities may this Agent/Skill request?” | Exact server, capability type/name, project boundary, scope/data category, disabled-by-default statement. | `Allow selected` / `Keep disabled` |
| Per-invocation consent | “May this exact request happen now?” | Skill/Agent, target, server, tool/resource/prompt, redacted argument summary, external consequence, one-time nature. | `Allow once` / `Deny` / `Cancel` |

Copy must use the same safety vocabulary as existing IVAI flows: **explicit**, **selected**, **one-time**, **no automatic change**, **local project**, and **safe recovery**. It must not use “always safe,” “trust server,” “run automatically,” “connected forever,” or optimistic simulated health/latency wording.

## 4. Visual and Accessibility Direction

The Phase 7 IVAI design system remains the visual foundation: dark/light semantic tokens, indigo base, emerald/aqua action emphasis, violet secondary accent, existing heading/state/target/execution primitives, and the launcher-only rule for VA artwork. A capability or external-network state cannot rely on color alone.

| Area | Acceptance expectation |
|---|---|
| Navigation | Existing five destinations preserved; secondary routes have a heading, back action, and predictable return location. |
| BiDi | Natural Persian/Arabic mixed text survives; endpoint/code fragments are narrow bounded LTR exceptions only. |
| TalkBack | Screen title, server state, selected capability, target/project context, consent consequence and transient lifecycle state are announced once and clearly. |
| Touch/contrast | Every task-critical control has accessible size and semantic contrast in dark/light mode. |
| Empty/offline/error | States describe only known local/foreground outcome; no fabricated online activity, auto-retry, or server health. |
| Privacy | Screens never render secret token, raw prompt/file/resource content, or identity mapping in cards, trace, screenshot baseline, export, or error copy. |

## 5. Prototype and Test Plan Before Implementation

Phase 8.0 produces a static or Compose-preview prototype only. It must use mock/local metadata and must not embed a real endpoint, secret, OAuth token, external tool, local server, or live Agent write. The review prototype covers the following flows: empty Skill Library; Skill detail; Agent binding final review; empty MCP profile list; disabled MCP editor; connection-review warning; capability allowlist; one-time external consent; cancel/timeout/scope failure; and redacted trace.

| Test layer | Required evidence before any runtime phase |
|---|---|
| Content/design review | Terminology distinguishes Skill, Provider target, MCP server, capability grant, and per-invocation approval. |
| Screenshot/semantics | Dark/light, compact/medium, empty/error/disabled/selected states with stable labels/tags. |
| Accessibility | TalkBack labels, touch target review, contrast, Force-RTL and mixed BiDi prototype review. |
| Threat review | No screen suggests auto-connect, trust-all, auto-grant, cross-project access, automatic target selection, background execution, secret display, or a consent transition not permitted by the [Phase 8.0 matrix](PHASE8_0_CAPABILITY_CONSENT_MATRIX.md). |
| User research | Add Skills/MCP scenarios to a future controlled, de-identified usability script only after Phase 7.5 completion; no live server or real credential is needed. |

## 6. Design Exit Criteria

The design stage is complete only when the team has approved the information architecture, explicit state vocabulary, consent hierarchy, data categories, error/cancel/revoke behavior, and UX acceptance matrix. It does **not** authorize schema changes, connection code, protocol parsing, OAuth, capability invocation, external mutation, background execution, or a public Alpha release.

## References

[1] [Future Architecture — Skills and MCP](SKILLS_MCP_FUTURE_ARCHITECTURE.md)

[2] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[3] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
