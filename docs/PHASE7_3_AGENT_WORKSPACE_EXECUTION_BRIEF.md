# Phase 7.3 — Agent Profile and Live Run Workspace: Execution Brief

> **Decision status:** Ready for implementation after the Phase 7.2 hardening correction in [PR #32](https://github.com/ILIV007/IVAI-App/pull/32). This is an independent, **UI-only** redesign phase. It must reuse existing Agent state and callbacks rather than change Agent tools, runtime policy, persistence, providers, Router or data contracts.

## Goal

Turn the current list-heavy Agents destination into a comprehensible **local-operation workspace**. A user must be able to understand an Agent profile’s target, bounded tools, project confinement and limits; start and observe a local run; cancel a non-terminal run; review a one-time file-write proposal; and recover from a failed target without losing explicit control.

## Baseline and non-negotiable guardrails

| Area | Baseline to preserve |
|---|---|
| Local-first | Profile, run, trace and approval state continue to come from existing Room-backed local state. No sync, telemetry, remote logging or mandatory account may be introduced. |
| Backendless | No IVAI backend, proxy, login, analytics/session replay or background worker is introduced. |
| BYOK and provider neutrality | A profile continues to point only to an explicit, user-managed Direct Model or ordered Combo. The UI must never choose or repair a provider/model/Combo automatically. |
| Bounded tools | The existing local read tools and the existing approval-gated `WRITE_PROJECT_FILE` remain unchanged. No Shell, Termux, Shizuku, Accessibility, MCP, arbitrary HTTP or new tool is added. |
| Approval-first writes | A write stays previewed and requires the existing explicit **Allow once** or Deny callback. There is no always-allow, auto-approve or replay of a pending write. |
| Runtime limits | Existing max steps, max tool calls and max runtime values remain the source of truth. The redesign may explain them but must not change their defaults or enforcement. |
| Visual system | Reuse IVAI semantic tokens, primitives, wordmark boundary, accessibility utilities and the AA-verified palette. Launcher artwork remains launcher-only. |

## Existing state and callback contract

Phase 7.3 must render from `AgentManagementState` and reuse the current screen callbacks without changing their signatures.

| Existing contract | Phase 7.3 presentation responsibility |
|---|---|
| `availableTargets` | Show explicit, selectable Direct Model/Combo targets and a no-target prerequisite state. |
| `profiles` and `AgentProfileCard` | Present a profile library with target, tools, project scope, limits and enabled state. |
| `onCreateAgent(...)` | Invoke only after a progressive review of user-entered profile details; preserve the existing target/project/tool/limit arguments. |
| `activeRuns`, `selectedRunId`, `selectedRunTrace` | Present a focused run workspace with timeline, selected trace and terminal/running state. |
| `onStartRun(profileId, goal)` | Invoke only from an explicit local-run confirmation using the selected profile. |
| `onSelectRun(runId)` and `onCancelRun(runId)` | Preserve selection and cancellation behavior; show cancel only for non-terminal runs. |
| `pendingApprovals` and `AgentApprovalCard` | Present path, bounded preview, one-time safety copy and explicit Allow once/Deny outcome. |
| `onResolveApproval(approvalId, allowOnce)` | Preserve existing approval resolution; no new consent persistence is created. |
| `operationError` and `onDismissError()` | Present clear safe reason categories, active target context and edit-target/retry routes where existing navigation permits. |

## In-scope experience work

### 1. Profile library and preflight states

Replace the generic profile list with cards built from `IvaiFoundation` primitives. Each card should make its explicit target, project boundary, allowed tools, write-approval condition and fixed limits scannable before a run starts. Empty states must distinguish no profile from no available target, and route the user to **Connections** only as an explicit action.

### 2. Progressive profile editor

Replace the monolithic alert-dialog form with an accessible multi-step sheet or page flow:

| Step | User decision | Required safety copy |
|---|---|---|
| 1. Identity | Name and local instructions | Instructions remain local to this profile. |
| 2. Target | Explicit Direct Model or Combo | IVAI does not select a provider/model/Combo for the user. |
| 3. Tools and project | Bounded tools and required project for workspace tools | Project file operations stay inside this one project; every write needs one-time approval. |
| 4. Review | Profile target, tools, project and immutable limits | Creation saves only the explicit review; no hidden permission or connection is added. |

The UI can provide defaults already enforced by the current callback path only as visible fixed limits; it must not create a new configurable runtime-policy model in this phase.

### 3. Live run workspace

Present a selected run as the primary work surface rather than a secondary list. It should combine goal, Agent name, target label where already available, state/timeline, safe trace summaries and a clear cancel affordance for running or awaiting-approval states. Terminal states must distinguish completed, cancelled and failed without fabricating a retry mechanism. A retry/edit-target route may only trigger existing start/navigation behavior.

### 4. One-time write-approval sheet

Redesign the existing approval dialog into a focused review surface with the target path, bounded preview, one-time consequence, Allow once and Deny. The surface must not reveal secrets, use a diff that the existing state cannot produce, or imply an always-allow grant.

### 5. Recovery and error presentation

Map existing safe error text into understandable reason categories: unavailable/invalid target, cancellation, bounded-runtime/step/tool limit and general local operation error. The UI may offer dismissal, target editing or a new explicit run where existing callbacks allow; it must not silently alter a target or resume a failed run.

## Explicitly out of scope

| Excluded item | Reason |
|---|---|
| New Agent tools, write modes, tool permissions or tool-policy persistence | Phase 7.3 is a presentation redesign; bounded-tool policy is stable Alpha scope. |
| Autonomous, scheduled or background Agent execution | Violates the current Local-first bounded Agent boundary and requires a separate threat model. |
| Always-allow, remembered approval or approval replay | Contradicts approval-first file writes. |
| Changed limits, new runtime semantics, retry execution engine or trace storage | Runtime/data contract changes are excluded from Phase 7 UI redesign. |
| Provider/model auto-selection, endpoint testing or connection repair | Preserves BYOK and explicit user ownership. |
| Room schema, migration, Vault, Router or provider-adapter changes | Must remain independently reviewable and out of this UI phase. |
| Embedded UX analytics or session replay | Violates the Backendless/Local-first product boundary. |

## Component and test plan

| Deliverable | Test/semantics evidence |
|---|---|
| `AgentProfileLibrary` and prerequisites | Empty-profile, no-target and target-ready screenshot/semantics states; explicit Connections action tag. |
| Progressive editor | Tests that no profile callback fires before final review; visible target/tool/project/limit review and enabled/disabled progression. |
| Run workspace | Screenshot/semantics states for running, awaiting approval, completed, cancelled and failed; cancel visible only for non-terminal state. |
| Trace inspector | Stable selected-run and trace-step tags; safe summaries only; empty-trace state. |
| Approval sheet | Path/preview/Allow once/Deny semantics; test that no write occurs before Allow once and Deny remains terminal. |
| Recovery UI | Error category, current explicit target and no-auto-repair copy; existing invalid-target/run tests remain green. |
| Accessibility | TalkBack labels, minimum touch targets, live terminal state announcement, dark/light parity and Persian/Arabic mixed-content screenshot evidence. |

## Acceptance gate

The Phase 7.3 implementation may be proposed for review only when all conditions below are satisfied.

1. Existing Agent approval, run-trace, cancellation, project-confinement and invalid-target tests pass unchanged.
2. New semantic and screenshot coverage proves running, awaiting approval, completed, cancelled, failed and denied states in the redesigned workspace.
3. No profile is created until the explicit final-review action, and no write resolves before Allow once.
4. Target, tool scope, project confinement and fixed bounds are visible before profile creation and before a run starts.
5. Full validation passes:

   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
   export ANDROID_HOME=/tmp/android-sdk-ivai
   ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
   git diff --check
   ```

6. Secret, cleartext/trust-bypass, prohibited-execution, implicit-provider/default and launcher-only boundary scans are clean. The PR must change no Agent runtime, provider, Router, Vault or data-layer file.
7. Both required GitHub CI checks pass before squash merge; main branch protection is restored and verified after merge.

## Delivery sequence

1. Create a dedicated `feat/phase73-agent-workspace` branch from the current protected `main` baseline.
2. Add UI-only Agent workspace primitives and stable test tags, preserving current callbacks.
3. Redesign one flow at a time: profile library/editor, run workspace, approval/recovery presentation.
4. Add state-matrix screenshots and semantics tests, then run focused tests before full validation.
5. Complete visual review in dark/light and mixed BiDi; record only representative approved baselines.
6. Run full build/test/lint and hardening scans; update this brief with final evidence.
7. Submit one focused PR, wait for CI, squash merge with protection restoration, then update the Roadmap to mark 7.3 complete.

## Deferred beyond Phase 7.3

Physical-device TalkBack/Force-RTL/font-scale evidence, signed Alpha artifacts, physical local HTTPS network behavior, new tools, background autonomy and UX-research sessions remain deferred to their existing Phase 6/7.5 or separate-threat-model tracks.
