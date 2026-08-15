# IVAI Project Master

## Product boundary

IVAI is a native Android AI workspace designed around local-first data ownership and Bring Your Own Key (BYOK). Conversations, projects, settings, diagnostics, and run traces remain on-device by default. Network access is reserved for a user-configured model provider, an explicitly enabled online tool, or later encrypted backup.

## Fixed decisions

| Topic | Decision |
|---|---|
| License | Apache-2.0 |
| Minimum Android | API 29 / Android 10 |
| Package (temporary) | `dev.iliv007.ivai` |
| Architecture | Lightweight clean architecture with gradual modularization |
| Persistence | Room and DataStore in the dedicated data/security phase |
| Credentials | Android Keystore-backed encryption; no plaintext secret persistence |
| Networking | Official provider endpoint through a central network gate |
| Telemetry | Zero by default in Alpha; redacted local diagnostics only |
| Provider policy | BYOK; no credential interception or subscription bypass |

## Product phases

Phase 0 establishes reproducible builds and repository governance. Phase 1 closes the UI/RTL Skeleton. Phase 2 introduces local data and security. Phase 3 proves one real provider chat slice. Phase 4 adds sequential Router fallback. Phase 5 adds the bounded Agent only if the accepted Alpha decision keeps it in scope. Phase 6 hardens and releases Alpha.

## Alpha exclusions

No mandatory backend, default telemetry, shell/Termux, Shizuku, Accessibility automation, unrestricted storage, MCP process execution, local-model inference, race/council routing, voice, or autonomous background actions belong to the current Alpha implementation.

## Decision log

Any scope change, new dependency, architecture change, security-sensitive behavior, or feature outside this document and `docs/ROADMAP.md` requires a dated decision entry in the relevant task handoff or a dedicated ADR before implementation.
