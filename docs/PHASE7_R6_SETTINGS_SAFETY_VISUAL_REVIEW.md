# Phase 7 R6 — Settings Safety Visual Review

## Evidence

The focused confirmation regression recorded `app/build/roborazzi/r6_delete_confirmation_dark.png` on the Pixel 8 Roborazzi configuration.

| Reviewed element | Finding |
|---|---|
| Destructive hierarchy | The darkened Settings page stays visible behind a clear modal dialog headed **Permanently delete local data?**. The warning makes the consequence obvious before any callback is invoked. |
| Disclosure | The dialog lists only local workspace project files, local database records, and encrypted stored provider credentials. It expressly excludes remote provider data, system backups, external files, and other-app data. |
| Action distinction | **Cancel** is a visible, non-destructive text action. **Delete permanently** is visually differentiated as the explicit destructive confirmation action. |
| Scope honesty | The confirmation does not claim an undo, export, backup, cloud reset, provider request, account deletion, automatic completion, or reset success. |

## Review Outcome

The R6 dialog is legible in the reviewed dark state, preserves the IVAI indigo/emerald/violet system, and makes the safety decision understandable without adding extra authentication or a misleading recovery promise. Deterministic regression verifies that the initial delete button does not invoke the callback, Cancel does not invoke it, and explicit confirmation invokes it once.

This visual review does **not** replace physical-device font-scale, TalkBack, Force-RTL, light-theme, rotation, lifecycle, or fresh-install evidence. Those remain Phase 7.5/Alpha gates.
