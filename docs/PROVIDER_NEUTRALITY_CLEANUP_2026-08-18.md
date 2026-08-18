# Provider-Neutral Repository Cleanup — 2026-08-18

> **Decision:** IVAI’s repository identity is the **Local-first, Backendless, BYOK Agent Harness**. A supported proof adapter may remain installed and explicitly selectable, but no vendor-first product identity, template capability, stale service configuration, or user-facing vendor prefix may define the application.

## Scope and Result

This cleanup removed nonfunctional branding and legacy artifacts while preserving the active adapter contract and Android build requirements. It does not add a provider, alter endpoint policy, make a network request, change stored data, add a credential, modify permissions, configure signing, or change any Phase 7.5/Alpha gate.

| Area | Cleanup result | Reason |
|---|---|---|
| Development guidance | The legacy vendor-named guide was renamed to `DEVELOPMENT_GUIDE.md` and rewritten as an IVAI implementation guide. | Development rules belong to the harness, not one adapter. |
| Legacy chat path | Unused deprecated direct chat session was removed. `LocalProviderChatSession` and the adapter registry remain the active provider-neutral path. | No production call site referenced the deprecated class. |
| Build template | The unused service-plugin passthrough property and vendor-named IDE comment were removed from `gradle.properties`. | No build script or plugin consumed the property. |
| Product metadata | Description and major capability now identify a Local-first, Backendless, BYOK Agent Harness. | Repository metadata must not advertise a single server-side provider capability. |
| User-facing labels | The selectable provider preset and final-review label use the neutral adapter name without a vendor prefix. | Selection remains explicit and user-controlled. |
| Documentation | Nonfunctional vendor wording in release/security scans, survey suggestions, cloud backup, distribution, and historical task phrasing was generalized. | Documentation now describes capability and policy rather than unnecessary platform branding. |

## Retained Technical References

The following references remain intentionally because they are functional dependencies or required protocol provenance, not product identity. They must not be used as a default-provider policy.

| Reference class | Why it remains | Safety boundary |
|---|---|---|
| Proof-adapter protocol endpoint and parser | Required to implement the installed, user-selectable streaming adapter. | Only a foreground action through an enabled user-managed connection/account/model may invoke it. |
| Official adapter documentation URL | Required reviewable metadata in the local preset catalog. | A preset cannot create a connection, choose a model, save a credential, or contact a provider. |
| Android dependency repository and KSP plugin coordinate | Required to resolve Android and symbol-processing build dependencies. | It is a build dependency, not an IVAI backend, telemetry SDK, provider selection, or runtime service. |
| No-telemetry audit evidence | The release audit names reviewed SDK classes to prove they are absent. | No hosted telemetry or backend SDK is added. |

## Regression Guard and Validation

`scripts/check_provider_neutral_branding.sh` now fails if the removed legacy guide, vendor-first display branding, stale service property, server-side single-provider metadata, or IDE/template wording reappears. The guard also requires the generic development guide, the explicit adapter registry, and the installed proof adapter to remain present. It runs in the protected Android quality workflow before build/test/lint.

The cleanup must pass the following deterministic evidence before merge:

| Check | Required result |
|---|---|
| Branding guard | All removed nonfunctional patterns remain absent; neutral metadata and adapter registry are present. |
| Unit regression | Provider preset catalog retains the neutral adapter label and its existing provider kind. |
| Build quality | Debug build, minified R8 release, all unit tests, and lint succeed. |
| Safety scans | No hardcoded credential, cleartext/trust bypass, prohibited execution, implicit provider selection, or global forced-LTR override is introduced. |
| Scope | No provider endpoint, protocol, credential handling, transport policy, runtime selection behavior, or release gate changes occur. |

## Release Effect

This is repository and product-identity cleanup only. It does not close participant/device evidence, signing, tag, release-note, owner-approval, Alpha, or stable-public release gates.
