# Phase 6 Hardening Readiness Audit

**Scope:** Post-PR #21 review of the `main` branch, followed by the IVAI visual identity update.

## Verified alignment

IVAI remains a **Local-first, Backendless, BYOK Agent Harness**. Provider connections, accounts, endpoint classes, models, capabilities, Combos and Agent targets remain user-managed local records. The review found no fixed provider target, central backend, embedded credential, automatic provider connection, background network discovery or unrestricted Agent capability.

| Review area | Evidence and outcome | Status |
|---|---|---|
| Provider-neutral execution | Registry-derived Direct Model and Combo paths remain the only execution targets. Provider catalog metadata does not create a connection, select a model, or make a request. | Verified |
| Local endpoint trust | Room v5 persists `REMOTE_HTTPS`, `LOCAL_LOOPBACK_HTTPS` or `LOCAL_LAN_HTTPS`; exact HTTPS endpoint classification, confirmation and no-auth transport are enforced and tested. | Verified |
| Branding asset | The user-provided VA mark is now the launcher asset for all legacy density buckets and Android adaptive icons, and is exposed in the Compose top bar with a content description. | Implemented |
| Theme consistency | Platform launch bars and Compose light/dark palettes now use the indigo, emerald/aqua and violet identity derived from the approved brand reference. | Implemented |
| Visual regression | The Robolectric/Roborazzi main-screen test asserts that the visible brand logo and its semantic content description are present before its capture. | Implemented |
| Documentation accuracy | The roadmap and architecture no longer describe Room v4 or local endpoints as wholly blocked; the remaining limits are explicitly stated. | Corrected |

## Findings intentionally deferred to the next hardening phase

| Priority | Finding | Why it is not closed in this branding change | Required evidence or implementation |
|---|---|---|---|
| P0 | The application shell and `IvaiTheme` currently provide `LayoutDirection.Ltr`; narrower LTR overrides also exist in Markdown/code-oriented renderers. | Changing global direction affects navigation, drawers, list placement, touch targets and mixed BiDi rendering. Some narrow LTR handling is appropriate for code/protocol blocks, so this must be handled as a focused RTL/accessibility change rather than hidden in a visual-branding commit. | Remove global forced-LTR behavior while retaining only demonstrably necessary code/protocol overrides; capture Force-RTL and mixed Persian/English semantics/screenshot evidence. |
| P0 | No physical-device release matrix exists. | Robolectric verifies code paths and screenshots but not device launcher masks, Android system bars, real TLS local-server behavior, rotation, offline behavior or OEM-specific rendering. | Fresh install, upgrade, restart, rotation, offline, local HTTPS loopback/LAN, cancellation and timeout checks on the agreed API/device matrix. |
| P0 | No signed release artifact or checksum exists. | A debug build is not a public release artifact. | Owner-approved signing workflow, reproducible release build command, SHA-256, source tag, release notes and rollback guidance. |
| P1 | The adaptive launcher icon intentionally does not declare a monochrome variant. | A generic monochrome treatment would not faithfully preserve the approved VA artwork. | Approve a distinct single-color VA vector mark before enabling Android themed-icon monochrome resources. |
| P1 | HTTP local server and `.local`/mDNS discovery are out of scope. | Enabling these capabilities would broaden Android network trust and needs a separate threat model. | Separate approval, Network Security Configuration review and physical-device evidence; do not add a broad cleartext exception. |

## Next phase: evidence-driven Alpha hardening

The next development phase should not introduce additional providers or Agent tools. It should first define an executable device and accessibility matrix, then close the P0 evidence gaps in this order: remove only global forced-LTR behavior and capture RTL/BiDi semantics/screenshots; fresh-install/upgrade/restart/rotation evidence; foreground local HTTPS cancellation/timeout/offline evidence; and finally owner-controlled release signing and release artifacts. The public Alpha release remains blocked until the gates in [ALPHA_RELEASE.md](ALPHA_RELEASE.md) are satisfied.

## Follow-up Resolution — Phase 6.1

The P1 monochrome launcher finding was resolved in [PR #39](https://github.com/ILIV007/IVAI-App/pull/39), squash-merged to `main` as `08115c9`. The increment supplies a dedicated single-color vector resource through the `<monochrome>` layer of both adaptive launcher entries and adds a regression test for the resource wiring. The original VA bitmap, legacy icons, and all product UI remain unchanged. This closes the deterministic lint/resource gap only; themed-icon OEM rendering remains part of the physical-device matrix and does not alter the P0 Alpha release blockers above.
