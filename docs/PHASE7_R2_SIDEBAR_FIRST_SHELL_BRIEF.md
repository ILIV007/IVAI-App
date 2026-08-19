# Phase 7 R2 — Sidebar-First Application Shell

**Status:** Implemented on a focused branch; physical-device navigation/accessibility evidence remains pending.

## Goal

Replace dense compact bottom navigation plus the separate Chat-only drawer with one product sidebar, while retaining the five existing destinations and all runtime/provider/data contracts.

## Scope

| Area | Decision |
|---|---|
| Compact navigation | `IvaiAdaptiveDestinationScaffold` no longer renders a bottom bar. Compact mode is explicitly named `COMPACT_SIDEBAR`. |
| Product sidebar | A single `IvaiProductSidebar` contains Chat, Agents, Workspace, Connections and Settings. Local Chat history is embedded as a section only while Chat is active. |
| Application wiring | The app-level `ModalNavigationDrawer` now wraps every destination. Selecting a destination, thread or new chat closes the same sidebar. |
| TopBar | Production chrome contains one sidebar opener and current context only. Appearance stays in Settings; the preview-state switcher is removed from end-user UI. |
| Responsive layouts | Medium and expanded rails retain the existing destination model; only compact navigation changes from bottom-bar to sidebar-first. |

## Test-first evidence

The compact screenshot regression initially failed because the current mobile shell exposed `ivai_compact_navigation`. It now asserts zero compact-bottom-nav nodes. Additional UI regressions verify that the product sidebar exposes all five destination tags plus local Chat history, and that TopBar exposes a product-sidebar opener while legacy theme/preview controls are absent.

## Deliberately unchanged

This shell phase does not redesign Chat content, composer, Provider/Account/Model flow, Agent/Workspace detail surfaces, Settings local-data behavior, theme palette, project persistence, Provider/Router/Agent behavior, data schema, credential vault, network policy, or release signing. Theme remains user-controlled through Settings.

## Deterministic acceptance gate

```bash
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

The focused navigation screenshot tests, semantic contrast audit, launcher icon validator, provider-neutral guard, RTL guard, Phase 8.0 readiness guard, and both package-verifier regressions must pass.

## Evidence still required

Physical compact and medium device validation must verify that the sidebar opens/closes predictably, all five destinations remain reachable, Chat history is understandable without a second navigation surface, no content is hidden behind the keyboard, TalkBack announces navigation correctly, and Force-RTL/font-scale/rotation do not clip content. No such result is claimed by this branch.
