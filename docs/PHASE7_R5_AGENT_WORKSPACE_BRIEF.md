# Phase 7 R5 — Agent and Workspace Surface Rebuild

## Goal

R5 rebuilds the **Agent** and **Workspace** presentation surfaces around an intentional, local-first hierarchy. The work makes an Agent profile’s explicit target, bounds, current run state, and pending one-time write decision legible before a user acts. It also makes selected project context and the known local file count legible without inventing project activity, file contents, cloud state, or background work.

> **Boundary:** R5 is a presentation and semantics rebuild over existing Room-backed state and existing ViewModel callbacks. Agent runtime behavior, profile persistence, target selection, tool limits, project isolation, approval execution, data schema, export/import, provider transport, and network behavior remain unchanged.

| Surface | R5 hierarchy |
|---|---|
| Agent | Local safety boundary → pending one-time approvals when present → profile library → run activity → selected run workspace/trace. |
| Agent profile | Name, explicit target, bounded tools/project scope, immutable runtime limits, and explicit start action. No automatic target or run. |
| Agent run | Status-first selected run workspace with safe trace and cancel only while non-terminal. |
| Write approval | Still requires visible preview and explicit **Deny** or **Allow once**; R5 only raises its priority in the page hierarchy. |
| Workspace | Local safety/context header → selected project detail when selected → project library → known local-file summary → explicit routes to Chat and Agents. |
| Project activity/files | Only the persisted `fileCount`, project description, and last-modified label may be represented. R5 must not fabricate file names, file contents, event history, task status, or provider activity. |

## Scope

R5 will apply the existing IVAI design tokens to replace dense, same-weight sections with a clear top-to-bottom reading order. It will preserve stable callbacks and accessibility semantics, add meaningful test tags for the new hierarchy, and retain the existing empty, offline, and error state boundaries.

The Agent page may re-order sections and refine summary cards, but it may not introduce profile editing, deletion, individual tool execution, mutable run limits, provider/model discovery, target fallback, automatic retry, or a background Agent. The Workspace page may improve selected-project detail and file-count communication, but it may not add file browsing, project-file writes, activity telemetry, synthetic history, cloud synchronization, or hidden routes.

## Acceptance Gate

| Gate | Requirement |
|---|---|
| Explicit control | Profile creation, target choice, run start, cancel, and approval decisions remain user-initiated and visibly labeled. |
| Approval safety | A pending write approval is visually prioritized but remains unresolved until an explicit Deny or Allow once action. |
| Honest local state | Workspace detail exposes only persisted project fields; no file/activity claims beyond known local file count and last-modified label. |
| Regression | Existing Agent/Workspace callback, target, terminal-run, approval, selection, empty/offline behavior remains covered; new hierarchy has focused Compose regression and dark screenshot evidence. |
| Architecture | No data schema, Agent runtime/tool policy, provider/transport, secret handling, backend, telemetry, or implicit-selection behavior changes. |
| Quality | Full Android debug/release/test/lint gate plus architecture/secret/transport/RTL/contrast guards pass. |

## Deliberately Deferred

| Item | Reason |
|---|---|
| Agent profile edit/delete/enable controls | Requires a separate lifecycle, target-reference, active-run, and approval safety decision. |
| File browser and project activity feed | Current UI model exposes only count, description and timestamp. Rendering richer data without a named data-surface phase would be fabricated. |
| Skills/MCP, background Agent work, scheduler, or automatic execution | Explicitly blocked by Phase 8/Alpha prerequisites and local safety architecture. |
| Provider/Model test action | Remains R8 and needs foreground consent, redaction, no retry, and no discovery. |
| Physical-device evidence | IME, light-theme, TalkBack, Force-RTL, font scale, rotation and lifecycle evidence remain Phase 7.5/Alpha gates. |
