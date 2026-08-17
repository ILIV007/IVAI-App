# Phase 6.1 — Launcher Monochrome Hardening Brief

> **Status:** Implementation planned on `feat/phase61-launcher-monochrome-hardening`.
>
> **Relationship to the Roadmap:** This is a narrow, deterministic **Phase 6 hardening increment**. It closes only the Android adaptive-icon monochrome resource gap identified as P1 in [Phase 6 Hardening Audit](PHASE6_HARDENING_AUDIT.md). It does not approve Alpha release readiness or complete the physical-device evidence gate.

## Goal

Provide a distinct, single-color **launcher-only** vector mark for Android themed icons. The mark should retain the recognizable interlocking VA silhouette of the approved launcher artwork while allowing Android to apply system icon tinting through the adaptive-icon `<monochrome>` layer.

## In Scope

| Area | Change |
|---|---|
| Adaptive launcher resources | Add one alpha-safe vector drawable dedicated to themed-icon monochrome rendering. |
| `ic_launcher` and `ic_launcher_round` | Reference that drawable through `<monochrome>`. |
| Regression coverage | Add resource/manifest assertions that both adaptive icons reference the dedicated monochrome resource and that it stays launcher-only. |
| Documentation | Record scope, validation evidence, and the remaining Alpha/device gates accurately. |

## Explicitly Out of Scope

This increment must not modify the approved VA bitmap, legacy density icons, app foreground/background artwork, Compose theme, TopBar/wordmark, Provider/Router/Agent/Data runtime, Room schema, credential handling, network policy, permissions, release signing, or physical-device evidence. The monochrome mark must not appear anywhere in the product UI.

## Design Constraints

The source reference remains the supplied launcher artwork: a black interlocking **V/A** silhouette inside an indigo/emerald/violet atmosphere. The new vector is a **functional system mask**, not a replacement brand logo. It uses a single opaque path color with transparent surroundings; Android, not IVAI, supplies the final themed tint.

## Acceptance Gate

| Gate | Required result |
|---|---|
| Adaptive resources | Both adaptive launcher XML files declare a `<monochrome>` layer pointing to the dedicated vector. |
| Lint | `MonochromeLauncherIcon` findings are eliminated; no new Error/Fatal finding is introduced. |
| Build and test | `clean assembleDebug testDebugUnitTest lintDebug` succeeds. |
| Architecture | Security, transport, prohibited-execution, implicit-provider, global-LTR and launcher-boundary scans remain clean. |
| Visual boundary | The new resource is referenced only by adaptive launcher XML; no Compose/UI source references it. |

## Deferred

Actual themed-icon rendering varies by launcher/OEM and remains part of the physical-device Phase 6/7.5 matrix. This increment does not claim device verification, signed release readiness, or public Alpha approval.
