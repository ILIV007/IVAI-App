# Post-R8 Engineering Audit — 2026-08-19

## Scope

This audit reviewed the current protected `main` baseline after R6 Settings Safety and R8 Provider/Model Test Readiness. It covered repository and CI state, local-first/BYOK architecture boundaries, Provider neutrality, transport and execution policy, Room migration coverage, launcher/resource hygiene, lint, debug/minified-release builds, unit tests, roadmap consistency, and release/field-evidence claims.

> **Audit boundary:** This deterministic review does not replace physical-device validation, participant research, signed-release evidence, or owner approval. It does not authorize R8 runtime Provider/Model tests.

## Verified Baseline

| Area | Evidence | Result |
|---|---|---|
| Protected main | Required Secret scan and Build/unit-test/lint checks; one approval; linear history; admin enforcement | Verified active. |
| CI baseline | Current `main` workflow completed successfully after R8 readiness merge | Verified. |
| Local-first/BYOK | Static scans found no hardcoded keys, cleartext opt-in/trust-all code, prohibited execution, or implicit Provider selection | Verified. |
| Data integrity | Room is schema v6, migration chain v1→v6 has recovery coverage, and no destructive-migration fallback was found | Verified. |
| Destructive reset | Settings now confirms before invoking the existing local reset callback; resetter behavior remains unchanged | Verified by focused R6 regression. |
| Provider/Model test | R8 readiness contract and CI guard are present; runtime test symbols remain absent | Verified. |
| Launcher assets | Safe foreground generator and checksum validator pass; square/round fallback variants remain distinct | Verified. |

## Confirmed Findings and Remediation

| ID | Finding | Risk | Remediation in this audit |
|---|---|---|---|
| AU-01 | The current GitHub Actions run emitted Node 20 and `setup-java@v4` deprecation warnings. | CI maintenance and future workflow compatibility. | Updated checkout to `actions/checkout@v5`, setup-java to `actions/setup-java@v5`, and Gitleaks to `gitleaks/gitleaks-action@v3`. The selected upstream action metadata uses Node 24. |
| AU-02 | The approved launcher source composition lived inside `res/drawable-nodpi`, so Android packaged an unused generator input and lint reported it as unused. | APK hygiene and recurring lint noise. | Moved the source composition to `scripts/assets/` and updated `render_launcher_assets.py`. Generated launcher outputs, their dimensions, and reviewed checksums remain unchanged. |

## Validation After Remediation

| Gate | Result |
|---|---|
| Launcher generator and safe-zone validator | Pass. |
| Semantic contrast audit | Pass; all audited pairs meet 4.5:1 or better. |
| Provider-neutral, bounded RTL, Phase 8.0, R8 readiness, Phase 7.5 package, and release-candidate guards | Pass. |
| Secret, transport, prohibited-execution, and implicit-selection static scan | Pass. |
| `assembleDebug` and `assembleRelease` with R8 | Pass. |
| Unit suite | 155 tests; 0 failures, 0 errors, 0 skipped. |
| `lintDebug` | 0 errors/fatals; 15 warnings. |
| Whitespace check | Pass. |

## Deferred Findings

| ID | Remaining item | Why it is deferred |
|---|---|---|
| AU-03 | Fourteen `NewerVersionAvailable` warnings for Gradle/AGP, Compose/Kotlin, AndroidX, Navigation, Lifecycle, and Roborazzi. | A broad synchronized toolchain/dependency upgrade has compatibility, Kotlin/Compose compiler, AGP/Gradle, screenshot-test, and release-build risk. It must be a dedicated compatibility phase with its own version matrix and migration validation, not an opportunistic audit change. |
| AU-04 | One `ObsoleteSdkInt` warning for `mipmap-anydpi-v26`. | The v26 adaptive icon XML intentionally coexists with reviewed density-specific square/round fallback launchers. Moving the XML to `mipmap-anydpi` causes `IconMixed` lint warnings because density-independent XML and bitmap fallback variants then collide. The safe fallback contract is retained pending a dedicated launcher/minSdk policy decision. |
| AU-05 | Node 20 deprecation notices may persist until every third-party action execution path is fully refreshed by the runner ecosystem. | The audited first-party checkout/setup-java and Gitleaks action references were upgraded. The next protected CI run is the authoritative validation of remaining runner notices. |
| AU-06 | Physical-device, accessibility, network cancellation/timeout, participant research, signed Alpha APK, SHA-256, release notes, annotated tag, and owner approval. | These are explicit Phase 7.5/Alpha evidence gates and cannot be fabricated or replaced by sandbox/CI results. |

## No Runtime-Scope Drift

The audit introduced no Provider, Account, Model, Combo, Agent, Room schema, Vault, network policy, cleartext exception, backend, telemetry, scheduler, Skills/MCP runtime, transport behavior, authentication flow, or R8 runtime test action. The explicit Provider/Model test feature remains documentation-and-CI guarded until its real gates are complete.
