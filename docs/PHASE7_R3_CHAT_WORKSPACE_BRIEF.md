# Phase 7 R3 — Chat Workspace Rebuild

**Status:** Implemented on a focused branch; physical-device Chat validation remains pending.

## Goal

Make the Chat workspace calmer and more purposeful without weakening the existing explicit-target, streaming, cancellation, partial-response, local persistence or Provider/Router contracts.

## Scope

| Area | Decision |
|---|---|
| Composer | The generic outlined field is replaced with a dedicated semantic composer surface. Its empty prompt is **“Do anything…”** and the send icon has a stable accessible action label. |
| Drafting | A user can type a local draft before a target is chosen. IVAI does not send, select a Provider, or select a Model automatically. A draft send attempt opens explicit target selection when options exist, or Connections when none exists. |
| Target selection | The selection bottom sheet now has an explicit test surface tag. Its user-facing rule remains unchanged: targets are per chat and Combos use only user-defined order. |
| Send and stream | The send icon remains visible with a semantic disabled color before there is actionable content/target. During a stream the control remains the explicit `Stop streaming` action. |
| Conversation context | The redundant second conversation-options button is removed; the remaining target chip and project assignment action retain their existing semantics. |

## Test-first evidence

The new product-prompt regression failed before the implementation because the old field showed a language-specific template placeholder. It now verifies **“Do anything…”**, the input tag and the send action. A second regression proves a typed draft without target opens the explicit target-selection surface rather than sending or auto-selecting a target. Existing Chat tests continue to cover no-thread/no-connection/no-target/ready/streaming states and visible incomplete assistant markers.

## Deliberately unchanged

This phase changes no message persistence, stream protocol, Router candidate ordering, Provider choice, data schema, credential handling, transport policy, project assignment semantics, sidebar history mechanics, Agent runtime, Settings behavior, signing or Alpha approval.

## Deterministic acceptance gate

```bash
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
```

The Chat test suite, semantic contrast validator, launcher validator, provider-neutral guard, RTL guard, Phase 8.0 readiness guard and both package-verifier regressions must pass.

## Evidence still required

A physical-device session must confirm long-message growth, IME/keyboard avoidance, send icon visibility in light/dark themes, stop behavior during a real user-managed stream, rotation, offline recovery, Force-RTL, TalkBack focus order and 200% font scale. This branch does not claim any of that evidence.
