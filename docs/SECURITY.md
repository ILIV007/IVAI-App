# Security Architecture

## Principles

IVAI is local-first and BYOK. Secrets and personal content are treated as high-sensitivity data. The Alpha has no mandatory backend and no default telemetry. Diagnostics remain local and must be redacted before manual export.

## Credential policy

Provider credentials are introduced only in the data/security phase. The foundation uses a per-reference Android Keystore AES/GCM key and stores only a versioned ciphertext envelope (version, base64 IV, base64 ciphertext) in Preferences DataStore. Invalid or corrupted envelopes fail closed and are never silently replaced. Clearing an entry removes its DataStore record and deletes its matching Keystore key. The app stores a label and masked identifier for display, never re-displays the full key, and never places credentials in source, `BuildConfig`, plaintext preferences, Room plaintext, logs, screenshots, traces, exports, or backups.

## Persistence policy

Room persists projects, threads, and messages only in the app-private database. Provider credentials never enter Room. Database schemas are versioned and committed; a future migration or backup/restore change requires dedicated migration tests and a security review. The current repository boundary remains local-only and has no provider traffic.

## Network policy

All future traffic passes through a central Network Gate that records purpose, provider domain, status, latency, and an opaque attempt ID. It must not log Authorization headers, raw API keys, prompt/response content by default, or internal reasoning. Only official provider endpoints configured by the user are allowed.

## File and export policy

Projects use an app-private workspace. Any imported path is canonicalized and validated against the workspace boundary. Export/import is schema-versioned, checksummed, validated in temporary storage, and committed only after validation. Alpha exports omit all secrets, including opt-in paths.

## Agent policy

Future Agent tools require allowlisting, typed argument validation, least privilege, step/time/token/tool-call limits, user-visible trace, cancellation, and explicit approval before any mutation. Shell, unrestricted HTTP POST, unrestricted storage, Accessibility, Shizuku, and MCP process execution are out of Alpha scope.

## Security review triggers

A separate security review is required for Keystore/cryptography, database migration, backup/restore, file mutation, tool permission, dependency changes, provider networking, or release candidates.
