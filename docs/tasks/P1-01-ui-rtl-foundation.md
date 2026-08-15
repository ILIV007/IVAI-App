# P1-01 — Canonical Chat State and UI/RTL Foundation

## Metadata

| Field | Value |
|---|---|
| Owner | IVAI implementation agent |
| Branch | `feat/ui-rtl-foundation` |
| Target phase | Phase 1 — UI/RTL Skeleton closeout |
| Roadmap reference | `docs/ROADMAP.md`, Phase 1 |

## Goal

Remove the unused second chat path and make the app shell own one canonical workspace state through a ViewModel. Preserve current mock-only behavior while making thread selection, message changes, project assignment, and navigation testable and configuration-safe.

## Context to read

`AGENTS.md`, `IVAI_PROJECT_MASTER.md`, `docs/ARCHITECTURE.md`, `docs/RTL_BIDI.md`, `docs/SECURITY.md`, `MainActivity.kt`, `ChatsScreen.kt`, `NavigationDestinations.kt`, and the existing Robolectric tests.

## In scope

The task introduces a `WorkspaceViewModel` and immutable UI state for app-shell navigation, preview state, threads, projects, selected IDs, and mock chat mutations. `IvaiMainApp` obtains that state from one ViewModel and delegates events to it. The obsolete, unattached `MainChatScreen`/`MainChatViewModel` path is removed. Existing stable test tags remain intact, and tests move to the app-shell path with explicit navigation and mixed RTL/BiDi checks.

## Out of scope

No Room, DataStore, saved credentials, actual provider request, streaming, provider error handling, new dependency, full localization, app-shell RTL flip, design rewrite, Agent runtime, or network integration is included.

## Constraints

The app remains mock-only and local-first. The shell remains LTR; user message content remains direction-aware through existing message components. Technical tokens, IDs, paths, URLs, timestamps, and code stay LTR-isolated. No current test tag is renamed without an equivalent test update.

## Acceptance criteria

| Area | Required outcome |
|---|---|
| Canonical state | `IvaiMainApp` has no mutable thread/project/navigation business state; it renders `WorkspaceUiState` and sends events to `WorkspaceViewModel`. |
| Chat path | Only the `ChatsScreen` path is used by production UI; the obsolete standalone chat path is removed. |
| Navigation | Sidebar destination selection, thread selection, new chat, and delete behavior operate through the ViewModel and retain current UI behavior. |
| UI state | Thread creation, project creation/assignment, user-message insertion, preview selection/reset, and project filter selection are covered by focused state tests. |
| RTL/BiDi | At least one app-shell UI test exercises an RTL message alongside an LTR technical token and confirms the chat screen renders without regression. |
| Validation | `assembleDebug`, `testDebugUnitTest`, `lintDebug`, diff check, and the PR CI checks pass. |

## Risks and rollback

The main risk is accidentally changing mock data behavior while moving it from `remember` state to a ViewModel. Keep callbacks and test tags stable, migrate in small steps, and revert the feature branch if the UI contract regresses. No schema or user data migration exists in this phase.

## Handoff requirements

Report changed files, test commands/results, visual/RTL evidence, removed obsolete path, known limitations, scope deviations, commit SHA, and whether the Phase 2 Data/Security gate is ready.
