# Execution Roadmap

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

**Goal:** One provider-neutral adapter contract and a real Gemini chat vertical slice with streaming, cancellation, persistence, and safe error handling.

**Gate:** Real stream and cancel work; history survives restart; no secret appears in logs or exports.

## Phase 4 — Provider expansion and Router

**Goal:** OpenRouter and Custom OpenAI-compatible adapters, capability-aware Combo model, sequential fallback, account health, and attempt trace.

**Gate:** Auth, timeout, 429, offline, cancellation, and fallback scenarios are verified without duplicate side effects.

## Phase 5 — Bounded Agent

**Goal:** If retained in Alpha, provide safe-read tools and at most one user-confirmed write tool with limits, trace, cancellation, and workspace isolation.

**Gate:** No mutation happens without approval; every step is traceable; cancellation is clean.

## Phase 6 — Hardening and GitHub Alpha release

**Goal:** Security review, device/RTL/accessibility matrix, recovery validation, signed APK, checksum, release notes, and GitHub release.

**Gate:** All P0 checks are green and known limitations are documented.

## Deferred after Alpha

Google Drive backup, local-model inference, MCP, voice, Termux/Shell, Shizuku, Accessibility automation, race/council routing, smart routing, multi-device sync, and Google Play distribution require separate decisions and threat models.
