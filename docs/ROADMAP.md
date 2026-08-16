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

**Current state:** Gemini, OpenRouter and generic Custom OpenAI-compatible adapters are installed. A local, non-secret cloud preset catalog supports guided setup for OpenAI, Groq, Mistral, Together, DeepSeek, Fireworks and xAI without selecting a provider, model or credential automatically. Device-local and private-LAN model endpoints remain intentionally blocked pending their own trust mode, narrow Android cleartext policy, warning UX and device evidence.

**Gate:** Auth, timeout, 429, offline, cancellation, and fallback scenarios are verified without duplicate side effects. Local endpoint connectivity is not part of this gate until its dedicated threat model and acceptance criteria are approved.

## Phase 5 — Bounded Agent

**Goal:** Provide profile-selected safe read tools and at most one user-confirmed write tool with limits, trace, cancellation, and workspace isolation.

**Current state:** Calculation, current time, bounded project-file read, bounded workspace list, and bounded literal project search are local read-only tools. Workspace tools are confined to the profile project; observations remain in memory and traces retain only safe summaries. Project-file write remains the single mutation tool and requires preview plus Allow once.

**Gate:** No mutation happens without approval; every step is traceable; cancellation is clean; profile tool policy, project confinement, bounded path/size/result behavior, and non-persistence of file observations have regression coverage.

## Phase 6 — Hardening and GitHub Alpha release

**Goal:** Security review, device/RTL/accessibility matrix, recovery validation, signed APK, checksum, release notes, and GitHub release.

**Gate:** All P0 checks are green and known limitations are documented.

## Deferred after Alpha

Google Drive backup, local-model inference, MCP, voice, Termux/Shell, Shizuku, Accessibility automation, race/council routing, smart routing, multi-device sync, and Google Play distribution require separate decisions and threat models.
