# Phase 7.5 — Controlled Scenario Cards

> **Status:** Facilitator-only research material. These cards are static, local-only prompts; they are **not** product screens, runtime data, test results, or evidence that an Agent, Provider, endpoint, or file write has executed.
>
> **Boundary:** Present a card only to establish a hypothetical context for navigation and safety-comprehension tasks. Do not create the described connection, credential, target, Combo, Agent, run, approval, failure, project file, or chat content in the application. Do not enter a credential, contact an endpoint, send a provider request, or perform a file write.

## Use Rules

The facilitator gives the participant one card at a time in the randomized order specified by the [Phase 7.5 Runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md). The participant may read the card, ask about an unfamiliar word, and explore the clean controlled build. Before the participant's first route attempt, the facilitator must not identify a destination, UI control, Provider, model, or recovery path.

These cards intentionally use generic labels and no real values. They must remain outside product storage and must never be interpreted as an installed app state. Record only the task outcome, first route, and a short de-identified observation in the local worksheet.

| Card | Intended method | App data created? | Network, credential, or file operation? |
|---|---|---:|---:|
| A. Explicit target | Navigation and comprehension | No | No |
| B. HTTPS trust | Navigation and comprehension | No | No |
| C. Ordered Combo | Navigation and comprehension | No | No |
| D. One-time write review | Safety comprehension | No | No |
| E. Failed-target recovery | Recovery/navigation comprehension | No | No |

## Card A — Explicit Target Context

> A local chat has not selected a target yet. Before writing a message, you want to review or change the model or ordered Combo that this chat would use. Nothing is selected automatically.

**Facilitator prompt:** “Show where you would review or change the model or Combo a chat will use.”

**Observe without coaching:** Whether the participant looks for an explicit target-selection route and states that a Provider, model, or Combo is not chosen automatically.

## Card B — HTTPS Trust Context

> You are considering a new connection. Before saving it, you want to understand the HTTPS trust choice. No endpoint is configured and no network connection will be made.

**Facilitator prompt:** “Show where you would review an endpoint’s HTTPS trust setting before saving a connection.”

**Observe without coaching:** Whether the participant finds the explicit setup/review flow and identifies a user confirmation point rather than assuming automatic discovery or trust.

## Card C — Ordered Combo Context

> You want to prepare an ordered fallback Combo for later use. There are no saved accounts or models in this controlled build, and no Provider will be contacted.

**Facilitator prompt:** “Show where you would create and review an ordered fallback Combo.”

**Observe without coaching:** Whether the participant finds the Combo creation/review route and understands that ordering and target choices are explicit rather than discovered or selected automatically.

## Card D — One-Time Write Review Context

> **Hypothetical only:** An Agent proposes one local project-file change. The proposed path and preview would be visible before any decision. The operation has not run, and no file exists or will be changed in this session.

**Facilitator prompt:** “An Agent asks to write a project file. Explain what you would check and what **Allow once** means.”

**Observe without coaching:** Whether the participant identifies the visible path/preview, understands the decision is bounded to one reviewed operation, and does not infer a remembered or always-allow permission.

## Card E — Failed-Target Recovery Context

> **Hypothetical only:** A selected target reports a safe failure. Nothing retries automatically, no target changes automatically, and no message is sent. You want to recover deliberately and then begin a chat from a selected local project.

**Facilitator prompt:** “A target fails. Show how you would recover, then begin a chat in a selected local project.”

**Observe without coaching:** Whether the participant seeks an explicit recovery/edit-target route and an explicit project-chat action, without claiming that an Agent or Provider will retry, choose another target, or send a message automatically.

## Facilitator Stop Rule

Stop the affected scenario immediately if the participant proposes entering a credential, creating a real endpoint connection, sending a message, initiating a Provider request, or allowing a real file write. Record only a de-identified safety finding and severity. A misunderstanding that could lead to unsafe target, approval, or recovery behavior is a P1 under the [authoritative protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md).

## References

[1] [Phase 7.5 Usability and Heuristic Review Runbook](PHASE7_5_USABILITY_HEURISTIC_RUNBOOK.md)

[2] [Phase 7.5 Controlled Research and Device Field Kit](PHASE7_5_FIELD_KIT.md)

[3] [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)
