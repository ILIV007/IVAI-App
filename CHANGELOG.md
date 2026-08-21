# Changelog

All notable repository changes are documented here. IVAI has **not** published a public GitHub Alpha release. The active release decision and external gates are maintained in the [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md).

## [Unreleased]

### Added

- A Local-first Android workspace backed by Room for projects, chat threads/messages, provider registry records, Router Combos/attempts, Agent profiles/runs/traces, and approval state.
- An Android Keystore-backed encrypted credential vault that keeps only opaque credential references in Room.
- User-managed Gemini proof, OpenRouter, and Custom OpenAI-compatible HTTPS adapters, Connections, Accounts, Models, declared capabilities, endpoint trust, API-key/no-auth modes, and enable/disable controls.
- Explicit Direct Model and ordered Combo targets with capability-aware sequential fallback, redacted local attempt traces, and no implicit provider/model selection.
- A bounded local Agent with calculation/current-time tools, project-bound read/list/literal-search tools, one-time write review, limits, cancellation, local trace, target validation, and safe process-death recovery.
- Connection-first Provider/Account/Model management, sidebar-first navigation, rebuilt Chat workspace, safer local-data deletion confirmation, adaptive launcher safe-zone assets, and semantic light/dark contrast checks.
- Controlled Phase 7.5 research material: protocol, field kit, blank local worksheet, facilitator-only scenario cards, deterministic package verifier, and explicit non-public boundaries.
- Release Candidate helper/verifier, Android SDK provisioning contract, reproducibility evidence, and owner-controlled local signing-evidence handoff. These tools create neither an owner key nor a tag, upload, GitHub Release, or public artifact.
- Documentation index, contributor validation contract, repository text/binary hygiene, and a focused pull-request template.

### Fixed

- Direct-provider and Router chat now stop collection after the first terminal `Completed`, `Failed`, or `Cancelled` event. A malformed adapter cannot append later output to UI, persistence, or Router fallback decisions.
- The Release Candidate helper, verifier, synthetic fixture, and active documentation now enforce the current deterministic baseline of **160 clean unit tests**.
- CI SDK provisioning, compile SDK, and Release Candidate build-tools contract are fail-closed at API 37.1 / build-tools 37.0.0.

### Security and privacy boundaries

- No central IVAI backend, mandatory account, default analytics pipeline, cloud sync, or implicit provider target is introduced.
- No plaintext provider credential is stored in Room, UI state, local export, trace, source control, or release evidence.
- Custom remote, loopback, and private-LAN endpoints are explicit HTTPS-only connections. HTTP, `.local`/mDNS discovery, scanning, trust-all, hostname-verifier bypass, and background discovery remain blocked.
- No Shell, Termux, Shizuku, Accessibility automation, unrestricted storage, unrestricted HTTP tools, MCP process/server execution, autonomous background agents, or local-model inference is part of current Alpha scope.
- Project writes require a visible bounded preview and explicit **Allow once** approval; unresolved approval is denied during recovery and never replayed automatically.

### Validation and release status

- Current deterministic evidence includes protected CI, debug/release-R8 build, **160 tests with zero failures/errors/skips**, lint with zero issues, security/RTL/provider-neutrality/architecture guards, unsigned Release Candidate package verification, and owner-signing helper regression coverage.
- This evidence does **not** replace voluntary usability/heuristic review, compact/medium device matrix, Force-RTL, TalkBack, real HTTPS cancellation/timeout, owner-controlled signing, annotated tag, reviewed notes, or owner approval.

### Pending before a public GitHub Alpha release

- Record real, de-identified Phase 7.5 usability/heuristic evidence and resolve/retest any P0/P1 findings.
- Record compact and medium device install/upgrade/restart/rotation/offline, light/dark, font scale, Force-RTL, TalkBack, launcher, and real HTTPS loopback/private-LAN cancellation/timeout evidence.
- On one approved commit only, have the repository owner create a signed APK, retain SHA-256/provenance, create an annotated tag, review release notes, and approve the GitHub prerelease.
