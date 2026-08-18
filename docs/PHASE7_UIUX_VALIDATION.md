# Phase 7 — UX Validation Record

> **Status:** Evidence collection pending. This document is a controlled record for actual results; no participant, device, task, or defect result has been fabricated.
>
> **Protocol:** [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)
>
> **Data minimization:** Use anonymous IDs only. Do not record names, contact details, device identifiers, API keys, credentials, prompts, workspace files, run traces, screen recordings, or raw network logs.

## Study Administration

| Field | Record when study begins |
|---|---|
| Research owner | Pending owner assignment |
| Build/prototype identifier | Pending |
| Package manifest and all-artifact checksum verification | Pending; retain locally outside the repository |
| Dates | Pending |
| Recruitment channel | Pending; must be voluntary |
| Consent acknowledgement method | Pending |
| Participant count | Pending; target 5–8 for the initial qualitative pass |
| Data retention owner/location | Pending; keep outside application runtime and repository unless de-identified approval is explicit |

Before entering any result, the research owner verifies the controlled package manifest and every checksum, retains the local worksheet outside the repository, and transfers only approved de-identified aggregates. A verified package is provenance preparation, not a completed participant or device gate.

## Open Card Sort

The card deck contains: Provider, Account, Endpoint trust, Model, Combo, Project, Agent, Run, Chat, and Settings. Participants may create or rename groups. Record category labels and disagreement without trying to force the product vocabulary.

| Anonymous ID | Group labels supplied | Ambiguous cards/terms | Notes without sensitive content |
|---|---|---|---|
| No results recorded | — | — | — |

### Aggregation

| Primary navigation concept | Participants agreeing with intended placement | Agreement rate | Meets ≥70%? | Decision / label change |
|---|---:|---:|---|---|
| Chat | Pending | Pending | Pending | Pending |
| Agents | Pending | Pending | Pending | Pending |
| Workspace | Pending | Pending | Pending | Pending |
| Connections | Pending | Pending | Pending | Pending |
| Settings | Pending | Pending | Pending | Pending |

## Tree Test

Randomize task order. Count a route as direct success only when the participant reaches the intended location without a moderator hint or recovery detour.

| Task | Direct successes | Recoverable successes | Failures | Direct success rate | Meets ≥80%? | Decision |
|---|---:|---:|---:|---:|---|---|
| Change/review target | Pending | Pending | Pending | Pending | Pending | Pending |
| Review endpoint trust | Pending | Pending | Pending | Pending | Pending | Pending |
| Create/review Combo | Pending | Pending | Pending | Pending | Pending | Pending |
| Explain write approval | Pending | Pending | Pending | Pending | Pending | Pending |
| Recover then start project chat | Pending | Pending | Pending | Pending | Pending | Pending |

## Moderated Safety-Comprehension Sessions

Use mock/local-only data and state aloud before the session that no credentials, network request, or file write is involved.

| Anonymous ID | Active target explained? | One-time write approval explained? | Failed-target recovery completed? | Moderator rescue required? | Finding ID(s) |
|---|---|---|---|---|---|
| No results recorded | — | — | — | — | — |

## Heuristic Review

Two reviewers record findings independently before reconciliation. Do not collapse distinct findings into a generic “pass.”

| Finding ID | Destination/flow | Heuristic/check | Severity (P0–P3) | Evidence | Proposed fix | Retest result |
|---|---|---|---|---|---|---|
| No results recorded | — | — | — | — | — | — |

## Physical-Device and Accessibility Sweep

| Device class / OS | Build manifest and checksum status | Configuration | Task-critical result | Non-sensitive screenshot/reference | Finding ID(s) |
|---|---|---|---|---|---|
| Compact physical device | Pending | Fresh install, upgrade, restart, rotation, offline; dark/light; default/large font; Force-RTL; TalkBack | Pending | Pending | Pending |
| Medium physical device | Pending | Fresh install, upgrade, restart, rotation, offline; dark/light; default/large font; Force-RTL; TalkBack | Pending | Pending | Pending |
| HTTPS local endpoint device test | Pending | Explicit loopback/private-LAN HTTPS only; cancellation, timeout, offline | Pending | Pending | Pending |

## Exit Decision

| Gate | Current state |
|---|---|
| IA labels meet card-sort threshold or have a documented retest | Pending |
| Each critical tree-test task meets direct-success threshold or has a documented retest | Pending |
| Safety comprehension is demonstrated without unsafe misunderstanding | Pending |
| No unresolved P0/P1 usability or accessibility finding | Pending |
| Device/Force-RTL/TalkBack/HTTPS endpoint evidence is complete | Pending |
| Full build, test, lint, screenshot, and architecture security gates pass on the final commit | Pending final candidate |
| Phase 7 redesign complete | **Not approved** |
| Public GitHub Alpha release | **Not approved**; see [ALPHA_RELEASE.md](ALPHA_RELEASE.md) |
