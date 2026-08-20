# Phase 7.5 — Field Handoff Audit — 20 August 2026

> **Status:** Deterministic preparation verified. No participant, heuristic, device, accessibility, network, signing, or release result is recorded here.

## Objective

This audit makes the controlled Phase 7.5 handoff operational on a clean debug build without introducing product runtime fixtures, credentials, network requests, file writes, telemetry, a backend, or an Alpha artifact. It complements the [authoritative UX validation protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md), the [facilitator runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md), and the [field kit](PHASE7_5_FIELD_KIT.md).

## Confirmed Handoff Gap and Resolution

A clean controlled build intentionally starts with no Provider, account, model, Combo, Agent target, project file, pending approval, or failure state. This preserves the no-credential/no-network/no-write research boundary, but the safety-comprehension prompts for one-time write review and failed-target recovery require hypothetical context.

The resolution is not a seeded runtime state. Instead, the package now carries static [controlled scenario cards](PHASE7_5_CONTROLLED_SCENARIO_CARDS.md). A facilitator gives one generic card at a time without route hints. The cards establish hypothetical context while leaving the application empty and local-only. They are checksum-covered artifacts and the package verifier rejects a handoff that omits them or their non-product-state boundary.

| Handoff element | Verified deterministic condition | Human/device result implied? |
|---|---|---|
| Debug package | Research helper requires clean source, JDK 21, Android SDK, build, unit tests, and lint. | No |
| Artifact provenance | Manifest records commit and debug-only/non-public boundary; checksum inventory covers every transferred artifact. | No |
| Worksheet | Starts blank and instructs de-identified local-only recording. | No |
| Scenario cards | Static facilitator-only context for five tasks; no app data is created. | No |
| Package verifier | Required inventory, manifest identity, worksheet boundary, scenario-card boundary, and checksum match are fail-closed. | No |
| Verifier regression | Synthetic fixture passes; a checksum-tampered artifact is rejected. | No |

## Facilitator Sequence

The facilitator must execute the following sequence for each actual session. No row becomes Pass until it is observed in reality and recorded only in permitted de-identified form.

| Order | Required action | Safe completion condition |
|---:|---|---|
| 1 | Verify the received package with `scripts/verify_phase75_research_package.sh <package-dir>`. | Validator succeeds; manifest commit and debug-only boundary match the intended build. |
| 2 | Confirm `SHA256SUMS.txt` and inspect the blank worksheet and static scenario cards. | All checksums match; worksheet contains no prior result; cards are not treated as product screens. |
| 3 | Clear local application data before the session. | No credential, Provider, endpoint, model, Combo, Agent target, project file, or prior chat state remains. |
| 4 | Read the voluntary/local-only opening statement and assign an anonymous ID. | Consent acknowledgement only; no identity mapping enters the repository. |
| 5 | Present randomized task prompts and one matching scenario card at a time. | No route hint before a first attempt; no real state is entered into IVAI. |
| 6 | Stop on an unsafe proposed action or P0/P1 observation. | No credential, live request, endpoint connection, file write, or hidden retry is initiated. |
| 7 | Run the compact/medium device matrix separately. | Only non-sensitive configuration and outcome evidence is retained. |
| 8 | Transfer approved aggregate findings to the validation record after review. | No names, device identifiers, credentials, prompts, files, traces, recordings, or raw logs are transferred. |

## Gate Status

| Gate | State after this audit | Closure requirement |
|---|---|---|
| Deterministic research-package preparation | Ready | Rebuild/verify from the final intended commit before any session. |
| Scenario material for five prompts | Ready | Use facilitator-only cards; no runtime state is seeded. |
| Usability, card sort, tree test, safety comprehension | Pending | Real voluntary, de-identified outcomes; thresholds in the protocol. |
| Independent heuristic review | Pending | Two independent reviews and reconciliation; no unresolved P0/P1. |
| Compact/medium, Force-RTL, TalkBack, lifecycle, layout | Pending | Real-device evidence on the same controlled build. |
| HTTPS loopback/private-LAN cancellation/timeout | Pending | Explicit HTTPS-only physical-device evidence without a trust bypass. |
| Signed Alpha release | Blocked | All field gates, deterministic rerun, owner-controlled signing, checksum, tag, reviewed notes, and owner approval. |

## Boundary

This audit authorizes neither a feature phase nor a public release. Runtime Provider/Model test actions remain blocked by the Phase 7.5 and first Alpha gates. A successful package verifier or completed worksheet is provenance preparation, not evidence of usability, accessibility, safety comprehension, or device behavior.

## References

[1] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[2] [Phase 7.5 Usability and Heuristic Review Runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md)

[3] [Phase 7.5 Controlled Research and Device Field Kit](PHASE7_5_FIELD_KIT.md)

[4] [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md)
