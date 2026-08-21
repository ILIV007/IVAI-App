# Phase 7.5 — Deterministic Hardening Audit — 21 August 2026

> **Scope:** This record documents only deterministic repository and CI evidence for the `main` commit shown below. It records no participant outcome, physical-device result, accessibility-service result, HTTPS endpoint result, signing result, or release approval.

## Baseline and Decision

The audit was run against `main` commit `a0f4436ec517ab2da0fe573f3fd43d10505c55d7`, which contains the controlled Phase 7.5 scenario-card handoff. The protected-main CI run completed successfully, including Secret scan and Build/unit-test/lint.[1] The deterministic decision is **Ready for controlled field handoff; not approved for Alpha**.

| Gate | Deterministic result | Scope boundary |
|---|---|---|
| Source worktree and protected CI | Clean main baseline; CI succeeded. | Does not prove install, upgrade, restart, rotation, or offline behavior on a physical device. |
| Debug/release build and quality suite | Current quality evidence reports 160 unit tests with zero failures, errors, and skipped tests; lint is clean. | Does not replace accessibility or usability evidence. |
| Secret and credential signatures | No hardcoded credential signature was found by the focused source scan. | This is not a substitute for owner-controlled signing-key handling. |
| Transport and trust | No cleartext, trust-all, hostname-verifier, or implicit-provider signature was found in the focused source scan. | HTTPS loopback/private-LAN cancellation and timeout still require real-device evidence. |
| Local-first execution boundary | No shell/process, accessibility-service, scheduler, or background-work signature was found in the focused source scan. | Does not replace runtime behavior observation. |
| BiDi scope | No global forced-LTR wrapper was found in the app shell or theme. | Force-RTL and TalkBack remain real-device gates. |
| No-transfer policy | `allowBackup="false"`, legacy full-backup exclusions, and Android 12+ cloud/device-transfer exclusions are regression-protected. | Device-to-device behavior still requires physical-device verification. |
| Research package contract | The Phase 7.5 verifier regression passed, including expected rejection of a checksum-tampered artifact. The handoff package requires a blank worksheet and facilitator-only scenario cards. | A verified package is provenance preparation, not a study result. |
| Unsigned RC contract | The Release Candidate verifier regression passed, including expected rejection of a checksum-tampered artifact. The candidate remains unsigned and non-public. | No signed APK, tag, GitHub Release, or owner approval exists. |

## Scan Method and Results

The audit used repository-local scans for hardcoded secret patterns, cleartext/trust bypass patterns, prohibited execution/background work, implicit provider selection, and global forced-LTR wrappers. Every focused scan was clean. The audit also reviewed the current CI SDK provisioning and the Release Candidate helper contract. CI provisions `platforms;android-37.1` with `build-tools;37.0.0`; the Release Candidate helper defaults to the same build-tools version and fails closed when the required `apksigner` is unavailable.[2]

The no-transfer policy test validates the Android manifest flag and the exact exclusion sets for both Android 12+ extraction paths and Android 11-and-lower full backup rules. It guards application root, files, databases, shared preferences, external storage, and their device-protected counterparts.[3]

## Verified Deferred Gates

The following gates intentionally remain **Pending** because a repository, JVM, screenshot, debug package, or CI workflow cannot prove them. No status in this audit may be used to infer a pass for them.

| Deferred gate | Required evidence |
|---|---|
| Usability and IA | Voluntary, de-identified card-sort, tree-test, and safety-comprehension observations under the controlled protocol. |
| Heuristic review | Independent reviews of Chat, Agents, Workspace, Connections, and Settings; reconciliation with no unresolved P0/P1 issue. |
| Physical devices | Compact and medium device matrix covering install/upgrade/restart/rotation, layout, font scale, dark/light, offline, and reset behavior. |
| Accessibility and BiDi | Force-RTL plus TalkBack swipe and explore-by-touch evidence on real hardware. |
| HTTPS behavior | Explicit local HTTPS loopback/private-LAN cancellation and timeout evidence without cleartext, discovery, or trust bypass. |
| Alpha signing and publication | Owner-controlled signed APK, checksum, annotated tag, reviewed notes, GitHub Release, independent download/hash check, and explicit owner approval. |

## Follow-up Rule

The next autonomous work may only harden deterministic scripts, tests, architecture scans, reproducibility, and documentation. It must not add a Provider runtime test, mock production state, telemetry, backend, credential, signing material, public artifact, or Phase 8 runtime capability until every external gate in the Phase 7.5 protocol and Alpha checklist is actually closed.

## References

[1]: https://github.com/ILIV007/IVAI-App/actions/runs/32374729998 "Android quality CI run for main"
[2]: ../.github/workflows/android-quality.yml "Android quality workflow"
[3]: ../app/src/test/java/dev/iliv007/ivai/ExampleRobolectricTest.kt "No-backup policy regression test"
[4]: PHASE7_5_UX_VALIDATION_PROTOCOL.md "Phase 7.5 UX Validation Protocol"
[5]: RELEASE_READINESS_CHECKLIST.md "Release Readiness Checklist"
