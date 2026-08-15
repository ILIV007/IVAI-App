# Architecture

## Current state

The repository currently contains a single Android application module built with Kotlin, Jetpack Compose, Material 3, Coroutines, and ViewModel state. It is a mock-only UI Skeleton; it has no production Provider adapter, network client, database, credential vault, or Agent runtime.

## Target direction

The project evolves incrementally toward a lightweight clean architecture. UI features call use cases; use cases depend on provider-neutral domain contracts; local persistence, security, files, and provider adapters sit behind data interfaces. Module extraction is gradual and must follow a demonstrated boundary, not precede it.

```text
Compose UI
  -> ViewModel / UiState / UiEvent
  -> Domain use cases
  -> Repository interfaces
  -> Local data | Security vault | Files | Provider adapters
```

## Phase constraints

| Phase | Architectural rule |
|---|---|
| UI/RTL | Keep mock-only behavior; consolidate duplicated UI state before persistence. |
| Data/Security | Add Room, DataStore, Keystore and app-private workspace before credentials or provider traffic. |
| Provider | Add one provider-neutral adapter contract and one provider implementation first. |
| Router | Add sequential fallback only after the provider contract has stream, cancel, error, usage and capability semantics. |
| Agent | Add bounded tools only after workspace isolation, approval, budgets and trace entities exist. |

## Prohibited shortcuts

Do not store secrets in `BuildConfig`, source code, plaintext preferences, Room plaintext, exported diagnostics, or default backups. Do not make Compose screens call a provider directly. Do not add a backend proxy merely to avoid designing a local security boundary.
