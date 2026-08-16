# Security Architecture

## Security posture

IVAI Alpha is local-first, backendless, and BYOK. Secrets and personal workspace content are high-sensitivity data. There is no mandatory IVAI backend, default telemetry pipeline, analytics account, credential sync, or hidden provider selection. Diagnostics remain local and must be redacted before a user manually exports them.

## Credential policy

Credentials are stored as encrypted records in Preferences DataStore through a per-reference Android Keystore AES/GCM key. Room persists only a credential reference and provider/account metadata. Invalid or corrupted envelopes fail closed and are never silently replaced. Clearing a credential removes the DataStore record and deletes its matching Keystore key.

The app may display a user-provided label and masked status, but never re-displays a full key. Credentials must never enter source control, `BuildConfig`, Room plaintext, logs, UI state, screenshots, Router attempts, Agent traces, exports, or backups.

## Persistence and migration policy

`IvaiDatabase` v4 stores local workspace records, provider registry metadata, Router Combos and attempts, and bounded Agent records. Provider credentials never enter Room. Schemas are versioned and committed. Migration coverage includes a file-backed v1 database upgraded through all current migrations and reopened after validation.

Any schema, export/import, backup/restore, or local-file boundary change requires a dedicated migration/recovery test and security review. Project files remain within the app-private workspace and all relative paths are validated against traversal.

## Network policy

Network traffic occurs only for a user-initiated foreground provider operation. Provider adapters are resolved from an enabled user-managed connection, account, and selectable model, or from an enabled Combo; the Router does not inject an implicit fallback provider. Gemini is a proof adapter, not a required application provider.

Custom OpenAI-compatible endpoints require HTTPS and a nonblank host. Network gates must not log Authorization headers, raw API keys, prompt/response content by default, raw model reasoning, or unredacted secrets. The current adapter set is Gemini, OpenRouter, and Custom OpenAI-compatible.

## File, export, and deletion policy

Projects use an app-private workspace. Export/import is schema-versioned, checksummed, validated in temporary storage, and committed only after validation. Alpha exports omit all secrets. The local data reset flow removes the local workspace, Room records, and credential material through the controlled reset boundary.

## Bounded Agent policy

Agent profiles must use an enabled local Direct Model with a matching connection/account/model, or an enabled Combo with at least one usable candidate. The UI presents only registry-derived targets and the runtime validates again before a run begins.

Current safe tools are calculation and current time. The only mutation-capable tool writes a project file after a bounded preview and explicit **Allow once** confirmation. There is no always-allow setting. Agent runs are limited by steps, tool calls, and runtime; each step is persisted in a safe trace. Shell, Termux, unrestricted HTTP POST, unrestricted storage, Accessibility, Shizuku, MCP process execution, and background autonomy are out of Alpha scope.

Write content remains in runtime memory only. If the process dies while approval is pending, startup recovery denies the approval, fails the interrupted run safely, and records that no write was performed. A pending write is never replayed automatically after restart.

## Security review triggers

A separate security review is required for Keystore/cryptography, database migration, backup/restore, file mutation, tool permission, dependency changes, provider networking, any new Agent tool, or a release candidate. A public Alpha release also requires the evidence listed in [ALPHA_RELEASE.md](ALPHA_RELEASE.md).
