# Phase 7.4 — Project Hub, Settings and Cross-screen Polish

> **Status:** Approved implementation brief on branch `feat/phase74-project-settings-polish`.
>
> **Baseline:** Post-Phase 7.3 review completed on commit `36eb915`. A clean `assembleDebug`, all 111 unit tests, and `lintDebug` passed. The independent security, transport, prohibited-execution, provider-default, global-LTR, launcher-art boundary, and stale-documentation scans were clean. Lint has no errors or fatals; its 19 existing warnings are outside this UI-only scope and must not increase.

## Goal

Make **Workspace** and **Settings** as clear and intentional as the Chat, Connections, and Agents destinations, while bringing shared UI states and product copy into the IVAI design system. The outcome is a project-aware local workspace overview and concise settings hierarchy that communicate user control without changing how project data, credentials, providers, models, Combos, Agents, files, or deletion work.

## Scope

| Surface | In scope | Existing contract retained |
|---|---|---|
| Workspace / Project Hub | A local-only overview; project cards that expose available summary data; selected-project context; intentionally named navigation actions using existing callbacks; predictable empty/loading/offline/error treatment. | `WorkspaceProject` remains limited to `id`, `name`, `description`, `fileCount`, and `lastModified`. Project creation, persistence, isolation, deletion, and files are not changed. |
| Chat linkage | A project card may establish selected-project context before dispatching the existing `createNewChat(projectId)` callback. | The existing chat persistence, target rules, thread assignment, and no-target behavior remain unchanged. |
| Settings | Four concise sections: Appearance, Connections, Privacy, and Local data. Retain the existing dark/light toggle, Connections route, privacy commitments, and destructive local-data callback. | Theme persistence and toggle behavior, provider-management callbacks, vault behavior, data deletion semantics, export/import behavior, and permissions are unchanged. |
| Shared states and copy | Reuse `IvaiScreenScaffold`, `IvaiPageHeader`, `IvaiStateCard`, semantic colors, tokens, direct provider-neutral copy, stable test tags, and clear state descriptions. | State truth continues to come from existing `WorkspaceUiState` and established destination-specific state; no new runtime state machine is introduced. |
| Theme parity | Light/dark surface, semantic color, spacing, shape, icon, and contrast refinement on redesigned Workspace and Settings screens. | The IVAI indigo/emerald/violet identity remains fixed; launcher artwork stays out of product UI. |

## Explicitly Out of Scope

The phase must not change project file isolation, the meaning or confirmation path of deleting local data, export/import behavior, app permissions, Room schema or migrations, provider registry contracts, credential storage, endpoint trust, router selection, model/Combo rules, Agent runtime, autonomous/background work, analytics, session replay, HTTP, local discovery, or central backend behavior.

No field may reveal a secret. No quick action may imply that a provider, model, Combo, Agent, or network connection was chosen automatically.

## Information Architecture and Interaction Rules

### Workspace / Project Hub

The header explains that project summaries are **local** and deliberately avoids claiming metrics that the existing UI state cannot support. A selected project is a context choice, not a permission or provider setting.

| UI element | Source | User action | Meaningful outcome |
|---|---|---|---|
| Project summary | Existing `WorkspaceProject` fields only | None | Displays project name, description, file count, and local modification label. No fabricated file/activity telemetry. |
| Project context chip/card | `WorkspaceUiState.selectedProjectId` | Select or clear project context | Uses the existing `selectProject` callback; it does not mutate project data. |
| Start project chat | Existing `createNewChat(projectId)` | Explicit “Start chat” action | Creates a normal existing chat assigned to the chosen project and routes to Chat. It does not choose an execution target. |
| Open Chat | Existing destination callback | Explicit navigation action | Routes to Chat without creating or sending anything. |
| Status card | Existing `UiPreviewState` used as a preview/test seam | No network retry or hidden recovery | States explain the local UI condition and never suggest background repair. |

### Settings

Settings contains only four discoverable sections with direct copy. Connections is an explicit route to the existing Connections destination; no connection may be added, tested, enabled, disabled, or deleted from Settings.

| Section | Primary copy principle | Allowed interaction |
|---|---|---|
| Appearance | Name the current display mode plainly. | Existing theme toggle only. |
| Connections | State that providers, credentials, models, and Combos are user-controlled. | Existing `onOpenConnections` only. |
| Privacy | Describe verified product commitments without slogans or future promises. | Informational only. |
| Local data | State the destructive consequence before the existing clear-data action. | Existing `onDeleteAllLocalData` only. |

## Design and Accessibility Requirements

Each redesigned screen must use the Phase 7 foundation instead of raw colors, brushes, dp spacing, or hand-rolled card treatments. All interactive targets require stable test tags, an explicit accessible label or state description, and the foundation touch-target minimum. Decorative icons must have no content description. Page titles must retain heading semantics.

The layout must inherit system direction. It must be readable in English, Persian, Arabic, and mixed BiDi text without a global forced-LTR wrapper. Copy must remain provider-neutral, local-first, precise, and present-tense. Examples of prohibited wording include “Cyber Obsidian,” “Default Clean,” unqualified “automatic,” and claims of activity/telemetry not backed by UI state.

## Test Matrix and Acceptance Gate

| Coverage | Required evidence |
|---|---|
| Workspace ready | Existing projects render; selected context and intentional chat action have stable semantics. |
| Workspace empty | Direct explanation and safe route to Chat; no fabricated activity. |
| Workspace loading, offline, error | Uniform semantic state cards; no implicit retry, network claim, or auto-repair. |
| Settings dark and light | Toggle semantics, concise section headings, Connections route, privacy text, and destructive-action warning. |
| Cross-screen regression | Existing five destinations still compile and retain navigation/RTL behavior. |
| Visual | Roborazzi captures for Workspace and Settings representative dark/light states, reviewed for token use, contrast, clipping, and brand drift. |
| Technical | `clean assembleDebug testDebugUnitTest lintDebug`; zero test failures/errors; zero lint errors/fatals; `git diff --check`; security/transport/execution/provider-default/global-LTR scans clean. |

## Deferred to Later Phases

Physical-device accessibility validation, Force-RTL, TalkBack, rotation, font-scale, fresh-install, upgrade, restart, offline network matrix, signed release artifact, and external UX research remain explicitly deferred to Phase 7.5 and the Alpha release gate. Phase 7.4 may add deterministic unit/Roborazzi coverage but cannot substitute sandbox results for those device gates.

## Completion Condition

Phase 7.4 is complete when Project Hub and Settings are rebuilt on the Phase 7 foundation, their representative states are semantically and visually tested in dark/light and RTL-safe layouts, no protected runtime/data contract has changed, repository documentation records the completed phase and remaining Phase 7.5 validation work, and the protected-branch PR/CI/merge workflow has completed successfully.
