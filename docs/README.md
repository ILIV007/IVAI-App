# IVAI Documentation Index

> **Current release state:** IVAI is in controlled **pre-Alpha** preparation. Deterministic checks are green, but participant, physical-device, accessibility, real-network, owner-signing, and release-approval evidence remain pending. A document may record deterministic evidence without closing a human or physical gate.

## Start by role

| Role or objective | Primary documents |
|---|---|
| Product owner or reviewer | [Roadmap](ROADMAP.md), [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md), [Alpha Release Policy](ALPHA_RELEASE.md) |
| Android contributor | [Architecture](ARCHITECTURE.md), [Security](SECURITY.md), [Repository Contribution Guide](../CONTRIBUTING.md) |
| Provider/runtime reviewer | [Provider Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md), [Provider and Combo Execution Brief](PHASE7_2_PROVIDER_COMBO_EXECUTION_BRIEF.md), [R8 Provider/Model Test Readiness](PHASE7_R8_PROVIDER_MODEL_TEST_READINESS.md) |
| Release owner | [Release Candidate Preparation](RELEASE_CANDIDATE_PREPARATION.md), [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md), [Alpha Release Policy](ALPHA_RELEASE.md) |
| Phase 7.5 facilitator | [UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md), [Field Kit](PHASE7_5_FIELD_KIT.md), [Controlled Scenario Cards](PHASE7_5_CONTROLLED_SCENARIO_CARDS.md) |
| Security reporter | [Root Security Policy](../SECURITY.md) and [Security Architecture](SECURITY.md) |
| Future capability planner | [Skills and MCP Future Architecture](SKILLS_MCP_FUTURE_ARCHITECTURE.md), [Skills and MCP UI Blueprint](SKILLS_MCP_UI_BLUEPRINT.md) |

## Product and architecture

| Document | Purpose | Status |
|---|---|---|
| [Roadmap](ROADMAP.md) | Authoritative phase plan, scope decisions, completed deterministic work, and external gates. | Active |
| [Architecture](ARCHITECTURE.md) | Local-first data path, provider boundaries, Router, Agent, workspace, and recovery design. | Active |
| [Security](SECURITY.md) | Threat boundaries, credential handling, transport policy, local storage, and release integrity. | Active |
| [Provider Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md) | Provider-neutral/BYOK product alignment and adapter constraints. | Active |
| [Provider and Combo Execution Brief](PHASE7_2_PROVIDER_COMBO_EXECUTION_BRIEF.md) | Explicit target selection, capability matching, ordered fallback, and Provider/Combo execution behavior. | Active |

## Validation and release

| Document | Purpose | Status |
|---|---|---|
| [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md) | Canonical quality, evidence, signing, tag, and publication gate checklist. | Active |
| [Release Candidate Preparation](RELEASE_CANDIDATE_PREPARATION.md) | Local unsigned candidate build and verification procedure. | Active |
| [Alpha Release Policy](ALPHA_RELEASE.md) | Owner-controlled signing and GitHub Release boundaries. | Active |
| [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md) | Authoritative participant/device/research protocol and thresholds. | Active |
| [Phase 7.5 Field Kit](PHASE7_5_FIELD_KIT.md) | Facilitator preflight, prompts, device sweep, and evidence rules. | Active |
| [Phase 7.5 Field Handoff Audit](PHASE7_5_FIELD_HANDOFF_AUDIT_2026-08-20.md) | Controlled research package contract and explicit deferred gates. | Active |
| [Phase 7.5 Deterministic Hardening Audit](PHASE7_5_DETERMINISTIC_HARDENING_AUDIT_2026-08-21.md) | Latest static security, release, and quality evidence. | Active |
| [Terminal Stream Hardening Audit](TERMINAL_STREAM_HARDENING_AUDIT_2026-08-21.md) | Provider-stream terminal-event remediation and regression evidence. | Active |
| [Repository Professionalization Audit](REPOSITORY_PROFESSIONALIZATION_AUDIT_2026-08-21.md) | README, documentation navigation, GitHub governance, hygiene, and maintenance-automation evidence. | Active |

## UX, accessibility, and visual work

| Document | Purpose |
|---|---|
| [UI/UX Validation Record](PHASE7_UIUX_VALIDATION.md) | Evidence record; remains Pending until real field/device results are entered. |
| [RTL and BiDi](RTL_BIDI.md) | Directionality boundaries and allowed narrow LTR exceptions. |
| [UI Quality and P0 Monitor](UI_QUALITY_AND_P0_MONITOR_2026-08-19.md) | Deterministic quality status and explicitly pending Alpha blockers. |
| [UI/UX Map](phase7_uiux_map.png) | Visual navigation and UX reference map. |
| [Phase 7 UI/UX Research Notes](PHASE7_UIUX_RESEARCH_NOTES.md) | Design/research decisions and future validation context. |

## Future architecture and planning

| Document | Purpose |
|---|---|
| [Skills and MCP Future Architecture](SKILLS_MCP_FUTURE_ARCHITECTURE.md) | Local-first architectural direction for future Skills/MCP capability. |
| [Skills and MCP UI Blueprint](SKILLS_MCP_UI_BLUEPRINT.md) | UI planning material for a later, separately gated phase. |
| [Task Template](TASK_TEMPLATE.md) | Template for scoped, reviewable future task packets. |
| [Compatibility Research](COMPATIBILITY_RESEARCH_2026-08-19.md) | Historical evidence for the toolchain compatibility closure. |

## Historical decision records

Files named as dated audits, compatibility reports, review records, or earlier phase briefs preserve provenance. They are not automatically current policy. When a historical record conflicts with a current checklist or current roadmap statement, the **active** document in the tables above governs. Do not delete historical evidence merely to make the documentation shorter.

## Documentation maintenance rules

Every material change to provider behavior, network policy, persistence/schema, file access, Agent capability, release tooling, or user-visible safety controls must update the relevant active document and state what remains deliberately unchanged. No documentation update may claim a real-device, participant, signing, or release result unless corresponding evidence exists.
