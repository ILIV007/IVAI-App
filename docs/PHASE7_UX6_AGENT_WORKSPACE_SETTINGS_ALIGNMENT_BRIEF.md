# Phase 7 UX-6 — Agents, Workspace and Settings Surface Alignment

**Status:** Deterministically complete in [PR #134](https://github.com/ILIV007/IVAI-App/pull/134), squash-merged as `d227f6749bd2335f389dd1ecc9d378004877cd44` on 22 August 2026.

## Goal

Align the remaining local-workflow destinations with the rebuilt IVAI product hierarchy. Agents, Workspace and Settings must make the current local context, user-controlled next action and safety boundary easy to scan without repeating provider setup, inventing activity, or changing any existing runtime behavior.

## Scope

| Surface | UX-6 presentation decision |
|---|---|
| Agents | Add a clear page-level hierarchy that separates the local safety boundary, pending one-time approval, active selected run, profile library and run history. Preserve current callback ownership and visible safety labels. |
| Workspace | Make selected project context, known local facts and intentional continuation routes visually distinct from the project library. Preserve the existing project selection and Chat/Agents navigation callbacks. |
| Settings | Refine section hierarchy so appearance, Connections, privacy commitments and destructive local-data action communicate purpose and consequence at a glance. Keep the theme toggle in Settings only. |
| Semantics and regression | Add stable semantic/test contracts for the aligned hierarchy and focused Compose evidence for explicit action, safety, local-only facts and destructive-action confirmation. |

## Deliberately unchanged

UX-6 does not add or edit an Agent profile lifecycle, target choice, provider/model/Combo behavior, Connection flow, credentials, vault, Room schema, project persistence, file browsing, file writes, approval execution, run start/cancel behavior, retries, fallback, network behavior, backend, telemetry, scheduling, skills, MCP, signing or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition |
|---|---|
| Hierarchy | Agents, Workspace and Settings expose a stable page hierarchy whose major sections are distinguishable in Compose semantics. |
| Explicit control | Existing user-initiated actions retain their callbacks: Connection navigation, project context/select/clear, Chat/Agent continuation, run cancel and local-data deletion confirmation. |
| Safety and honesty | Pending write review remains unresolved until explicit action; Workspace represents only persisted local facts; Settings retains the local-only delete confirmation. |
| Local-first/BYOK | No provider default, target auto-selection, transport request, backend, telemetry, secret handling or data-model change is introduced. |
| Quality gate | Static BYOK/RTL scans, contrast check, Secret scan, debug/release build, unit suite and lint complete successfully in protected CI. |

## Validation outcome

The focused implementation passed the protected GitHub CI **Secret scan** and **Build, unit test, and lint** checks for [PR #134](https://github.com/ILIV007/IVAI-App/pull/134). The successful workflow completed the repository architecture guards, debug and minified-release builds, unit suite and Android lint. One initial unit-test failure was diagnosed as a test-only misuse of `ComposeTestRule.setContent` twice in one test; the regression was split into independent Workspace and Settings tests without changing production behavior. The corrected commit passed the complete protected workflow. No local Android build is claimed because this sandbox does not contain the Android SDK.

## Deferred validation

Physical compact/expanded layout, device rotation, font-scale, TalkBack, Force-RTL, physical keyboard behavior, network/cancellation behavior, signing and release evidence remain field/Alpha gates. This phase makes no claim that they have been executed.
