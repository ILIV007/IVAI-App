# IVAI — Local-first BYOK Agent Harness for Android

> **Alpha status:** The repository is buildable and continuously validated, but a public GitHub Alpha release has not been created yet. IVAI is intentionally local-first, backendless, and BYOK; it is **not** a single-provider client or a hosted agent service. The current release decision, exact gates, and deterministic audit are documented in the [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md) and [Release Readiness Audit](docs/RELEASE_READINESS_AUDIT_2026-08-17.md). The latest [provider-neutral repository cleanup](docs/PROVIDER_NEUTRALITY_CLEANUP_2026-08-18.md) records the removed nonfunctional branding and retained technical adapter/build references.

IVAI is an Android harness for configuring, routing, and running bounded AI-agent workflows using connections and credentials that remain under the user's control. The user owns the provider connection, account, endpoint, model, capability metadata, ordered fallback Combo, chat execution target, Agent profile, and project workspace. The app creates no central IVAI account, mandatory backend, telemetry service, or implicit provider configuration.

The supplied **VA** brand mark is reserved for the Android launcher only. The product UI uses an independent IVAI wordmark and an indigo ground with emerald/aqua and aurora-violet accents; this presentation layer does not alter provider selection, local data ownership, or any execution boundary.

## Product principles

| Principle | IVAI Alpha behavior |
|---|---|
| **Local-first** | Workspace, chats, provider metadata, router attempts, Agent profiles, runs, trace steps, and approvals are stored locally in Room. Project files stay in app-private storage. |
| **Backendless** | The application has no IVAI server, proxy, required login, or default analytics pipeline. |
| **BYOK** | Credentials are user supplied and referenced locally. Room stores only credential references; encrypted material is managed through an Android Keystore-backed vault. |
| **Provider-neutral** | Gemini is a streaming proof adapter, not a fixed provider. The user can configure Gemini, OpenRouter, or a Custom OpenAI-compatible HTTPS endpoint. |
| **Bounded agents** | Agent runs are bounded by step, tool-call, and runtime ceilings. Every action is persisted in a reviewable local trace. |
| **Approval-first writes** | A project-file write requires a visible preview and explicit **Allow once** approval. There is no always-allow mode and no automatic replay after restart. |

## What is implemented

### Provider, chat, and router harness

The local provider registry supports user-managed connections, accounts, models, credential references, enable/disable controls, and manual model metadata. The current adapter set contains Gemini, OpenRouter, and Custom OpenAI-compatible adapters. The latter accepts only user-configured HTTPS endpoints. Chat execution can use a direct configured model or an ordered local Combo; the sequential router performs capability matching, controlled fallback, and records a local attempt trace.

### Local Agent Alpha

Agent profiles are attached only to a valid, enabled user-managed Direct Model or Combo. The profile editor intentionally presents registry-derived local targets rather than accepting a free-form provider target. Current safe tools are calculation and current time; project-file writes require a review dialog with a bounded preview. Runs, steps, approvals, limits, cancellation, failures, and terminal states are persisted locally.

After process death, write payloads are intentionally unavailable because they are not persisted. Any unresolved approval is denied during foreground recovery, the interrupted run is stopped safely, and the trace records that no write occurred.

### Data and security

Room schema history is exported under `app/schemas/`. Migration and reopen coverage includes a real legacy v1 fixture upgraded through all current migrations to v5, including endpoint trust and authentication-mode defaults. Local export/import is versioned and secret-free. The app-private workspace rejects unsafe traversal paths. Provider diagnostics and user-visible errors are designed to avoid raw credentials, authorization headers, raw model reasoning, and unredacted secrets.

## Explicit Alpha boundaries

The following capabilities are intentionally **not** part of Alpha: Shell/Termux execution, Shizuku, Accessibility automation, unrestricted storage access, unrestricted HTTP POST tools, MCP process/server execution, autonomous background agents, multi-agent swarms, local model inference, cloud backup, multi-device synchronization, voice, public app-store distribution, or a central IVAI backend.

Safe workspace read/list/search tooling is implemented within the bounded local runtime. The full RTL/accessibility/device evidence matrix remains planned hardening work; see [the roadmap](docs/ROADMAP.md), [the Alpha release checklist](docs/ALPHA_RELEASE.md), and the [Phase 6 hardening readiness audit](docs/PHASE6_HARDENING_AUDIT.md).

## Architecture

```text
Compose UI
  -> WorkspaceViewModel / local UI state
  -> LocalWorkspaceRepository
  -> Room database | app-private project workspace | Keystore-backed secret vault
  -> provider adapters selected from user-managed local registry
  -> foreground, user-initiated provider request only
```

The full architecture and security boundaries are documented in [Architecture](docs/ARCHITECTURE.md), [Security](docs/SECURITY.md), and [Provider Harness Alignment](docs/PROVIDER_HARNESS_ALIGNMENT.md).

## Build and validate

### Prerequisites

Use a full **JDK 21** and an Android SDK that includes platform `36` with minor API level `1`. The repository uses Android Gradle Plugin `9.1.1`, Gradle `9.3.1`, Kotlin/Compose, Room, DataStore, and Material 3. Configure `ANDROID_HOME` or `ANDROID_SDK_ROOT`; do not commit a machine-local `local.properties` file.

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
```

### Local quality gate

Run the same practical Android validation expected by the repository workflow:

```bash
./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
```

Build output is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The debug APK is a development artifact, **not** a signed Alpha release artifact. See the release checklist before publishing any APK or GitHub Release.

## Contribution workflow

Changes are made on focused branches through pull requests. The protected `main` branch requires linear history, one approval, secret scanning, and the Android build/unit-test/lint gate. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a change.

Every change that adds a provider, network behavior, persistent data, file access, Agent capability, or release artifact must describe its safety boundary, migration impact, and validation evidence. Do not add secrets, credentials, or personal workspace data to source control.

## Current hardening status

| Roadmap area | Status |
|---|---|
| Governance and protected CI | Complete |
| Local data, encrypted credential boundary, and workspace isolation | Complete with ongoing hardening |
| Provider registry, explicit cloud preset catalog, adapters, direct target and Combo router | Complete for current Alpha scope; no provider or model is selected automatically |
| User-operated local model server endpoints (Ollama, LM Studio, vLLM, compatible OpenAI-style servers) | Available only as an explicit, user-confirmed **HTTPS** loopback or RFC1918 IPv4 connection; the user chooses endpoint, model and API-key/no-auth mode. HTTP, `.local` discovery, scanning and bundled inference remain out of scope. |
| Bounded Agent with explicit profile tool policy, one-time write approval, trace, budget, cancellation, target validation, and restart recovery | Complete for current Basic Agent scope |
| Safe read/list/search Agent tools | Complete for Alpha scope: app-private, project-bound, bounded, and not persisted in Run Trace |
| Migration and recovery coverage | Complete through Room v6, including persisted endpoint trust/account authentication defaults and an explicit durable marker for visible assistant partials interrupted before stream completion |
| Phase 7.5 deterministic hardening | Complete in [PR #37](https://github.com/ILIV007/IVAI-App/pull/37): explicit no-backup/no-transfer rules and regression coverage are on `main` |
| Voluntary UX research, physical-device RTL/accessibility/network evidence, and signed Alpha release | Pending before a public Alpha release; no participant or device result is claimed without evidence. See the [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md) and [Release Readiness Audit](docs/RELEASE_READINESS_AUDIT_2026-08-17.md). |

## License

This repository is licensed under the [Apache License 2.0](LICENSE).
