# Phase 7 UX-5 — Chat Workspace, Context History and IME Rebuild

**Status:** In progress on a focused branch.

## Goal

Rebuild the Chat workspace around a clear target context, readable local history and a keyboard-safe composer. The composer must remain reachable while the IME is visible, keep the product prompt **“Do anything…”**, expose a semantic Send/Stop control, and preserve the user’s explicit target-selection flow.

## Scope

| Included | Decision |
|---|---|
| Chat layout | Establish a chat-local header/context strip, message workspace and bottom composer hierarchy without changing thread/session data. |
| Target context | Keep the selected direct Model or ordered Combo visible and actionable per chat; retain project assignment as chat metadata, not a target substitute. |
| Composer | Apply explicit `imePadding` and navigation-bar protection to the composer, bring a focused input into view, retain multiline constraints and expose Send/Stop semantics. |
| History context | Keep local conversation history in the Chat-owned sidebar section, separate from primary destinations; improve its labels only if required by the chat contract. |
| States | Preserve empty, no-target, streaming, incomplete response, offline and error flows with local recovery actions and no silent fallback. |
| Regression | Add tests for IME-safe composer markers, target context and send/stop behavior under the existing callbacks. |

## Deliberately unchanged

UX-5 does not add a provider default, target auto-selection, model discovery, transport request, provider/model test request, retry/fallback policy, stream lifecycle behavior, Room schema, thread persistence, credential/vault behavior, backend, telemetry, Agent logic, launcher resources, signing or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition |
|---|---|
| Composer visibility contract | Composer carries explicit IME and navigation-bar inset handling, and focused input uses a bring-into-view requester. |
| User-controlled target | Selected target remains explicit and target-less drafts open the existing target-resolution flow; no default is inferred. |
| Send/stop semantics | Empty draft stays disabled, drafted target-ready message exposes Send, and streaming exposes Stop with stable accessibility labels. |
| Recovery states | Empty/no-target/streaming/incomplete/error/offline presentations retain their existing state ownership and local recovery copy. |
| Quality gate | Secret scan, debug/release build, unit suite and lint succeed in protected CI. |

## Deferred validation

Physical keyboard adjustment, split-screen, rotation, font-scale, TalkBack, Force-RTL, streaming/network timeout and cancellation behavior remain device/field gates. This increment makes no claim that those physical validations have been executed.
