# Phase 7 UX-1 — Adaptive Launcher Icon Rebuild

**Status:** Implemented locally; CI and physical-launcher validation are pending.

## Goal

Replace the composite PNG previously used as an adaptive-icon foreground with a dedicated **symbol-only vector**. The correction targets the installed-launcher crop/zoom failure while preserving IVAI's launcher-only VA identity.

## Verified diagnosis

The retired `ivai_launcher_foreground_safe.png` contained a full square composition: background, gradient/glow field, circle, VA mark, and decorative sparkle. Android adaptive icon launchers independently apply masks and insets to foreground assets. Treating that complete composition as the foreground caused the installed result to be visually cropped/zoomed.

## Scope

| Included | Contract |
|---|---|
| Foreground | `ivai_launcher_foreground.xml` is a 108dp vector containing the VA silhouette only. The mark is scaled to 70% around its 59.5dp optical x-centre and translated to the 54dp container centre. |
| Background | `ivai_brand_icon_background.xml` is an independent, full-bleed `#101432` layer matching reviewed splash/system chrome. |
| Themed icon | `ivai_launcher_monochrome.xml` uses the same 70% silhouette geometry as a tintable vector mask. |
| Entries | Both `ic_launcher.xml` and `ic_launcher_round.xml` reference the independent background, symbol-only foreground, and monochrome mask. |
| Regression | Resource unit test and shell guard reject bitmap foreground wrappers, the retired composite assets, missing adaptive layers, and a missing safe-zone scale. |

## Deliberately unchanged

This increment does **not** alter Compose screens, product wordmark, UI palette, runtime/provider behavior, database/schema, credential handling, networking, telemetry, manifest identity, signing, release process, or physical-device evidence. The VA mark remains launcher-only and is not introduced into the product navigation/UI.

## Acceptance gate

| Evidence | Required outcome | Status |
|---|---|---|
| Launcher resource guard | symbol-only vector, independent background, matching monochrome layer, no composite fallback | Pending local/CI run |
| Unit suite | all existing tests plus new adaptive-layer assertions pass | Pending CI |
| Build + lint | debug/release build and lint have no regression | Pending CI |
| Physical compact/medium launcher | no crop/zoom on installed icon | Pending owner/device evidence |
| OEM/themed launcher | readable monochrome silhouette and no composite artefact | Pending owner/device evidence |

## References

- Android Developers, [Configure Adaptive Icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)
- IVAI, [UI/UX execution blueprint](/home/ubuntu/ivai_uiux_execution_blueprint_2026-08-21.md)
