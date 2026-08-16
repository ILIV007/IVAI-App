# Phase 7.2 — Provider and Combo Setup Experience

> **Status:** Implementation started on branch `feat/phase72-provider-combo-ui`. This is an **interface-only** increment. Existing Provider, Vault, Router, Agent, Room, migration, endpoint validation, and network contracts are inputs to the UI; none may be changed by this phase.

## Goal

Make IVAI’s user-managed Provider, Account, Endpoint, Model, and Combo setup understandable without weakening Local-first, Backendless, or BYOK guarantees. The user must be able to understand what they are creating, which HTTPS trust boundary applies, whether a credential is stored without revealing it, which model capabilities they declared, and the exact ordered fallback list that will be saved.

## Scope

| Area | Phase 7.2 deliverable |
|---|---|
| Connections destination | A provider/connection hub with cards that summarize family, trust zone, account credential state, declared model availability, enablement, and clear next actions. |
| Provider creation | A four-step guided sheet: **Family** → **Endpoint and trust** → **Account and credential** → **Model, capability and final review**. No persistent provider is created before the explicit final save. |
| Trust explanation | Distinct, non-interchangeable Remote HTTPS, Local-device HTTPS, and Private-LAN HTTPS explanations. Local choices expose the existing explicit trust confirmation and retain no-auth as an explicit option only where currently supported. |
| Combo creation | An ordered candidate builder with visible priority numbers, move-up/move-down controls, capability summary, review copy, and an explicit final save. |
| Empty states | A clear progression from connection to account/credential to declared model to Combo. No empty state claims that IVAI selected or discovered anything. |
| Accessibility and testability | Every new control has a stable test tag; icon-only controls have content descriptions; state transitions and final save behavior receive Compose tests and recordable screenshots. |

## Non-goals and invariants

| Boundary | Required behavior |
|---|---|
| Provider neutrality | No default provider, account, model, endpoint, or Combo. Presets reduce typing only; they never create or select a target. |
| Credentials | Secret text remains password-transformed, is sent only to the existing vault callback at explicit final save, and is never displayed in cards, screenshots, test fixtures, traces, or documentation. |
| Endpoint trust | HTTP, cleartext exceptions, `.local`/mDNS, scanning, automatic discovery, self-signed bypass, and connection testing are excluded. Existing HTTPS endpoint validation remains authoritative. |
| Creation semantics | The UI does not call provider creation until the fourth-step final save. The UI does not call Combo creation until its review step final save. |
| Runtime and data | No Provider adapter, Router algorithm, Agent behavior, Room schema, migration, Vault API, permission, network request, background task, or preset data change. |
| Visual boundary | The independent IVAI indigo/emerald/violet system and launcher-only artwork rule remain in effect. |

## Implementation boundaries

The expected code surface is limited to Compose presentation and UI tests. The primary existing entry points are the Connections route (`RouterScreen`), the provider management presentation currently mounted in Settings, the existing add-provider callback, and the existing create-Combo callback. The state objects and callbacks are reused rather than expanded. Any proposed change outside these UI layers requires a new, separately approved phase.

## Acceptance gate

| Gate | Pass condition |
|---|---|
| Existing policy tests | Endpoint policy, migration, vault, provider registry, Router, and Provider Management tests remain green. |
| New UI tests | Tests cover family selection, each trust zone’s disclosure path, local trust confirmation requirement, no-auth explicitness, account/credential field semantics, model/capability review, final-save-only creation, empty progression, ordered Combo candidates, reordering, and final Combo save. |
| Screenshot evidence | Recordable compact and expanded captures cover an empty Connections hub, trust selection/review, credential-safe model review, ordered Combo builder, and a populated connection/Combo summary. |
| Security scan | No secret pattern, cleartext/trust bypass, prohibited execution, or implicit-selection pattern is introduced. |
| Quality gate | `clean assembleDebug testDebugUnitTest lintDebug`, `git diff --check`, and both GitHub CI gates pass. |

## Deferred

Connection testing, model discovery, credential reveal, HTTP or local discovery, provider health probes, edits to existing persistent provider records, advanced bulk model management, and all changes to data/runtime behavior remain outside Phase 7.2. Physical-device Force-RTL, TalkBack, font-scale, and local HTTPS network evidence remain Alpha-hardening work.


## Initial visual review checkpoint

The compact Combo review confirms that Connections summarizes local readiness before the builder, shows the exact user-defined fallback order, and presents a final-save affordance only after review. The review card clearly states that no provider is inserted automatically.

The first Provider review capture confirms that credential material is not displayed, but it also exposed two polish items before the final validation gate: review-row labels need a clearer delimiter, and the record-mode test must wait for model-field recomposition before capturing its final summary. These are presentation/test-evidence corrections only and do not affect the provider creation callback or any runtime contract.


## Visual review resolution

After the corrective polish, the record-mode Provider review shows the declared model, unambiguous summary delimiters, the selected Remote HTTPS trust zone, and the credential-safe statement `API key will be encrypted after save`; the API key value itself is absent. The compact Combo review continues to show the exact reordered candidates and an explicit final-save action. Both reviewed artifacts therefore satisfy the Phase 7.2 presentation boundary without exposing a credential or suggesting any implicit provider selection.


## Implementation validation

The completed implementation keeps production changes confined to Compose shell and screens: `MainActivity` route wiring, the Connections/Provider presentation, the Combo presentation, and the Settings shortcut. It introduces no change to provider adapters, endpoint validators, vault behavior, router persistence/execution, agents, Room/data, permissions, or network operations.

The final local gate `./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain` passed. The suite contains **102 tests** with **0 failures, 0 errors, and 0 skipped**. Lint has **0 Error/Fatal** findings; its 19 warnings are the pre-existing version-maintenance/informational findings recorded by the Phase 7.1 hardening audit. Whitespace, secret, cleartext/trust-bypass, prohibited-execution, implicit-selection, and launcher-only boundary scans were clean.
