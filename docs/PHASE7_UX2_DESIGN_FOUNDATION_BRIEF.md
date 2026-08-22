# Phase 7 UX-2 — Design Foundation and Component Primitives

**Status:** In progress on a focused branch.

## Goal

Establish a quieter, independent IVAI UI foundation that gives the light theme its own neutral hierarchy, preserves a sibling dark system, and makes shared components consume semantic tokens rather than ad-hoc colors, shapes, and spacing.

## Scope

| Included | Decision |
|---|---|
| Semantic color roles | Formalise canvas, three surface levels, ink hierarchy, limited action colors and state colors for both light and dark themes. |
| Typography | Use a practical screen-title / section / body / meta / code hierarchy without forcing LTR for ordinary content. |
| Layout tokens | Align spacing, shape, touch, elevation and motion decisions to the approved structural blueprint. |
| Shared primitives | Update screen scaffold, page header, state notice, target control and execution banner so their hierarchy is token-driven and accessibility semantics remain explicit. |
| Contrast and regression | Expand deterministic semantic contrast assertions and retain foundation screenshot/semantics coverage. |

## Deliberately unchanged

UX-2 does not change primary navigation, responsive shell, Chat history/IME behavior, Connection/Account/Model/Combo flow, Agent/Workspace/Settings behavior, data/schema, provider runtime, transport, credential vault, telemetry, manifest, launcher resources, signing, or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition |
|---|---|
| Foundation token test | both light and dark semantic foreground/container pairs meet 4.5:1 normal-text contrast; typography remains BiDi-safe. |
| Primitive semantics | heading, target availability and polite execution announcement contracts remain covered. |
| Visual regression | light/dark foundation screenshots render using the new semantic surfaces. |
| Quality gate | secret scan, debug/release build, unit suite and lint succeed in protected CI. |
| Scope audit | no source changes in navigation, screen flows, provider/runtime/data layers. |

## Deferred validation

Light-theme perception, TalkBack, Force-RTL, font-scale, compact/medium device layouts and physical IME behavior remain separate field/device gates. A deterministic screenshot is not evidence that these physical tasks have been performed.
