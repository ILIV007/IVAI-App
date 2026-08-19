# Phase 7 R1.2 — Semantic Theme Contrast Foundation

**Status:** Implemented on a focused branch; physical light-theme review remains pending.

## Goal

Establish a deterministic accessibility floor for the existing IVAI light/dark semantic palette before any shell or feature-screen rebuild. This phase addresses the real-device feedback that light mode does not yet provide the same quality as the dark experience.

## Verified finding

The new contrast audit reproduced four light-theme failures against the 4.5:1 WCAG AA text/action threshold: muted text on the light canvas/surface, success text on white, and warning text on white. The dark semantic pairs audited in the same check already met the threshold.

## Scope

| Role | Previous | Updated | Minimum audited ratio |
|---|---|---|---|
| Light muted text | `#718096` | `#667085` | 4.61:1 on canvas; 4.97:1 on surface |
| Light success | `#059669` | `#047857` | 5.48:1 on white |
| Light warning | `#D97706` | `#B45309` | 5.02:1 on white |

`scripts/test_theme_contrast.py` parses the source palette and checks the Material/semantic text-action pairs for both themes. The Android quality workflow runs it before the Debug/Release build.

## Deliberately unchanged

This phase changes no layout, screen hierarchy, navigation, typography scale, Provider/Router/Agent/data behavior, network policy, app icon, theme-control placement, or dynamic-color policy. Light-theme visual hierarchy and component redesign belong to later focused phases.

## Deterministic acceptance gate

```bash
python3 scripts/test_theme_contrast.py
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

Existing provider-neutral, RTL, Phase 8.0 architecture, controlled-research-package and release-candidate-package guards must also pass.

## Evidence still required

This audit proves color-pair contrast only. Physical-device light-theme review across compact/medium layouts, font scale, Force-RTL, TalkBack, dynamic launcher treatment and real screen density remains required before Phase 7.5 or Alpha approval.
