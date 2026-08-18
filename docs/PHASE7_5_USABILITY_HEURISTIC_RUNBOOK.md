# Phase 7.5 — Usability and Heuristic Review Runbook

> **Status:** Execution plan only. This runbook records no participant, device, task, or defect result.
>
> **Authority:** [UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md) defines the evidence rules and release gates. This runbook explains how the team executes that protocol consistently.

## 1. Objective and Release Boundary

The purpose of the run is to collect real, de-identified evidence that IVAI users can find the five task-critical destinations and understand the safety-critical behavior of explicit target selection, endpoint trust, ordered Combo setup, one-time write approval, and failed-target recovery. The run must not add telemetry, analytics, session replay, product instrumentation, a backend, or a runtime/data-layer change.

> A successful build, a facilitator script, or a completed spreadsheet is **not** a Phase 7.5 pass. Phase 7 and public Alpha remain blocked until actual evidence is recorded, every P0/P1 finding is closed and retested, the final deterministic quality gate is green, and the release owner approves the signed artifact.

## 2. Roles and Separation of Duties

| Role | Responsibility | Must not do |
|---|---|---|
| Research owner | Confirms scope, recruitment, consent process, data-retention location, and final evidence completeness. | Enter participant identity/contact mapping into the repository. |
| Facilitator | Runs the fixed opening statement and randomized tasks without route hints before the first attempt. | Add a credential, trigger a provider request, initiate a write, or coach a participant to a route. |
| Observer/note-taker | Records only anonymous outcome, first route, short non-sensitive observation, finding ID, and severity. | Record screens, raw logs, prompts, files, device identifiers, or participant identity. |
| Heuristic reviewer A/B | Independently inspects the five destinations before reconciliation. | Coordinate conclusions before each reviewer records independent findings. |
| Release owner | Determines whether all release gates are closed and controls signing/publication. | Treat a debug APK or unverified field note as an Alpha release approval. |

## 3. Recruitment, Consent, and Data Minimization

Recruit **5–8 voluntary participants** for the first qualitative pass. Include at least one person who routinely uses technical or AI tooling and, where feasible, one person who uses Android accessibility services. Use anonymous IDs (`P01`–`P08`), retain any contact mapping only outside the repository, and do not require an IVAI account.

Before every session, the facilitator reads the following statement and records only whether the participant acknowledged it:

> “This is a local prototype. Participation is voluntary. Nothing you do will send a request, use a credential, or write a file. We will not collect your name, contact details, device ID, prompts, files, or recordings. You may stop at any time.”

| Record only | Never record |
|---|---|
| Anonymous ID; task outcome; first route; short de-identified paraphrase; finding ID; severity; safe device class/Android version. | Name, email, phone number, device ID/serial/Android ID, account data, API key, credential, prompt, workspace file, run trace, screen recording, raw network log, or identity mapping. |

## 4. Controlled Build and Session Setup

Use the package produced by `./scripts/prepare_phase75_research_build.sh`. Before each session, confirm that `RESEARCH_PACKAGE_MANIFEST.txt` declares the intended commit/debug-only status and run `sha256sum --check SHA256SUMS.txt` for every transferred artifact, then clear local app data. The app must contain no account, credential, connection, endpoint, model, Combo, Agent target, project file, or prior chat data. The session is local-only: do not send provider traffic, use local endpoint networking, or let an Agent write a file.

| Preflight check | Required result | Owner |
|---|---|---|
| Build provenance | Package manifest declares the intended research commit and debug-only status. | Facilitator |
| Artifact integrity | `sha256sum --check SHA256SUMS.txt` succeeds for every package artifact. | Facilitator |
| Quality provenance | Build-quality log is present for the controlled package. | Facilitator |
| App state | Fresh/cleared local state with no credentials or personal content. | Facilitator |
| Safety notice | Participant has heard the opening statement and may stop. | Facilitator |
| Evidence record | Blank local worksheet and de-identified repository template are ready; identity mapping remains outside repository. | Note-taker |

## 5. Participant Session Script

Use one moderated session per participant. Randomize the five task prompts; do not reveal the intended route before the participant’s first attempt. A facilitator may clarify the words in a prompt but must not suggest a destination, control, provider, model, or target.

| Step | Suggested duration | What happens | Record |
|---|---:|---|---|
| Opening and consent | 3–5 minutes | Explain local-only prototype, voluntary participation, and no data/credential/file use. | Anonymous ID and consent acknowledgement only. |
| Open card sort | 10–15 minutes | Present Provider, Account, Endpoint trust, Model, Combo, Project, Agent, Run, Chat, and Settings. Participant names/groups cards freely. | Group labels, ambiguous cards, and non-sensitive observation. |
| Tree test | 10–15 minutes | Ask the five navigation prompts in randomized order without route hints. | First route; Direct, Recoverable, or Failure; short reason. |
| Moderated safety tasks | 15–20 minutes | Run the same five prompts using mock/local-only state and ask for explanation of target/approval/recovery. | Task result; paraphrase of safety understanding; rescue required; finding ID. |
| Short debrief | 3–5 minutes | Ask what was unclear or unexpected without soliciting personal data. | Optional non-sensitive observation. |

The five fixed task prompts are: (1) review/change a chat model or Combo target; (2) review an endpoint’s HTTPS trust setting before saving; (3) create/review an ordered fallback Combo; (4) explain an Agent write preview and **Allow once**; and (5) recover from a failed target then begin chat in a selected local project.

## 6. Outcome Coding and Thresholds

Use the same coding for every participant. A direct tree-test success reaches the intended location without a hint or recovery detour. A recoverable success reaches it only after a detour; it must not be counted as direct. A safety-comprehension rescue means the participant needed facilitator intervention to explain the active target, one-time approval, or recovery.

| Measure | Formula | Gate |
|---|---|---|
| Navigation-label agreement | Participants placing a primary concept in intended group ÷ valid card-sort participants | **≥70%** for Chat, Agents, Workspace, Connections, and Settings, or revise/retest. |
| Direct task success | Direct successes for a task ÷ valid participants receiving that task | **≥80%** for each of the five critical tasks, or revise/retest. |
| Safety comprehension | Count participants independently explaining active target, Allow once, and failed-target recovery | Every participant must demonstrate all three without unsafe misunderstanding. |
| Heuristic severity | Highest confirmed severity after independent review and reconciliation | No unresolved **P0/P1** before Phase 7 completion. |

If a participant stops early or a setup defect invalidates a task, record the reason as non-sensitive and exclude that response from the denominator only when the research owner documents the reason. Never convert missing data into success.

## 7. Heuristic Review Procedure

Reviewer A and Reviewer B independently inspect **Chat, Agents, Workspace, Connections, and Settings** in the controlled build. Each reviewer assesses all six checks below before any reconciliation meeting.

| Check | Pass expectation |
|---|---|
| Visible context | Active target and project context are visible before an action that can execute work. |
| Control and recovery | Stop/recovery is possible without hidden retry or automatic target change. |
| Provider neutrality | Provider, trust, model, and Combo choice are explicit and provider-neutral. |
| Write approval | Consequence, bound/path, preview, and one-time decision are named. |
| Honest state copy | Empty/loading/offline/error/success states explain what happened without fabricated activity or telemetry. |
| Accessible interaction | Headings, labels, state descriptions, touch targets, BiDi flow, contrast, and live announcements are meaningful where relevant. |

Reconcile only after both worksheets are complete. Create one finding ID per distinct problem and record destination, heuristic, severity, de-identified evidence, proposed fix, and retest result in `docs/PHASE7_UIUX_VALIDATION.md` only when the summary is fully de-identified and approved for repository entry.

## 8. Severity, Stop Rules, and Remediation

| Severity | Definition | Required action |
|---|---|---|
| P0 | Prevents task completion, causes unsafe action, exposes a secret, or breaks Local-first/BYOK boundary. | Stop the affected scenario; block Phase 7 and Alpha; make a focused remediation; rerun deterministic tests and collect fresh task/device evidence. |
| P1 | Materially confuses target/approval/recovery, excludes critical accessibility path, or materially degrades a primary task. | Block Phase 7; create focused remediation and retest before release review. |
| P2 | Noticeable friction with a safe workaround. | Record and prioritize; owner may accept only with explicit decision. |
| P3 | Cosmetic or wording refinement with no safety/task impact. | Record for a later focused increment. |

A participant must never be asked to enter a credential, use a real endpoint, create a real provider request, or perform a real file write to investigate a finding. If a safety-critical misunderstanding occurs, end that scenario and record only the de-identified observation.

## 9. Physical-Device Sweep Coordination

Run the device matrix separately but against the same controlled build and evidence rules. Use at least one compact and one medium Android device. Required checks are fresh install, upgrade, restart, rotation, local-data reset, dark/light, default/large font, Force-RTL, TalkBack swipe/explore-by-touch, offline handling, and explicit HTTPS loopback/private-LAN cancellation/timeout. A screenshot is permitted only if it contains no sensitive content.

## 10. Evidence Handoff and Completion

1. The facilitator verifies package provenance before the session, then transfers only approved de-identified aggregate outcomes from the local worksheet to the validation record; raw notes, checksum verification material, and any identity mapping remain outside the repository.
2. The research owner calculates agreement/direct-success rates and marks every gate Pass, Pending, or Blocked without inference.
3. Any P0/P1 creates a focused code/documentation remediation PR and a matching retest plan; do not mix remediation with unrelated provider/data/runtime work.
4. After evidence passes, rerun the [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md) on the final candidate.
5. Only after all field/device gates, deterministic checks, signing, checksum, tag, release notes, and owner approval pass may a signed APK be attached to a GitHub Release.

## References

[1] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[2] [Phase 7.5 Field Kit](PHASE7_5_FIELD_KIT.md)

[3] [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md)

[4] [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
