# Phase 8.0 — Skills and MCP Prototype Review Specification

> **Status:** Planning-only prototype specification. It defines what a future static or Compose-preview review must show and how reviewers record outcomes. It adds **no** production screen, navigation route, Room entity, vault entry, endpoint, network request, server profile, OAuth flow, tool invocation, process execution, or release approval.
>
> **Dependency rule:** Prototype review preparation does not replace Phase 7.5 participant/device evidence or any signed Alpha gate. A static mock may clarify a future decision, but it cannot claim user comprehension, TalkBack behavior, Force-RTL behavior, device usability, network safety, or runtime correctness has passed.

## 1. Purpose and Review Boundary

This specification turns the existing Skills/MCP architecture, consent matrix, threat model, and UX blueprint into a finite set of reviewable static states. Its purpose is to catch ambiguous vocabulary, misleading consent hierarchy, missing failure states, unsafe data-sharing copy, or visual/semantic gaps **before** any future focused runtime subphase is proposed.[1] [2] [3]

The prototype remains inert. Every label, endpoint, capability, error, project, Agent, target, and trace item must be mock/local data. A reviewer may inspect the visual/semantic intent, but may not connect to a server, enter a real credential, import executable content, execute a Skill, invoke a tool, attach a live file/prompt, or approve an external action.

## 2. Prototype Fixture Rules

| Fixture category | Permitted mock form | Prohibited content |
|---|---|---|
| Skill | Local display name, inert instruction excerpt, declared input placeholder, required capability ID, local provenance label. | Executable code, shell command, script, remote package URL, signed binary, hidden endpoint, authority claim. |
| Server profile | Neutral label such as `Research capability`, endpoint placeholder such as `https://capability.example.test/mcp`, selected transport label, disabled/reviewed/scoped state. | Real server, localhost/LAN address, HTTP URL, URI credential, query/fragment secret, actual network status. |
| Credential/authentication | `No auth` or `Vault reference available` mock status only. | Token, API key, refresh token, client secret, OAuth authorization code, PKCE value, copied error payload. |
| Agent/target/project | Local mock labels and selected-ID summaries. | Automatic target/project selection, real workspace file, user identity, real prompt, cross-project data. |
| Capability/trace | Synthetic read/mutation labels, declared data category, redacted lifecycle state, normalized error class. | Raw arguments, resource value, server response, file/prompt content, timing that implies live connectivity, telemetry event. |

Every fixture must state when it is a mock. A status chip may say `Disabled`, `Untested`, `Reviewed`, `Scoped`, `Canceled`, `Denied`, `Timeout`, `Scope unavailable`, or `Revoked`; it must never imply hidden online health, background activity, automatic recovery, trusted server behavior, or permanent permission.

## 3. Required Prototype Scenarios

The prototype is reviewed state-by-state. A scenario passes content/design review only when every listed safety assertion is visually and semantically present in the static review material. It does not pass device or participant validation.

| ID | Scenario | Required visible state | Required safety/copy assertions | Must not imply |
|---|---|---|---|---|
| PR-01 | Empty Skills Library | Empty local-only library under Agents. | `No local Skills yet`; create/import reviewed local definition; local-only/provenance statement. | Marketplace, remote search, auto-install, execution, or a sixth primary navigation destination. |
| PR-02 | Skill detail and review | Inert Skill content, declared inputs, capability references, safety notes, provenance, enable state. | `This Skill grants no new authority and does not run by itself`; target unchanged statement. | Code execution, remote fetch, capability grant, target/project change, or schedule. |
| PR-03 | Bind Skill to Agent final review | Selected Agent, existing Provider/Combo target, selected project, Skill, required capability references. | Explicit target/project summary; final review; no automatic change; cancel/back action. | Binding as execution, profile auto-creation, implicit capability grant, or cross-project scope. |
| PR-04 | Empty/disabled MCP profile list | Connections secondary route with disabled/untested entries and empty state. | `External capability connection`; disabled-by-default explanation; explicit create/edit/delete/revoke actions. | Online presence, auto-connect, default server, discovery, or Provider substitution. |
| PR-05 | MCP profile editor before save | Identity, exact HTTPS endpoint placeholder, trust class, credential-method mock, final review. | Reject/annotate HTTP, URI credential, query/fragment, `.local`, discovery and scan attempts in mock validation examples; save-disabled statement. | Real endpoint validation, network call on save, imported credential, or trust-all. |
| PR-06 | Foreground compatibility-review warning | Exact mock endpoint, initialization data category, `Start review` and `Cancel`, foreground-only explanation. | User starts review; external/network warning; cancel leaves state untested/disabled. | Automatic health check, background retry, trusted capability, or live response. |
| PR-07 | Capability allowlist | Untrusted declared tool/resource/prompt categories, exact selected IDs, Agent/Skill binding, project, data category. | Default deny; `Allow selected` / `Keep disabled`; no wildcard; server declaration untrusted. | Trust-all, global grant, scope escalation, or an automatic invocation. |
| PR-08 | External-read one-time preflight | Selected profile, read-only capability, Agent/Skill, target, project, redacted argument/data-category summary. | `Allow once`, `Deny`, `Cancel`; exact external operation; no fallback/replay statement. | Profile review or allowlist as read authorization; raw payload rendering; automatic retry. |
| PR-09 | External-mutation one-time preflight | All PR-08 detail plus declared external consequence and preview placeholder when feasible. | Separate mutation warning; one-time nature; denial/cancel safe recovery. | Read approval inherited as mutation permission, permanent grant, silent retry, or process-death replay. |
| PR-10 | Cancel/timeout/disconnect/scope failure | Distinct error/recovery states with redacted normalized cause. | No request after cancel; no fallback profile/provider/tool; explicit deliberate edit/review path. | Hidden reconnect, target change, data loss claim, or fabricated recovery. |
| PR-11 | Disable/revoke/delete | Disabled/revoked profile, Skill, grant, or vault-reference-unavailable state. | Operation unavailable; local audit metadata only; deliberate re-enable/edit/remove action. | Use of old scope/token, substituted credential/profile, or automatic reconnect. |
| PR-12 | Redacted external trace | Local lifecycle summary with selected IDs, consent result and normalized status. | No raw token/prompt/file/argument/resource/response/identity mapping; trace is local only. | Telemetry upload, content retention, or evidence of a live external request. |

## 4. Semantic and Copy Contract

A future static prototype should label the following concepts distinctly. These are content assertions for later screenshot/semantics tests; they do not prescribe implementation code in Phase 8.0.

| Concept | Required semantic intent | Approved vocabulary | Disallowed vocabulary |
|---|---|---|---|
| Local Skill | A reviewed local workflow description, not authority. | `Local Skill`, `reviewed local definition`, `does not run by itself`, `grants no new authority`. | `Installed tool`, `automatic workflow`, `trusted code`, `runs in background`. |
| Server profile | An external capability connection controlled by the user. | `External capability connection`, `disabled`, `untested`, `reviewed`, `scoped`, `revoked`. | `Always connected`, `server is trusted`, `online now` without user-started evidence. |
| Capability declaration | Untrusted server-provided metadata. | `Declared capability`, `review before allowing`, `untrusted description`. | `Safe capability`, `approved by server`, `ready to use` without allowlist. |
| Scope allowlist | Exact, local, project-bounded selection. | `Allow selected`, `Keep disabled`, `selected project`, `data category`. | `Trust all`, `grant all`, `global access`, `use every tool`. |
| External invocation | One exact foreground operation. | `External request`, `selected`, `one-time`, `Allow once`, `Deny`, `Cancel`, `safe recovery`. | `Run automatically`, `always allow`, `retry automatically`, `connected forever`. |
| Failure/revocation | A safe terminal state needing a deliberate next action. | `Canceled`, `Timeout`, `Scope unavailable`, `Revoked`, `Needs review`. | `Recovered automatically`, `switched to another server`, `permission kept`. |

## 5. Acceptance Matrix for Future Static Review

| Review dimension | Required review variants | Pass condition for planning review | Not proven by this review |
|---|---|---|---|
| Information architecture | Existing five primary destinations; secondary entry/return path for Agents and Connections. | Skill, Provider target, MCP profile, capability, grant, and invocation remain distinguishable. | Navigation usability on device or participant preference. |
| Consent hierarchy | PR-05 through PR-11, including deny/cancel/revoke. | Profile review, scope allowlist, read consent, and mutation consent cannot be confused or collapsed. | Actual network/authorization behavior. |
| Privacy/redaction | PR-05 through PR-12 with mock data only. | No fixture/screenshot/copy exposes secret, raw prompt/file/resource/response, identity mapping, or telemetry claim. | Runtime redaction correctness under real data. |
| Accessibility intent | Titles, selected state, external warning, data category, consequence, cancel/deny/revoke and transient status concept. | Semantic intent and copy support a later TalkBack/RTL/contrast test plan. | TalkBack, touch target, Force-RTL, font scale, or device outcome. |
| BiDi/content direction | English, Persian, Arabic mock copy plus narrow endpoint/code placeholders. | Natural text is not globally forced LTR; only technical placeholders are marked as bounded LTR candidates. | Real IME, selection, assistive-service behavior. |
| Visual state coverage | Dark/light; compact/medium; empty/disabled/selected/error/revoked states. | No fabricated connection/activity indicator; color is not sole external/selected/error signal. | Real device layout, OEM rendering, or contrast measurement. |
| Threat-model alignment | TM-01 through TM-12 mapping. | Every scenario references the relevant consent/authority/data-boundary mitigation. | Penetration test or live-server security assessment. |

## 6. Review Record Template

A future design/content reviewer records only non-sensitive observations. The template below is intentionally blank; it is not evidence that the review has occurred.

| Prototype revision | Reviewer role | Scenario IDs reviewed | Content/semantics finding | Severity | Required resolution | Resolution evidence | Status |
|---|---|---|---|---|---|---|---|
| _Pending_ | _Pending_ | _Pending_ | _Pending_ | _Pending_ | _Pending_ | _Pending_ | _Pending_ |

Review material must not include participant identities, real credentials, prompt/file content, live server metadata, screen recordings, device IDs, or raw network logs. Any later participant or device evidence belongs to the controlled Phase 7.5/future-subphase protocol, not this repository template.

## 7. Planning Exit and Runtime Block

This prototype-review specification is planning-ready only when reviewers can map every mock scenario to the consent matrix and threat model, and can identify how a later focused implementation would prove semantics, visual states, redaction, failure recovery, and least privilege. It does not authorize the prototype to be treated as a production screen or implemented in `app/src/main`.

A future Phase 8 runtime PR requires all prior roadmap prerequisites, its own named subphase, focused threat-model assertions, deterministic tests, semantic/visual review, protected CI, and relevant physical/user evidence. The current public Alpha decision remains **Not approved**.[4] [5]

## References

[1] [Future Architecture — Skills and MCP](SKILLS_MCP_FUTURE_ARCHITECTURE.md)

[2] [Phase 8.0 Capability Consent and Traceability Matrix](PHASE8_0_CAPABILITY_CONSENT_MATRIX.md)

[3] [Phase 8.0 Skills and MCP Threat Model](PHASE8_0_SKILLS_MCP_THREAT_MODEL.md)

[4] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[5] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
