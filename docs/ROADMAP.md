# Execution Roadmap

> **Product North Star:** IVAI is a Local-first, Backendless, BYOK **Agent Harness**. Users own and manage their provider connections, accounts, endpoints, models, capabilities, credential references, and ordered Combos. Gemini is the first streaming proof adapter only; it is not the product's fixed provider, required account, backend, or routing layer. See [Provider Harness Alignment](PROVIDER_HARNESS_ALIGNMENT.md).

## Phase 0 — Build and repository governance

**Goal:** Buildable repository, Apache-2.0 governance, focused-branch workflow, CI quality gate, and protected main branch.

**Gate:** Wrapper integrity; `assembleDebug`, `testDebugUnitTest`, and `lintDebug` pass; governance files and workflow are present; `main` is protected after the governance pull request is merged.

## Phase 1 — UI/RTL Skeleton closeout

**Goal:** Canonical navigation and chat UI state, predictable state restoration, stable test tags, dark-first design decision, and RTL/BiDi regression coverage.

**Gate:** One chat UI path, navigation tests, English/LTR shell plus mixed RTL content tests, screenshot/semantics baseline, and no real network calls.

## Phase 2 — Local data and security

**Goal:** Room, DataStore, Keystore-backed credential vault, app-private project workspace, redacted diagnostics, and versioned local export/import without secrets.

**Gate:** Migration, process-death, vault, delete-all-data, corrupted import, and redaction tests pass.

## Phase 3 — One-provider chat

**Goal:** Prove the provider-neutral adapter contract with one real Gemini chat vertical slice. This validates streaming, cancellation, persistence, and safe error handling; it does not make Gemini a fixed application provider.

**Gate:** Real stream and cancel work; history survives restart; no secret appears in logs or exports.

## Phase 4 — Provider expansion and Router

**Goal:** User-managed provider connections and accounts; OpenRouter and Custom OpenAI-compatible adapters alongside Gemini; explicit cloud provider presets; manual/discovered models; a capability-aware Combo model; sequential fallback; account health; and attempt trace.

**Current state:** Gemini, OpenRouter and generic Custom OpenAI-compatible adapters are installed. A local, non-secret cloud preset catalog supports guided setup for OpenAI, Groq, Mistral, Together, DeepSeek, Fireworks and xAI without selecting a provider, model or credential automatically. Custom OpenAI-compatible local servers may be configured only through an explicit persisted **HTTPS** loopback or RFC1918 IPv4 trust mode, user confirmation, and API-key/no-auth selection. HTTP, `.local`/mDNS discovery, scanning and cleartext opt-ins remain intentionally blocked.

**Gate:** Auth, timeout, 429, offline, cancellation, and fallback scenarios are verified without duplicate side effects. The local HTTPS trust mode has a dedicated threat model plus unit/migration/transport coverage; physical-device cancellation, timeout and mixed-network evidence remain Phase 6 hardening requirements.

## Phase 5 — Bounded Agent

**Goal:** Provide profile-selected safe read tools and at most one user-confirmed write tool with limits, trace, cancellation, and workspace isolation.

**Current state:** Calculation, current time, bounded project-file read, bounded workspace list, and bounded literal project search are local read-only tools. Workspace tools are confined to the profile project; observations remain in memory and traces retain only safe summaries. Project-file write remains the single mutation tool and requires preview plus Allow once.

**Gate:** No mutation happens without approval; every step is traceable; cancellation is clean; profile tool policy, project confinement, bounded path/size/result behavior, and non-persistence of file observations have regression coverage.

## Phase 6 — Hardening and GitHub Alpha release

**Goal:** Security review, device/RTL/accessibility matrix, recovery validation, signed APK, checksum, release notes, and GitHub release.

**Scope:** This phase does not add new providers, Agent tools, or UI screens. It closes evidence gaps only:
1. Remove global forced-LTR behavior; retain narrow LTR only for code/protocol blocks; capture Force-RTL, mixed BiDi, and TalkBack/semantics screenshots.
2. Physical-device matrix: fresh install, upgrade, restart, rotation, offline, HTTPS loopback/LAN cancellation/timeout.
3. Migration/recovery evidence: legacy upgrade, reopen, corrupted import, delete-all-data.
4. Owner-approved release signing, reproducible build, SHA-256, annotated tag, and reviewed release notes.

**Gate:** All P0 checks are green, known limitations are documented, and the owner approves the signed artifact.

## Phase 7 — Full UI/UX Redesign

**Goal:** Complete visual and interaction redesign of all screens — Chat, Router, Agent, Provider Management, Settings, Sidebar, and onboarding — using an independent IVAI design system with a calm indigo/emerald/violet atmosphere. This phase delivers a polished, production-quality UI/UX that is consistent, accessible, and optimized for the Agent Harness workflow.

**Current state:** **7.0 — Design Foundation and Test Harness is complete.** The app now has shared semantic color, spacing, shape, elevation, stroke, icon-size, and motion tokens; reusable screen/header/state/target/execution primitives; light/dark Roborazzi coverage; and semantics contracts for headings, target availability and execution announcements. The VA artwork remains strictly Android launcher-icon artwork: the in-app TopBar now uses an accessible `IVAI` wordmark, and the app UI does not reuse the launcher mark.

**Scope:**
- Full redesign of all Compose screens and components with a unified design language
- Improved empty states, onboarding, and provider setup flows
- Consistent spacing, typography, elevation, and motion
- Dark/light theme parity with the approved independent IVAI UI palette; the launcher artwork is not reused in product screens
- Accessibility: TalkBack labels, touch targets, contrast, and BiDi text support
- Screenshot/semantics baseline for all redesigned screens
- No new provider adapters, Agent tools, or backend dependencies introduced in this phase

**Gate:** All redesigned screens pass screenshot regression, accessibility semantics, contrast checks, and the launcher-only boundary scan. No behavioral regression in provider, Agent, or data layers.

## Deferred after Alpha

Google Drive backup, local-model inference, MCP, voice, Termux/Shell, Shizuku, Accessibility automation, race/council routing, smart routing, multi-device sync, and Google Play distribution require separate decisions and threat models.
