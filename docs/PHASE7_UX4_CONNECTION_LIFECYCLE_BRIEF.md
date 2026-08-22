# Phase 7 UX-4 — Connection, Account, Model and Combo Lifecycle Surface

**Status:** Deterministic implementation and CI validation are complete in [PR #130](https://github.com/ILIV007/IVAI-App/pull/130), squash-merged to `main` as `bba74053432ba36cb3f860be418f19fa4dead79c` on 22 August 2026. Physical validation remains pending.

## Goal

Make the user-owned setup lifecycle legible and progressive: an explicit **Connection** contains explicit **Accounts** and declared **Models**; only then may the user intentionally create an ordered **Combo**. The interface must never ask for a Connection again while adding a Model, infer a target, or imply a model discovery/test that does not exist.

## Scope

| Included | Decision |
|---|---|
| Lifecycle hierarchy | Present the fixed order `Connection → Account → Model → Combo` as an explicit progression with local counts and clear next actions. |
| Connection surface | Show provider family, HTTPS trust boundary, enabled state and destructive action in the connection header; group Accounts and Models under that saved connection. |
| Account/model ownership | Keep “Add account” and “Add model” actions on the selected Connection; neither action can re-open provider family setup. |
| Combo surface | Explain that only user-selected, eligible account/model candidates can be ordered; keep ordered fallback explicit and preserve the existing two-step final review. |
| Test readiness | State that no provider/model network test runs in UX-4; the R8 runtime test remains separately blocked, rather than presenting a non-functional control. |
| Regression | Add lifecycle labels/state semantics and retain existing save, trust, no-auto-selection and ordering tests. |

## Deliberately unchanged

UX-4 does not add providers, models, presets, endpoint discovery, network requests, provider/model test calls, credential persistence behavior, Room schema/migrations, vault implementation, target auto-selection, router fallback logic, Agent behavior, Chat/IME, telemetry, backend, signing or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition | Status |
|---|---|---|
| Lifecycle clarity | setup view exposes the four ordered stages and its next-action text refers only to the first incomplete stage. | **Passed in PR #130 CI** |
| Ownership | account/model actions are connection-scoped and existing tests prove they do not create a second provider. | **Passed in PR #130 CI** |
| BYOK boundary | setup copy and UI retain explicit local vault/HTTPS/no-discovery/no-auto-selection constraints; no secret is rendered after save. | **Passed in PR #130 CI review** |
| Combo ordering | candidate selection and final review preserve the user’s explicit sequence; no implicit candidate appears. | **Passed in PR #130 CI** |
| Quality gate | secret scan, debug/release build, unit suite and lint succeed in protected CI. | **Passed in PR #130 CI** |

## Validation record

PR #130 initially exposed pre-existing test-host viewport assumptions after the lifecycle card made the Connections hub appropriately taller. The regression tests were updated to use actual parent-list swipe interaction, and the required Compose gesture import was added. These were test-only fixes; provider, vault, endpoint and runtime behavior did not change. The required `Secret scan` and `Build, unit test, and lint` checks completed successfully before merge. The sandbox did not contain an Android SDK, so local Gradle completion is not claimed.

## Deferred validation

Provider/model one-shot test execution remains the separately blocked R8 runtime scope. Physical keyboard, TalkBack, Force-RTL, font-scale, local endpoint, network timeout/cancellation and usability evidence remain device/field gates.
