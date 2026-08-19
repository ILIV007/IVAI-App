# Phase 7 R4 — Connection-First Account and Model Management

## Goal

R4 replaces the prior coupled setup behavior with a **connection-first management flow**. A user creates one explicit Provider Connection and its first Account, then may add additional Accounts and declared Models beneath that same Connection. This aligns the management surface with the existing Room relationship model and keeps all provider choice, endpoint trust, credentials, model declaration, and Combo ordering under the user's local control.

> **Boundary:** R4 changes only local presentation and local persistence orchestration. It does not add a provider adapter, network discovery, automatic target selection, live provider/model testing, HTTP, mDNS, LAN scanning, backend synchronization, telemetry, or a secret stored in Room.

| Area | R4 decision |
|---|---|
| Initial setup | Three explicit steps: connection family, endpoint/trust, and first account/credential. A model is not required. |
| Additional Accounts | A saved Connection exposes an **Add account** sheet. The user explicitly provides a label and, when applicable, an API key at the final save boundary. |
| Additional Models | A saved Connection exposes an **Add model** sheet. The user supplies each model identifier and at least one capability. There is no discovery or implicit capability selection. |
| Persistence | Existing Room foreign keys and the unique `(connection_id, provider_model_id)` index remain unchanged. A Connection may therefore retain multiple Accounts and multiple Models. |
| Secrets | Raw API keys exist only in the save callback; the Keystore-backed vault receives the key and Room retains only an opaque credential reference. |
| Runtime behavior | No connection, model, or account is tested automatically. No provider/model/Combo is selected automatically. |

## Scope

The Connections Hub now displays Accounts and declared Models as nested resources of each Connection. It provides local, explicit controls to add either resource. Connection creation now ends immediately after the account/credential validation succeeds; the old fourth model declaration step is removed. The existing first-connection ViewModel method preserves legacy support for an optional supplied model, but the R4 UI always passes an empty model declaration and uses the dedicated model action instead.

The ViewModel owns two foreground-only local actions: `addAccountToConnection()` and `addModelToConnection()`. Both verify that the Connection exists before persistence. Account addition rejects duplicate labels per Connection, observes the existing endpoint trust policy through the repository, stores an API key only in the vault when API-key authentication is selected, and clears the vault record if persistence fails. Model addition rejects blank IDs, empty capability declarations, and case-insensitive duplicate model IDs under the same Connection. No action opens a transport.

## Regression Coverage

| Test area | Verified behavior |
|---|---|
| Connection setup UI | Final save is available after the account step; a Model is not required and the creation callback receives no model declaration. |
| Additional Account UI | A new Account is added under an existing Connection without triggering a new Provider creation callback. Required label/API-key validation is visible. |
| Additional Model UI | A Model is added under an existing Connection without triggering a new Provider creation callback. Model ID and capability declaration are both required. |
| Room registry | One Connection retains two Account credential references and two Model records, then cascade-deletes both resource sets when the Connection is removed. |
| Existing router flow | Ordered Combo candidate selection remains explicit and continues to use persisted Connection/Account/Model record identifiers. |

## Acceptance Gate

R4 is complete only when the focused UI and Room regressions pass, the full deterministic quality gate passes with no new lint errors or secret/transport/implicit-selection violations, and the pull request CI confirms both required jobs. The branch must be reviewed and squashed into protected `main`; temporary merge bypass protection must be restored immediately after the merge.

## Deliberately Deferred

The following capabilities are excluded from R4 and require later focused phases and explicit consent design:

| Deferred item | Reason |
|---|---|
| Edit, remove, or enable/disable individual Account or Model records | R4 establishes independent addition and correct nested visibility first. Mutating controls need separate UX, vault cleanup, Combo-reference, and recovery review. |
| Provider or Model test button | The planned R8 test action must be one-shot, foreground-only, consent-led, redacted, and must never use discovery/retry behavior. |
| Network execution changes | Phase 7 UI work must not weaken existing HTTPS-only, explicit-target, local-first, or BYOK safeguards. |
| Physical-device validation | IME, light/dark, TalkBack, Force-RTL, font-scale, lifecycle and network cancellation evidence remain Phase 7.5/Alpha gates. |

## Review Checklist

- [x] Connection creation is no longer coupled to a required Model declaration.
- [x] A saved Connection exposes independent Add Account and Add Model actions.
- [x] The user declares Model ID and capabilities; no discovery exists.
- [x] The user provides an Account API key only at final save; Room retains no secret.
- [x] Duplicate Account labels and Model IDs are rejected before saving.
- [x] Existing Room schema and foreign-key contracts remain unchanged.
- [x] No transport, HTTP allowance, local discovery, automatic target selection, backend, or telemetry was introduced.
- [ ] Physical-device and accessibility evidence remains pending and is not claimed by this document.

## Next UI Work

The next structural rebuild phase is **R5 — Agent and Workspace surfaces**, followed by Settings destructive-action safety and the separately scoped R8 Provider/Model test action. These phases remain subject to the existing Phase 7.5 and Alpha evidence gates.
