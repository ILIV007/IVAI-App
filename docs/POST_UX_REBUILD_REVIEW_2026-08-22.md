# Post-UX-Rebuild Engineering Review — 22 August 2026

> **Status:** Deterministic review complete. This record does **not** claim physical-device, accessibility-service, network, signing, participant, or release validation.

## Scope and Baseline

This review evaluates the UI/UX rebuild after UX-1 through UX-7 and the UX-8 preparation handoff. The reviewed `main` baseline is `fbca66b39b126fba6ada379bf60bcf33e011852e`, which records the preparation-only UX-8 evidence. The repository working tree was clean at review start, and the protected-branch policy required one approval, linear history, admin enforcement, Secret scan, and Build/unit-test/lint checks.

The review intentionally separates **verified deterministic facts** from work that requires a controlled package and a real Android device. It does not change Provider/Router/Agent/Data runtime behavior, Room schema, vault behavior, endpoint policy, network policy, telemetry, signing, or release state.

## Verified Deterministic Results

| Area | Review result | Evidence |
|---|---|---|
| Git and governance | `main` was clean; required checks, one approval, enforced admins, and linear history were active. | Protected branch configuration and merged PR history reviewed. |
| CI quality baseline | The UX-8 preparation and evidence PRs passed Secret scan and Build/unit-test/lint. The workflow provisions Android API 37.1, performs architecture guards, debug/minified-release builds, unit tests, and Android lint. | [1] [2] |
| Local-first / BYOK | Source scans found no hard-coded secret pattern, cleartext/trust bypass, prohibited execution/background API, provider default, target auto-selection marker, or global forced-LTR root. | Source scan recorded in this review; provider registry and vault source were manually inspected. |
| Provider neutrality | Presets are local metadata only; saving remains explicit, custom endpoints are HTTPS-gated, and adapter resolution requires an explicit `ProviderKind`. | [3] |
| Credential boundary | Room stores a credential reference; `EncryptedSecretVault` stores encrypted payloads and `AndroidKeystoreSecretCipher` uses per-reference AES/GCM Android Keystore aliases. | [4] [5] [6] |
| Schema and migration | The source database is at schema version 6 with migrations through `5 → 6`; repository documentation contains no stale `Room v5` claim. | [7] |
| UI quality guards | Provider-neutral branding, bounded RTL exceptions, Phase 8 readiness, Provider/Model-test gating, research/RC package verifier regressions, launcher safe-zone validation, and semantic contrast check passed locally. The contrast verifier reported all audited pairs at or above 4.5:1. | [2] |
| UI rebuild evidence | UX-1 through UX-7 and their independent evidence records are merged; UX-8 has a preparation-only controlled operator matrix. | [8] [9] |

## Findings and Disposition

| Severity | Finding | Disposition |
|---|---|---|
| P0 | No confirmed P0 code, security, provider-default, local-first, or release-governance defect found by this review. | No production change warranted. |
| P1 | No confirmed P1 deterministic accessibility/UX regression found. | Physical TalkBack, IME, Force-RTL, font-scale and device matrix are still required before a P1-free field conclusion is possible. |
| P2 | No confirmed P2 code defect found in audited scope. | Continue to record only actual field findings if they occur. |
| P3 | The development playbook outside this repository still describes a Room v5 schema while source is v6. This is tooling-document drift, not an app/repository defect. | Keep separate from product changes; correct only through the dedicated skill-maintenance workflow if that material is revised. |
| Maintenance | Dependabot PRs [#122](https://github.com/ILIV007/IVAI-App/pull/122) and [#123](https://github.com/ILIV007/IVAI-App/pull/123) each have green checks but are old, review-required workflow-major updates with unknown merge state. | Do not mix them into UX/release work. Rebase/review/merge only in a dedicated CI-dependency increment. |

## Remaining Release Blockers

The following gates remain **pending**, not failed or waived:

| Gate | Required evidence before closure |
|---|---|
| UX-8 device matrix | Actual controlled observations on compact and medium devices, including launcher/OEM behavior, sidebar layout, light/dark, font scale, rotation, restart, and local-data confirmation. |
| Accessibility | Real TalkBack linear/explore navigation and Force-RTL/mixed-script outcomes for task-critical controls. |
| Chat interaction | Keyboard/IME open-close, IME action/hardware Enter when available, portrait/landscape, and Send/Stop visibility with a real device. |
| Network safety | Offline and approved loopback/private-LAN **HTTPS** cancellation/timeout observations; no real credential, HTTP, scanning, or trust bypass. |
| Research and usability | Voluntary de-identified card-sort, tree-test, safety-comprehension, and heuristic outcomes according to the established protocol. |
| Release control | Owner-controlled signing, checksum, release notes, tag, and explicit Alpha approval after all P0/P1 field findings are closed. |

> The controlled [UX-8 Physical Validation Handoff](PHASE7_UX8_PHYSICAL_VALIDATION_HANDOFF.md) is ready for this work. Its existence and its green documentation CI do not constitute any row of the physical matrix.

## Decision

No source-code remediation is created from this review because none is supported by a confirmed deterministic finding. The correct next action is **controlled real-device validation**, not speculative UI/runtime change. Phase 7 redesign and public Alpha remain **not approved**.

## References

[1]: https://github.com/ILIV007/IVAI-App/pull/138 "PR #138 — UX-8 physical validation handoff"
[2]: https://github.com/ILIV007/IVAI-App/blob/main/.github/workflows/android-quality.yml "Android quality workflow"
[3]: https://github.com/ILIV007/IVAI-App/blob/main/app/src/main/java/dev/iliv007/ivai/provider/ProviderRegistry.kt "Provider registry and endpoint policy"
[4]: https://github.com/ILIV007/IVAI-App/blob/main/app/src/main/java/dev/iliv007/ivai/data/local/WorkspaceEntities.kt "Room provider account entity"
[5]: https://github.com/ILIV007/IVAI-App/blob/main/app/src/main/java/dev/iliv007/ivai/security/EncryptedSecretVault.kt "Encrypted Secret Vault"
[6]: https://github.com/ILIV007/IVAI-App/blob/main/app/src/main/java/dev/iliv007/ivai/security/AndroidKeystoreSecretCipher.kt "Android Keystore secret cipher"
[7]: https://github.com/ILIV007/IVAI-App/blob/main/app/src/main/java/dev/iliv007/ivai/data/local/IvaiDatabase.kt "Ivai Room database schema"
[8]: https://github.com/ILIV007/IVAI-App/blob/main/docs/ROADMAP.md "IVAI roadmap"
[9]: https://github.com/ILIV007/IVAI-App/blob/main/docs/PHASE7_UX8_PHYSICAL_VALIDATION_HANDOFF.md "UX-8 physical validation handoff"
