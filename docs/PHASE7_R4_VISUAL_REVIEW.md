# Phase 7 R4 — Visual Review

## Evidence

The focused Compose regression test recorded two Roborazzi artifacts on the Pixel 8 test configuration:

| Artifact | Reviewed state | Finding |
|---|---|---|
| `app/build/roborazzi/r4_add_account_dark.png` | Existing remote HTTPS Connection with the Add Account sheet open | The dimmed Connections Hub remains visible behind a clear, scroll-safe bottom sheet. The sheet has a strong heading, concise non-automatic-operation disclosure, readable Account label and API key fields, a visible final save action, and a separate Cancel action. Dark-theme contrast and primary action affordance are visually clear. |
| `app/build/roborazzi/r4_add_model_dark.png` | Existing remote HTTPS Connection with the Add Model sheet open | The Model ID field is visually prominent, followed by a clear capabilities hierarchy. The no-discovery/no-auto-selection/no-auto-contact disclosure is readable. Capability controls are discrete, full-width touch targets and the sheet remains consistent with the Account sheet and background product shell. |

## Review Outcome

The reviewed dark-state layouts are coherent with the indigo/emerald/violet IVAI system and preserve the user-visible local-control boundary. No clipping, bottom-navigation regression, duplicated product sidebar, malformed typography, missing primary action, or unreadable dark-theme hierarchy was observed in these deterministic artifacts.

This review does **not** substitute for physical-device, light-theme, IME, rotation, font-scale, Force-RTL, TalkBack, or real-network evidence. Those remain explicitly deferred Phase 7.5/Alpha gates.
