# Phase 7 UX-3 — Single Sidebar Shell and Navigation Contract

**Status:** Deterministic implementation and CI validation are complete in [PR #128](https://github.com/ILIV007/IVAI-App/pull/128), squash-merged to `main` as `665751927989aef16688ba66a217baba8644d540` on 22 August 2026. Physical validation remains pending.

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

| Evidence | Pass definition | Status |
|---|---|---|
| Width contract | compact is modal; medium/expanded are persistent; no `NavigationRail` or bottom navigation is mounted. | **Passed in PR #128 CI** |
| State preservation | width is presentation-only: current destination/thread/project state is owned by the existing ViewModel and not rewritten. | **Passed in PR #128 CI review** |
| Semantic navigation | every primary destination has a visible label, selected state and stable test tag. | **Passed in PR #128 CI** |
| Context separation | `ChatSessionDrawerSection` is visible only in Chat context and is not counted as a primary route. | **Passed in PR #128 CI** |
| Quality gate | secret scan, debug/release build, unit suite and lint succeed in protected CI. | **Passed in PR #128 CI** |

## Validation record

PR #128 initially exposed a test-host visibility assumption for a LazyColumn row and a non-existent Compose test assertion import. Both issues were limited to the new regression test and resolved in follow-up commits before the final successful CI run. The required `Secret scan` and `Build, unit test, and lint` checks completed successfully before merge. The sandbox did not contain an Android SDK, so local Gradle completion is not claimed.

## Deferred validation

Physical compact/medium ergonomics, rotation, font scale, Force-RTL, TalkBack and keyboard behavior remain device gates. The shell rebuild adds no claim that these field tasks have been executed.
