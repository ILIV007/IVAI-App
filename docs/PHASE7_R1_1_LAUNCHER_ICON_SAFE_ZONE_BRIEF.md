# Phase 7 R1.1 — Launcher Icon Safe-Zone Remediation

**Status:** Implemented on a focused branch; physical-launcher validation remains pending.

## Goal

Preserve the entire approved VA launcher composition when Android applies adaptive-icon masks. This phase addresses real-device feedback that the launcher showed a cropped or zoomed portion of the artwork.

## Scope

| Item | Decision |
|---|---|
| Adaptive foreground | The approved 800×800 source composition is centered unchanged in a 1200×1200 bitmap with a 200px inset on every edge. |
| Adaptive wiring | Both existing adaptive launcher entries continue to reference the same foreground drawable, which now uses the padded safe-zone bitmap. |
| Legacy fallbacks | Square and round WebP fallbacks are regenerated for all Android density buckets from the padded composition. The round fallback applies a distinct circular alpha mask. |
| Reproducibility | `scripts/render_launcher_assets.py` regenerates the padded foreground and every legacy fallback from the approved reference bitmap. |
| Regression protection | `scripts/test_launcher_icon_assets.sh` verifies foreground wiring, the safe 1200×1200 canvas, exact preservation of the centered approved 800×800 image, expected density dimensions, and distinct square/round fallbacks. The validator runs in CI before Android builds. |

## Deliberately unchanged

This is launcher-only work. It does not change the approved VA artwork, product-screen logo policy, Android manifest package identity, Provider/Router/Agent behavior, data model, network policy, release signing, or the UI redesign phases that follow.

## Deterministic acceptance gate

```bash
bash scripts/test_launcher_icon_assets.sh
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

The phase additionally requires the existing provider-neutral, RTL, Phase 8.0 architecture, controlled-research-package and release-candidate-package guards to pass.

## Evidence still required

A sandbox cannot display a device launcher. Before this finding is declared closed for Phase 7.5, install the exact CI-tested build on physical devices/launchers that use circle and squircle masks, capture evidence that the full VA mark is visible without crop or zoom, and record the launcher/device/Android version. This does not replace the separate owner-signing and public Alpha gates.
