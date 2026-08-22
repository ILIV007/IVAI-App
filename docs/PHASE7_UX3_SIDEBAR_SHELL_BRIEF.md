# Phase 7 UX-3 — Single Sidebar Shell and Navigation Contract

**Status:** In progress on a focused branch.

## Goal

Replace the mixed drawer-and-rail shell with one responsive sidebar model. On compact width it is a modal product sidebar; on medium and expanded width it is the same persistent sidebar. The five existing destinations remain stable, while Chat history stays a destination-specific context section rather than primary navigation.

## Scope

| Included | Decision |
|---|---|
| Primary navigation | Exactly five labeled destinations: Chat, Connections, Agents, Workspace and Settings. |
| Compact layout | A modal sidebar opened from the top bar; selecting a route/thread closes it. |
| Medium/expanded layout | A persistent sidebar using the same sidebar content and destination list; no parallel rail implementation. |
| Top bar | Menu only when the compact sidebar is modal; route title; neutral overflow affordance. No theme toggle, preview control, subtitle pill or launcher-art reuse. |
| Context ownership | Chat history remains below primary destination navigation only while Chat is active. |
| Regression | Width-mode, item selection, primary-route count, top-bar and context ownership semantics/screenshot coverage. |

## Deliberately unchanged

UX-3 does not change destination routes, thread/project/provider/agent selection behavior, Chat composer or IME, Connections lifecycle, screen-specific layouts, data/schema, provider runtime, transport, credential vault, telemetry, launcher resources, signing or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition |
|---|---|
| Width contract | compact is modal; medium/expanded are persistent; no `NavigationRail` or bottom navigation is mounted. |
| State preservation | width is presentation-only: current destination/thread/project state is owned by the existing ViewModel and not rewritten. |
| Semantic navigation | every primary destination has a visible label, selected state and stable test tag. |
| Context separation | `ChatSessionDrawerSection` is visible only in Chat context and is not counted as a primary route. |
| Quality gate | secret scan, debug/release build, unit suite and lint succeed in protected CI. |

## Deferred validation

Physical compact/medium ergonomics, rotation, font scale, Force-RTL, TalkBack and keyboard behavior remain device gates. The shell rebuild adds no claim that these field tasks have been executed.
