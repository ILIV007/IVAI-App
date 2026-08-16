# Phase 7.0 — Design Foundation Report

> **Status:** In progress. This report records implementation evidence for the shared IVAI UI foundation only; no feature screen has been redesigned yet.

## Scope

Phase 7.0 introduces a reusable UI foundation for later Chat, Connections, Agent, Workspace, and Settings redesign sub-phases. It deliberately leaves Provider, Router, Agent, data, endpoint-policy, and runtime behavior unchanged.

## Implemented foundation

| Layer | Delivered contract |
|---|---|
| Tokens | Shared spacing, shape, elevation, stroke, icon-size, motion and semantic-color roles in `IvaiTokens.kt`. |
| Theme boundary | The dark indigo and light violet-white palettes are consumed as independent semantic UI roles; comments and gradient naming no longer imply that UI artwork derives from the launcher image. |
| UI primitives | `IvaiScreenScaffold`, `IvaiPageHeader`, `IvaiStateCard`, `IvaiTargetChip`, and `IvaiExecutionStatusBanner`. |
| Accessibility | Stable test tags, heading semantics, button role/state descriptions, terminal/approval live-region support, and 48dp target-chip minimum height. |
| Test matrix | Compose/Robolectric tests cover semantic heading/target/status contracts and recordable light/dark screenshots. |

## Launcher-icon boundary

The user-supplied VA image remains restricted to Android launcher-icon resources. The foundation has no VA image or mark, and the preview uses only an `IVAI` text identity plus independent tokenized components. The UI palette may harmonize with indigo, emerald/aqua, and violet but does not copy the launcher artwork’s gradient or mark.

## Visual review evidence

The record-mode test generated `phase7_foundation_light.png` and `phase7_foundation_dark.png` in `app/build/roborazzi/`. Review of both confirms the following.

| Criterion | Light | Dark | Result |
|---|---|---|---|
| Independent in-app identity | Text-only foundation preview; no launcher VA artwork | Text-only foundation preview; no launcher VA artwork | Pass |
| Hierarchy | Page heading, context target, execution state, then setup state read in order | Same order remains clear against indigo canvas | Pass |
| Semantic state differentiation | Warning approval state and informational setup state have distinct border/accent treatment | The same roles remain visually distinct and readable | Pass |
| Token rhythm | Shared 16/12/8dp rhythm and rounded component scale are visible | Shared rhythm remains stable | Pass |
| Decorative restraint | No gradient carries state or text meaning | No gradient carries state or text meaning | Pass |

## Deferred to later sub-phases

- Applying primitives to the actual Chat, Connections, Agent, Workspace and Settings screens.
- Full adaptive navigation and the Chat Session Drawer in 7.1.
- Existing legacy `StateViews` migration; it stays in place until each consuming screen is redesigned.
- Physical-device TalkBack, Force-RTL and font-scale evidence; these remain Alpha hardening gates.


## Launcher-only correction evidence

The prior TopBar reference to `ivai_brand_reference` was removed in this sub-phase after the user clarified that the supplied VA artwork is launcher-icon-only. `IvaiTopBar` now renders an accessible text wordmark (`IVAI wordmark`, test tag `ivai_wordmark`), and the existing app screenshot test was updated accordingly. A recorded screenshot confirms the app bar now contains the independent IVAI wordmark and no VA artwork. The post-change scan found no remaining launcher-artwork reference in `app/src/main/java` or `app/src/test/java`; the artwork remains only inside the launcher icon resource chain.
