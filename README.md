# IVAI — Local-first, Backendless, BYOK AI Agent Harness for Android

> **Release status — pre-Alpha.** IVAI is buildable, continuously validated, and prepared for controlled Phase 7.5 field research. It is **not approved for a public Alpha release**. No public binary, owner-signed artifact, tag, or GitHub Release exists. The current deterministic evidence is clean at **160 unit tests, zero failures/errors/skips, and zero lint issues**; real usability, device, accessibility, network, signing, and owner-approval gates remain open. See the [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md), [Alpha Release Policy](docs/ALPHA_RELEASE.md), and [Phase 7.5 Deterministic Hardening Audit](docs/PHASE7_5_DETERMINISTIC_HARDENING_AUDIT_2026-08-21.md).

IVAI is an Android harness for configuring and executing bounded AI-agent workflows using **connections and credentials controlled by the user**. It is not a single-provider client, hosted agent service, central backend, or telemetry product.

## Product principles

| Principle | Current IVAI behavior |
|---|---|
| **Local-first** | Workspace, chats, provider metadata, router attempts, Agent profiles, runs, trace steps, and approvals are stored locally in Room. Project files remain in app-private storage. |
| **Backendless** | IVAI has no central server, proxy, mandatory account, cloud sync, or default analytics pipeline. |
| **BYOK** | The user provides credentials. Room stores opaque references only; encrypted credential material is controlled by an Android Keystore-backed vault. |
| **Provider-neutral** | Gemini is a proof streaming adapter, not a default or required provider. Users explicitly manage Gemini, OpenRouter, and Custom OpenAI-compatible HTTPS connections. |
| **Explicit targets** | Users choose a Provider/Account/Model target or an ordered local Combo. IVAI never silently selects a provider or model. |
| **Bounded agents** | Agent runs apply local step, tool-call, and runtime limits, with a reviewable local trace. |
| **Approval-first writes** | A project-file write requires a visible preview and one explicit **Allow once** decision. There is no always-allow mode or automatic replay after restart. |

The supplied **VA** mark is Android launcher artwork only. Product UI uses an independent IVAI wordmark and a semantic indigo/emerald/aqua/aurora-violet design system; visual branding never affects provider selection, data ownership, or execution controls.

## Current capability surface

### Provider, chat, and routing harness

The local registry supports user-managed Connections, Accounts, Models, credential references, enable/disable controls, and manual capability metadata. Chat can run against one explicit configured Model or an ordered Combo. The sequential router performs capability matching and bounded fallback before any user-visible output; it records a local, redacted attempt trace. A terminal provider-stream event is authoritative: malformed adapter output after `Completed`, `Failed`, or `Cancelled` is not accepted by direct or routed chat.

Custom OpenAI-compatible endpoints are user supplied and HTTPS-only. Explicit local-device and private-LAN HTTPS modes are modeled separately. HTTP, `.local`/mDNS discovery, scanning, bundled inference, hidden defaults, and automatic network operations are out of scope.

### Bounded local Agent

Agent profiles attach only to valid, enabled, user-managed Direct Model or Combo targets. The current safe tools are calculation, current time, and bounded project-bound read/list/literal-search; project-file writes require a bounded preview and explicit approval. Runs, steps, approvals, limits, cancellation, failures, and terminal state are persisted locally. During foreground recovery, unresolved write approvals are denied and interrupted runs stop safely; write payloads are deliberately not persisted across process death.

### Data and security

Room schema history is exported in `app/schemas/`. Migration and recovery coverage includes a legacy v1 fixture through the current schema, endpoint-trust and authentication-mode defaults, and the durable marker for a visible assistant partial interrupted before stream completion. Local export/import is versioned and secret-free. App-private workspace paths reject unsafe traversal. User-visible provider diagnostics avoid raw credentials, authorization headers, model reasoning, and unredacted secrets.

## Deliberate Alpha boundaries

IVAI does **not** include Shell/Termux execution, Shizuku, Accessibility automation, unrestricted storage access, unrestricted HTTP tools, MCP process/server execution, autonomous background agents, multi-agent swarms, local-model inference, cloud backup, multi-device sync, voice, public app-store distribution, or a central IVAI backend. Future Skills and MCP work is planned as a separately gated architecture track; see [Skills and MCP Future Architecture](docs/SKILLS_MCP_FUTURE_ARCHITECTURE.md).

## Architecture

```text
Compose UI
  -> WorkspaceViewModel / local UI state
  -> LocalWorkspaceRepository
  -> Room database | app-private project workspace | Keystore-backed secret vault
  -> user-managed provider adapters and endpoint trust policy
  -> foreground, user-initiated provider request only
```

Read [Architecture](docs/ARCHITECTURE.md), [Security](docs/SECURITY.md), and [Provider Harness Alignment](docs/PROVIDER_HARNESS_ALIGNMENT.md) before changing a provider, network, persistence, runtime, or security boundary.

## Build and validate

### Prerequisites

Use **JDK 21** and an Android SDK containing `platforms;android-37.1` and `build-tools;37.0.0`. The repository currently uses Android Gradle Plugin **9.3.1**, Gradle **9.7.1**, Kotlin **2.4.10**, KSP **2.3.11**, Compose BOM **2026.08.00**, Core **1.19.0**, and Lifecycle **2.11.0**. Do not commit machine-local `local.properties`.

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
```

Run the deterministic local gate before opening a pull request:

```bash
bash scripts/check_android_sdk_provisioning_contract.sh
bash scripts/test_android_sdk_provisioning_contract.sh
bash scripts/test_phase75_research_package_verifier.sh
bash scripts/test_release_candidate_package_verifier.sh
bash scripts/test_owner_signed_release_evidence_helper.sh
bash scripts/check_provider_neutral_branding.sh
bash scripts/check_rtl_bounded_exceptions.sh
bash scripts/check_phase80_architecture_readiness.sh
bash scripts/check_provider_model_test_readiness.sh
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

The debug APK at `app/build/outputs/apk/debug/app-debug.apk` is a development artifact, **not** a signed Alpha release artifact.

### Build a local unsigned Release Candidate evidence package

Only a clean checkout may create a candidate package. The helper builds a minified unsigned release APK, debug APK, mapping file, test/lint reports, checksums, and security/repository evidence. It does not sign, tag, upload, or publish anything.

```bash
./scripts/prepare_release_candidate.sh /tmp/ivai-release-candidates
./scripts/verify_release_candidate_package.sh \
  /tmp/ivai-release-candidates/<version>-rc-<short-commit>
```

The candidate package is internal provenance evidence only. Review [Release Candidate Preparation](docs/RELEASE_CANDIDATE_PREPARATION.md) and [Alpha Release Policy](docs/ALPHA_RELEASE.md) before any owner-controlled signing step.

## Documentation map

| If you need to… | Start here |
|---|---|
| Understand product scope, phases, and deferred gates | [Roadmap](docs/ROADMAP.md) |
| Understand local data, vault, provider, and Agent boundaries | [Architecture](docs/ARCHITECTURE.md) and [Security](docs/SECURITY.md) |
| Prepare or verify an unsigned candidate | [Release Candidate Preparation](docs/RELEASE_CANDIDATE_PREPARATION.md) |
| Review Alpha gates and signing/publication boundaries | [Alpha Release Policy](docs/ALPHA_RELEASE.md) and [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md) |
| Run controlled usability/device research | [Phase 7.5 Field Kit](docs/PHASE7_5_FIELD_KIT.md) and [Validation Protocol](docs/PHASE7_5_UX_VALIDATION_PROTOCOL.md) |
| Understand the next Skills/MCP architecture direction | [Skills and MCP Future Architecture](docs/SKILLS_MCP_FUTURE_ARCHITECTURE.md) |
| Browse the complete document index | [Documentation Index](docs/README.md) |

## Contribute, report, and support

Use focused branches and pull requests; direct pushes to `main` are not the normal workflow. The protected branch requires one approval and the Secret scan plus Android quality gate. Read [Contributing](CONTRIBUTING.md) for scope, validation, and review expectations.

Do **not** create a public issue for a suspected vulnerability, credential exposure, unsafe execution path, or release-integrity problem. Follow the private reporting process in [Security Policy](SECURITY.md). Community expectations are in the [Code of Conduct](CODE_OF_CONDUCT.md).

## Release status

| Area | Status |
|---|---|
| Local-first data, encrypted credential boundary, workspace isolation | Complete for current Alpha scope; hardening continues. |
| Provider registry, explicit targets, adapters, direct Model and Combo routing | Complete for current Alpha scope; no automatic provider/model selection. |
| Bounded local Agent, local trace, cancellation, recovery, and one-time write approval | Complete for current Alpha scope. |
| Deterministic build, test, lint, CI, unsigned RC, and owner-signing handoff | Ready and continuously verified; no signed/public artifact has been produced. |
| Phase 7.5 usability, heuristic, device, RTL, TalkBack, and real HTTPS evidence | Pending; no participant or device result is claimed without recorded evidence. |
| Owner-controlled signing, annotated tag, GitHub Alpha Release, and public binary | Pending; blocked until every external gate is complete and the owner approves one exact candidate commit. |

## License

This repository is licensed under the [Apache License 2.0](LICENSE).
