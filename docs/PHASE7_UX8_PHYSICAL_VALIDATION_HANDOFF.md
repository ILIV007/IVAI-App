# Phase 7 UX-8 — Physical Validation Handoff

> **Status:** Preparation only. No physical device, launcher, keyboard, accessibility, network, usability, signing, or release outcome is recorded by this document.
>
> **Authority:** This handoff supplements the [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md), [Field Kit](PHASE7_5_FIELD_KIT.md), and [UX Validation Record](PHASE7_UIUX_VALIDATION.md). It does not replace their consent, data-minimization, severity, or release-decision rules.

## Purpose

UX-1 through UX-7 supplied deterministic coverage for the rebuilt IVAI icon, foundation, single sidebar, Connection lifecycle, Chat/IME contract, local-workflow surfaces, and visual/semantic navigation evidence. UX-8 is the required **real-device handoff** for the results that JVM tests and screenshots cannot prove. It must be performed on a controlled debug package with fresh local application data and no real provider credentials, endpoints, prompts, workspace files, Agent writes, screen recordings, or device identifiers.

## Preconditions

| Requirement | Operator action | Evidence disposition |
|---|---|---|
| Build provenance | Prepare and locally verify a controlled debug package with `./scripts/prepare_phase75_research_build.sh` followed by `./scripts/verify_phase75_research_package.sh <package-dir>`. | Keep package manifest, checksum result, and raw reports locally; do not commit APKs or private artifacts. |
| Data safety | Clear application data before each configuration. Do not enter API keys, credentials, endpoints, prompts, files, Agent traces, or create actual file writes. | Record only de-identified aggregate observations in the validation record. |
| Device coverage | Use at least one compact and one medium physical Android device. Include one OEM launcher if available. | Write actual device class/OS only when the operator has consent to retain that non-sensitive information. |
| Result integrity | Record `Pass`, `Fail`, `Blocked`, or `Not run` per row. “Not run” is not a pass. | Open P0/P1 remediation before any Phase 7 or Alpha approval. |

## Required Matrix

| ID | Surface and configuration | Procedure | Pass condition | Actual result |
|---|---|---|---|---|
| UX8-01 | Adaptive icon: compact device, default launcher | Fresh-install the controlled package; inspect circular/squircle/rounded masks and wallpaper contrast. | VA symbol is centered, not cropped or zoomed, with no embedded square foreground. | Pending |
| UX8-02 | Adaptive icon: OEM/themed icon when supported | Enable supported themed icon and inspect launcher grid. | Monochrome/silhouette is legible and safe-zone aligned; no publisher/icon regression is introduced. | Pending |
| UX8-03 | Sidebar: compact portrait and landscape | Visit all five destinations; open/close the modal sidebar; change orientation. | Exactly one primary navigation model is reachable; no bottom navigation, duplicate rail, hidden route change, or selected-context loss. | Pending |
| UX8-04 | Sidebar: medium device portrait and landscape | Visit all five destinations and change orientation. | Persistent sidebar remains usable; content controls are not clipped, overlapped, or unreachable. | Pending |
| UX8-05 | Light and dark presentation | Inspect Chat, Connections, Agents, Workspace and Settings in both themes. | Text, selection, status and focus remain perceivable without relying only on color; no template-like unreadable light surface appears. | Pending |
| UX8-06 | Font scale | Repeat UX8-03 through UX8-05 at default and large system font scale. | Target, Send/Stop, Add connection, Agent approval, project controls, Appearance and delete confirmation remain reachable. | Pending |
| UX8-07 | Force-RTL and mixed script | Enable Force-RTL; inspect Persian/Arabic plus English endpoint/code content. | App shell and ordinary copy remain naturally BiDi-safe; only bounded technical/code/footer tokens use LTR. | Pending |
| UX8-08 | TalkBack | Use linear swipe and explore-by-touch on target strip, Send/Stop, Connection lifecycle, Agent approval, local-data confirmation and sidebar destinations. | Each task-critical control is reached once, has a concise spoken name/state, and transient status is understandable. | Pending |
| UX8-09 | Chat keyboard/IME | With a target-less draft and a ready target, open/dismiss software keyboard; try IME send and hardware Enter if available; rotate once. | Composer remains above navigation/IME, final message and Send/Stop remain reachable, and no action selects a target implicitly. | Pending |
| UX8-10 | Local safety and lifecycle | Fresh install, restart, local-data confirmation cancel/confirm in controlled state, and upgrade from a prior debug package if safely available. | No crash, silent migration/loss, unexpected execution, or deletion before explicit confirmation. | Pending |
| UX8-11 | Offline/network boundary | Exercise offline state only; test approved loopback/private-LAN **HTTPS** cancellation/timeout only when a controlled endpoint is available. | No HTTP/cleartext, discovery, scanning, implicit trust, hidden retry, or duplicate side effect; recovery remains explicit. | Pending |

## Route-Specific Heuristic Prompts

Use these prompts only in a controlled local scenario; do not let the operator enter real data or execute remote work.

| Prompt | Expected safe understanding | P0/P1 trigger |
|---|---|---|
| “Where would you change the model or Combo for this chat?” | The operator finds the explicit target route and can state that IVAI never chooses a provider/model automatically. | Active target cannot be found or is misunderstood as automatic. |
| “Where would you confirm endpoint trust before saving a Connection?” | The operator identifies user-controlled HTTPS trust review. | The interface suggests implicit trust, HTTP, or discovery. |
| “An Agent proposes a file write; what does Allow once mean?” | The operator identifies bounded preview/path and one-time approval. | The action appears permanent, automatic, or lacks consequence information. |
| “Where would you remove local data?” | The operator reaches Settings and sees a consequence dialog before deletion. | Data is deleted without confirmation or scope is misleading. |
| “How do you recover a failed target before chatting?” | The operator finds an explicit repair/change route rather than a hidden fallback. | The interface hides recovery or implies automatic fallback. |

## Recording and Triage

Transfer only a concise de-identified result into [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md). Keep raw notes, package manifests and checksums outside the repository. Any issue that blocks task completion, risks an unsafe execution, exposes sensitive data, hides an active target, bypasses one-time approval, or removes a critical accessibility route is **P0/P1** and blocks Phase 7/Alpha until a focused remediation and retest exist.

> A green GitHub workflow, Robolectric test, or Roborazzi screenshot is deterministic support—not real-device, launcher, TalkBack, keyboard, network, participant, signing, or release evidence.

## Exit Condition

UX-8 is complete only after actual, de-identified results cover every required applicable row, all P0/P1 findings are closed and retested, the final protected regression workflow is green, and the owner makes the independent signing/release decision. Until then, **Phase 7 redesign and public Alpha remain not approved**.
