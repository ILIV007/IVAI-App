# Compatibility Visual Review — 2026-08-19

## Scope

This review covers the recorded Roborazzi shell baseline generated after upgrading the test-only Roborazzi dependency to `1.72.0`, together with stable Activity Compose and Navigation Compose candidates. It is deterministic screenshot evidence only; it does not replace device, TalkBack, Force-RTL, font-scale, IME, or Alpha evidence.

## Artifact

| Artifact | Configuration | Result |
|---|---|---|
| `app/src/test/screenshots/ivai_main.png` | Empty Chat shell, light theme, fixed Robolectric test configuration | Accepted as the initial tracked visual baseline after record-mode generation and normal verification-mode comparison both passed. |

## Observations

The rendered shell preserves the sidebar opener, IVAI wordmark, Chat context chip, explicit execution-target prompt, offline-safe empty state, clear local-conversation affordance, and the `Do anything…` composer placeholder. Content hierarchy remains readable and no overlap, clipping, unexpected bottom navigation, stale template control, Provider default, or visual corruption was observed in this deterministic artifact.

## Decision

The baseline is included so future dependency and UI changes receive a concrete visual comparison. The review found no visual regression attributable to the candidate Activity, Navigation, or Roborazzi updates. No production UI code or visual baseline was changed to accommodate the dependencies.

## Deferred

Physical-device screenshots, TalkBack traversal, Force-RTL, dynamic font-scale, keyboard/IME, rotation, offline, and endpoint cancellation testing remain release gates and are not represented by this artifact.
