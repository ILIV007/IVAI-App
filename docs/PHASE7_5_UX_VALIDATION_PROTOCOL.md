# Phase 7.5 — UX Validation and Hardening Protocol

> **Status:** In progress on `main` after [PR #37](https://github.com/ILIV007/IVAI-App/pull/37).
>
> **Purpose:** Validate the Phase 7 information architecture and safety comprehension without adding product telemetry, analytics, session replay, a backend, background work, or a provider/runtime/data-layer change. This protocol is an evidence plan; it does **not** claim that unrun studies or physical-device checks have passed.

## Scope and Guardrails

Phase 7.5 has two deliberately separate workstreams. The first is deterministic hardening that can run in the repository or a controlled local environment. The second is voluntary external research and physical-device accessibility validation. Study data must remain outside IVAI’s runtime and must never include API keys, credentials, prompt contents, workspace files, run traces, device identifiers, screen recordings, or raw participant identities.

| Workstream | In scope | Explicitly excluded |
|---|---|---|
| Deterministic hardening | Full build/test/lint, architecture/security/RTL scans, semantic and screenshot coverage review, low-risk manifest/resource lint fixes that preserve the no-backup/no-transfer policy. | Provider/Router/Agent/Data runtime, Room schema, project isolation, credential handling, deletion semantics, endpoint policy, network behavior, permissions, analytics, and background work. |
| IA validation | Open card sort and tree test using voluntary participants and de-identified observations. | Instrumentation in the product, participant tracking, session replay, hidden telemetry, or an attempt to infer user behavior from production data. |
| Safety comprehension | Moderated task sessions with an interactive debug build/prototype and a fixed moderator script. | Real user credentials, live provider calls, actual Agent file writes, or collection of sensitive content. |
| Accessibility/device evidence | Compact and medium physical-device checks across dark/light, font scale, Force-RTL, TalkBack, fresh install, upgrade, restart, rotation, and offline states. | Treating JVM/Roborazzi output as equivalent to physical-device or assistive-service evidence. |

> **Safety rule:** All execution-target choices remain explicit. A test participant must not be led to believe that IVAI selects a provider, model, Combo, Agent, endpoint trust mode, or write approval automatically.

## Pre-Phase Debug Baseline

The post-Phase 7.4 debug pass was clean at commit `66fbd36`: a clean `assembleDebug`, `testDebugUnitTest`, and `lintDebug` completed; an independently rerun unit suite reported **122 tests, 0 failures, 0 errors, and 0 skipped**. Secret, transport/trust-bypass, prohibited-execution/storage, implicit-provider-selection, global forced-LTR, and stale-roadmap scans were clean. The four remaining LTR overrides are narrowly confined to chat footer rows and terminal/Markdown code blocks, which is the allowed exception.

Lint has **0 Error/Fatal** and **17 Warning** findings. The findings are triaged below; a warning is not treated as proof of a product defect.

| Category | Count | Disposition |
|---|---:|---|
| Dependency/toolchain currency notices | 12 | Deferred. Updating AGP, Gradle, Kotlin, Compose, AndroidX, Navigation, and Roborazzi is a separate dependency-upgrade phase because it could alter the tested build/runtime surface. |
| Redundant `MainActivity` label | 1 | Eligible for low-risk cleanup; removing it preserves the application label inherited by the activity. |
| Android 12+ data extraction rules | 1 | **P1 hardening candidate.** Preserve the existing no-backup policy explicitly for cloud backup and device-to-device transfer; this requires a focused manifest/resource review and regression gate. |
| Obsolete `mipmap-anydpi-v26` qualifier | 1 | Eligible for resource-organization cleanup because `minSdkVersion` is already 29; it must not alter launcher artwork. |
| Adaptive-icon monochrome layer | 2 | Deferred pending an approved, distinct single-color launcher asset. The existing foreground is artwork/bitmap-based and must not be repurposed as general in-app branding. |

Android’s current backup guidance notes that some Android 12+ device implementations can still permit device-to-device transfer even when `android:allowBackup="false"`; explicit data extraction rules are therefore the relevant way to express the existing no-transfer intent for cloud and device transfer paths.[3]

## Research Questions and Decision Rules

| Question | Method | Evidence to collect | Decision rule |
|---|---|---|---|
| Do users group IVAI concepts into the intended primary navigation vocabulary? | Open card sort | Anonymous category labels, group membership, ambiguous labels, and facilitator notes. | At least **70%** agreement for each primary navigation category, or revise labels/placement and retest. |
| Can users find a target change, trust setting, Combo creation, write approval, and project chat from the proposed hierarchy? | Tree test | First path, final path, direct success, recoverable success, failure, and brief reason. | At least **80%** direct-task success across the five critical tasks, or revise IA and repeat validation. |
| Can users explain safety-critical behavior? | Moderated usability session | Task completion, observed error, exact paraphrase of target/approval/recovery meaning, and non-sensitive notes. | Each participant can identify the active target, explain one-time write approval, and recover from a failed target without moderator rescue. Any misunderstanding that can cause unsafe execution is P1. |
| Does the product provide control, recovery, clarity, and accessible interaction? | Heuristic review plus physical-device sweep | Severity-tagged finding list, screenshots that contain no sensitive content, and device/OS/configuration matrix. | No unresolved P0/P1 usability or accessibility finding before Phase 7 completion. |

Card sorting is used to discover users’ mental models and possible information groupings, while tree testing evaluates an established or proposed hierarchy; they must not be substituted for one another.[1] A moderated implementation may capture why participants grouped or searched in a particular way, but data collection remains de-identified and outside the product runtime.[2]

## Participants, Consent, and Data Minimization

Recruit **5–8 representative voluntary participants** for the initial qualitative pass, with at least one person who regularly uses technical/AI tooling and, where feasible, at least one participant who uses Android accessibility services. Recruitment may occur through private invitation or a third-party research service, but no participant needs an IVAI account because IVAI has no mandatory account system.

Before beginning, obtain an explicit verbal or written acknowledgement that the session is voluntary, that no real keys/files/prompts are required, and that the facilitator will record only anonymous notes. Use a neutral ID such as `P01`; keep the mapping to contact details, if one exists, outside the repository. Do not upload participant media or notes to the app, GitHub, or an external tool without the participant’s separate consent.

| Allowed study record | Prohibited study record |
|---|---|
| Anonymous participant ID, task outcome, first route, short non-sensitive quote/paraphrase, severity, and recommended change. | Name, email, phone number, device ID, account data, API key, credential, prompt, workspace file, run trace, screen recording, or raw network log. |

## Materials and Task Script

Use a debug build or static interactive prototype configured with mock/local-only data. Disable any real provider execution path or omit credentials entirely. The moderator must say: “This is a local prototype. Nothing you do will send a request, use a credential, or write a file.”

| Task | Participant prompt | Completion evidence |
|---|---|---|
| 1. Change target | “Show where you would review or change the model or Combo a chat will use.” | Navigates deliberately to Connections/target selection and can state that no target is chosen automatically. |
| 2. Review trust | “Show where you would review an endpoint’s HTTPS trust setting before saving a connection.” | Finds the explicit provider setup/review path and identifies the user confirmation point. |
| 3. Create Combo | “Show where you would create and review an ordered fallback Combo.” | Finds Connections and the Combo review route without expecting automatic discovery. |
| 4. Approve write | “An Agent asks to write a project file. Explain what you would check and what Allow once means.” | Identifies bounded preview/path and one-time decision; does not infer always-allow. |
| 5. Recover/project chat | “A target fails. Show how you would recover, then begin a chat in a selected local project.” | Finds recovery/edit-target route and explicit project-chat action without claiming that it sends a message automatically. |

The card-sort deck contains: **Provider, Account, Endpoint trust, Model, Combo, Project, Agent, Run, Chat, Settings**. Do not include terms that expose implementation details or secret-management internals. The tree test uses the five task prompts above, randomizes task order, and records direct success separately from recovery; card-sort results suggest possible labels, whereas tree-test results test findability of the proposed labels.[1]

## Heuristic Review Checklist

Two reviewers independently inspect the five destinations—Chat, Agents, Workspace, Connections, and Settings—using the following checklist. They reconcile only after recording independent findings.

1. The active target and project context are visible before an action that can execute work.
2. The user can stop or recover from an operation without a hidden retry or automatic change of target.
3. Provider, endpoint trust, model, and Combo choices are explicit and provider-neutral.
4. Write approval names the consequence, bound/path, and one-time nature of the decision.
5. Empty, loading, offline, error, and success states explain what happened without fabricated telemetry or futuristic/vague copy.
6. Headings, control labels, state descriptions, touch targets, BiDi flow, contrast, and live announcements are present where relevant.

| Severity | Definition | Phase 7.5 disposition |
|---|---|---|
| P0 | Prevents task completion, could cause an unsafe action, exposes a secret, or breaks the Local-first/BYOK boundary. | Block Phase 7 completion and Alpha. Fix, retest, and obtain fresh evidence. |
| P1 | Causes a material misunderstanding of target/approval/recovery, excludes a task-critical accessibility path, or materially degrades a primary task. | Block Phase 7 completion until fixed and retested. |
| P2 | Noticeable friction with a workaround and no safety/architecture impact. | Record, prioritize, and accept only with owner decision. |
| P3 | Cosmetic or wording refinement with no task/safety impact. | Record for a later focused increment. |

## Physical-Device and Accessibility Matrix

Automated Compose semantics and Roborazzi evidence already provide deterministic coverage, but Android recommends combining automated testing with manual accessibility-service and user testing.[4] Run the following matrix on at least one compact and one medium physical Android device, recording only non-sensitive screenshots and anonymous notes.

| Dimension | Required checks | Pass condition |
|---|---|---|
| Installation lifecycle | Fresh install, upgrade from prior debug build, restart, local-data reset. | No crash, corrupted local state, silent migration loss, or unexpected execution. |
| Layout | Compact/medium, portrait/landscape, dark/light, default and large font scale. | No task-critical clipping, overlap, or unreachable control. |
| BiDi | System Force-RTL plus English/Persian/Arabic mixed content. | Natural UI direction remains RTL-safe; code/terminal/footer LTR exceptions remain bounded and readable. |
| TalkBack | Linear swipe order, spoken labels/states, live status, target controls, approval decision, destructive local-data action. | Every task-critical control is reachable once, has concise meaning, and announces relevant transient states. |
| Network-bound behavior | Offline handling plus allowed HTTPS loopback/private-LAN cancellation and timeout on a real device. | Safe failure/recovery; no HTTP, cleartext, discovery, scanning, implicit trust, or duplicate side effect. |

For TalkBack, inspect both linear swipe navigation and exploration by touch, checking that spoken feedback states the purpose of each control and that all primary workflows are reachable.[4]

## Evidence Files and Completion Rules

| Evidence | Repository location | Status at protocol creation |
|---|---|---|
| Automated regression and scan output | `docs/PHASE7_5_HARDENING_AUDIT.md` | To be created after low-risk hardening review. |
| Participant outcomes and findings | `docs/PHASE7_UIUX_VALIDATION.md` | Template to be created; no participant results fabricated. |
| Device/accessibility matrix | `docs/PHASE7_UIUX_VALIDATION.md` | Pending access to physical devices and voluntary participants. |
| Local no-backup hardening evidence | `docs/PHASE7_5_HARDENING_AUDIT.md` and tests/lint | Pending focused implementation/review. |
| Alpha release decision | `docs/ALPHA_RELEASE.md` | Remains **not approved** until all P0 gates and owner approval are satisfied. |

Phase 7.5 can be marked complete only after actual, de-identified study findings and physical-device evidence are recorded, all P0/P1 findings are closed, the full regression gate is green, and no Local-first/Backendless/BYOK invariant regresses. Preparing this protocol alone is not completion.

## References

[1] [Nielsen Norman Group, “Card Sorting vs. Tree Testing”](https://www.nngroup.com/articles/card-sorting-tree-testing-differences/)

[2] [Digital.gov, “Open-source Information Architecture Design: Using the Tools You Have To Conduct Card Sorting and Tree Testing”](https://digital.gov/2022/01/06/open-source-information-architecture-design-using-the-tools-you-have-to-conduct-card-sorting-and-tree-testing)

[3] [Android Developers, “Back up user data with Auto Backup”](https://developer.android.com/identity/data/autobackup)

[4] [Android Developers, “Test your app’s accessibility”](https://developer.android.com/guide/topics/ui/accessibility/testing)
