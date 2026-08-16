# Changelog

All notable repository changes are documented here. IVAI has **not** published a public GitHub Alpha release yet.

## [Unreleased]

### Added

- Local Room-backed workspace persistence for projects, chat threads, messages, provider registry records, Router Combos/attempts, and bounded Agent records.
- Android Keystore-backed encrypted credential vault with credential references separated from Room metadata.
- User-managed Gemini, OpenRouter, and Custom OpenAI-compatible provider adapters, accounts, endpoint metadata, models, and capability metadata.
- A local, explicit cloud provider preset catalog for Gemini, OpenRouter, OpenAI, Groq, Mistral, Together, DeepSeek, Fireworks and xAI; presets prefill only reviewable non-secret setup metadata and never select a provider or model automatically.
- A preset-first, scrollable Provider setup flow that keeps model ID and declared capabilities under user control.
- Direct Model and ordered Combo execution targets with capability-aware sequential fallback and local attempt trace.
- Bounded Local Agent Alpha with calculation, current-time, explicit profile-selected bounded project-file read, workspace list, literal project search, one-time project-write approval, limits, cancellation, run trace, target validation, and process-death approval recovery.
- Legacy Room v1-to-v5 upgrade and reopen coverage, including safe defaults for persisted endpoint trust and authentication mode.
- Explicit local HTTPS trust mode for Custom OpenAI-compatible loopback and RFC1918 IPv4 endpoints, with persisted user confirmation, API-key/no-auth account choice, Router eligibility, and foreground streaming transport.
- Provider-neutral Sidebar execution status derived from local registry/router state.
- GitHub Alpha release checklist and updated product, architecture, security, and provider-harness documentation.

### Security

- No central IVAI backend, mandatory account, default analytics, or implicit provider target.
- No plaintext provider credential in Room, UI state, traces, exports, or source control; cloud preset metadata contains no credential.
- Remote custom endpoints, local loopback and private-LAN endpoints are HTTPS-only. Local connections require an exact classified host, persisted user confirmation and never receive an implicit cleartext exception.
- A no-auth local account holds only a canonical non-secret marker; it does not access the Vault or send an `Authorization` header.
- HTTP local endpoints, arbitrary LAN/public HTTP, `.local` discovery, scanning, self-signed-certificate bypass and background discovery remain blocked.
- No always-allow Agent write behavior and no replay of pending writes after process death; profile policy and project binding are enforced before every Agent tool execution.
- Read/list/search are app-private and bounded; their observations remain in memory and file content never enters persisted Run Trace.
- Shell, Termux, Shizuku, Accessibility automation, unrestricted storage, unrestricted HTTP POST tools, MCP process/server execution, and background autonomy remain outside Alpha scope.

### Pending before a public GitHub Alpha release

- Capture physical-device evidence for HTTPS loopback/LAN server behavior, cancellation, timeout and offline recovery. HTTP local servers and `.local` discovery remain separate, unapproved scopes.
- Capture RTL, accessibility, device, performance, and network/fallback release evidence.
- Produce an owner-approved signed APK, SHA-256 checksum, source tag, reviewed release notes, and known-limitations record.
