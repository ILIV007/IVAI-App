# Phase 7 UX-1 — Adaptive Launcher Icon Rebuild

**Status:** Deterministic implementation and CI validation are complete in [PR #124](https://github.com/ILIV007/IVAI-App/pull/124), squash-merged to `main` as `82807ab4cd4bbe040abeed9820dcb486cb0de931` on 22 August 2026. Physical-launcher validation remains pending.

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
| Launcher resource guard | symbol-only vector, independent background, matching monochrome layer, no composite fallback | **Passed locally and in PR #124 CI** |
| Unit suite | all existing tests plus new adaptive-layer assertions pass | **Passed in PR #124 CI** |
| Build + lint | debug/release build and lint have no regression | **Passed in PR #124 CI** |
| Physical compact/medium launcher | no crop/zoom on installed icon | **Pending owner/device evidence** |
| OEM/themed launcher | readable monochrome silhouette and no composite artefact | **Pending owner/device evidence** |

## Validation record

The PR’s required `Secret scan` and `Build, unit test, and lint` checks both completed successfully before merge. The sandbox did not contain an Android SDK, so no local Gradle result is claimed; local evidence is limited to the focused launcher guard and whitespace/reference audits. This deterministic success does **not** validate installed launcher rendering, OEM masks, themed icon rendering or Android-version behavior.

## References

- Android Developers, [Configure Adaptive Icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)
- IVAI, [UI/UX execution blueprint](/home/ubuntu/ivai_uiux_execution_blueprint_2026-08-21.md)
