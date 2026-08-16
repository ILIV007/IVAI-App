# Architecture

## Current Alpha state

IVAI is a single Android application module built with Kotlin, Jetpack Compose, Material 3, Coroutines, Room, DataStore, and Android Keystore primitives. It is a **local-first, backendless, BYOK Agent Harness**: user-owned local records determine whether any provider adapter can be used. The application has no central IVAI backend, mandatory login, proxy, default telemetry, or implicit provider target.

```text
Compose UI
  -> WorkspaceViewModel and feature state flows
  -> LocalWorkspaceRepository
  -> Room v4 | app-private ProjectWorkspace | encrypted Secret Vault
  -> ProviderAdapterRegistry and foreground provider sessions
  -> user-selected Direct Model or ordered Combo
```

## Canonical boundaries

| Layer | Responsibility | Boundary |
|---|---|---|
| Compose UI | Renders Room-backed workspace, provider, router, and Agent state; sends user actions to the ViewModel. | UI never receives or stores plaintext credentials. |
| `WorkspaceViewModel` | Coordinates local state flows, explicit foreground actions, and safe user-visible errors. | It does not select an implicit provider or execute shell/background automation. |
| `LocalWorkspaceRepository` | Transactional local persistence, registry validation, Router references, Agent target validation, and recovery state. | Room stores credential references, not secret values. |
| `ProjectWorkspace` | App-private project-file isolation with canonical relative-path validation and bounded read/list/search primitives. | No unrestricted external-storage or Shell file access; Agent file operations cannot escape the profile project. |
| `EncryptedSecretVault` | Encrypted credential storage through a per-reference Android Keystore key. | Secrets are excluded from Room, traces, exports, and UI state. |
| Provider preset catalog | Local, reviewable metadata for Gemini, OpenRouter, OpenAI, Groq, Mistral, Together, DeepSeek, Fireworks and xAI setup. | A preset provides no secret, model selection, connection, discovery or automatic network operation; cloud protocol mappings reuse installed adapters. |
| Provider adapters | Gemini, OpenRouter, and Custom OpenAI-compatible foreground request implementations. | A user-managed enabled connection/account/model is required before use. Local-server endpoint transport is intentionally not implemented until its separate trust mode is reviewed. |
| `SequentialRouter` | Capability-aware ordered Combo resolution, controlled fallback, and attempt trace. | No hidden fallback provider is injected. |
| `BasicAgentRuntime` | Bounded local runs, safe tools, approval-first writes, limits, cancellation, and trace. | No always-allow write, automatic post-restart write, Shell, Termux, MCP, or background autonomy. |

## Local data and migration

`IvaiDatabase` is currently schema version `4`. It persists workspace projects, threads, messages, provider registry records, router Combos and attempts, Agent profiles, runs, run steps, and approvals. Schemas are exported under `app/schemas/` and must be committed with every version change. The repository includes a file-backed legacy v1 fixture upgraded through `MIGRATION_1_2`, `MIGRATION_2_3`, and `MIGRATION_3_4`, followed by a reopen check.

Project deletion unassigns related threads; thread deletion cascades messages. Router and Agent records use durable identifiers and maintain a user-visible safe trace without raw model reasoning.

## Agent integrity and restart policy

An Agent profile must reference an enabled Direct Model with a matching enabled account/connection, or an enabled Combo containing at least one usable enabled candidate. The UI presents only such locally derived choices, and the runtime validates again before starting a run to prevent stale or externally inserted profiles from executing.

Agent profiles explicitly select enabled tools. The runtime enforces that policy before evaluation and binds every workspace read, list, search, or write to the profile-assigned project. Read/list/search observations remain in memory for the immediate caller; the persisted Run Trace stores only safe metadata such as tool kind, bounded counts, and truncation state, never file content or raw model reasoning.

Write content is deliberately held only in runtime memory. If Android terminates the process while an approval is pending, recovery denies the approval, fails the awaiting run safely, and records that no write was performed. This prevents replay of a mutation whose exact payload was not persisted.

## Architectural constraints

Do not store secrets in `BuildConfig`, source code, plaintext preferences, Room plaintext, exported diagnostics, default backups, traces, or screenshots. Do not make Compose screens call a provider directly. Do not add an IVAI backend proxy to avoid local security design. Remote custom endpoints remain HTTPS-only. Support for a device-local or private-LAN model server requires a separate persisted trust mode, narrow Android cleartext allowlist, user warning and confirmation UX, credential-less policy, foreground-only discovery, and device validation; it must never be enabled as an implicit exception. Any future provider, network behavior, Agent tool, file capability, migration, or release change requires a focused review and validation evidence.
