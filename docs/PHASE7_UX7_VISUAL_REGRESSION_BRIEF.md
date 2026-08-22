# Phase 7 UX-7 — Visual Regression, Semantics and Validation Runbook

**Status:** In progress on a focused branch.

## Goal

Close the deterministic UX-rebuild evidence gap after UX-1 through UX-6. UX-7 strengthens cross-cutting screenshot and semantic contracts for the five-destination shell, validates that navigation remains singular and understandable, and records a concise handoff runbook for the physical UX-8 matrix.

## Scope

| Area | UX-7 decision |
|---|---|
| Navigation semantics | Assert the product sidebar exposes exactly the five primary destinations, preserves the selected destination and keeps Chat history context-owned. |
| Visual regression | Add representative Roborazzi evidence for the selected/unselected navigation state in both light and dark themes and retain existing destination screenshots as the per-surface baseline. |
| Accessibility baseline | Assert headings, selected state and critical navigation labels through Compose semantics without adding global LTR behavior or substituting test tags for accessible labels. |
| Field handoff | Record the physical UX-8 matrix and the distinction between deterministic evidence and unperformed device/usability validation. |

## Deliberately unchanged

UX-7 does not alter destination routing, sidebar responsive implementation, Chat history ownership, page composition, any Agent/Workspace/Settings behavior, target selection, provider/model/Combo flow, Agent runtime, approval rules, Room schema, vault, transport, backend, telemetry, signing, release status or physical-device evidence.

## Acceptance gate

| Evidence | Pass definition |
|---|---|
| Single navigation model | Compact and persistent sidebar variants retain exactly five primary destinations; Chat history appears only in Chat context. |
| Semantics | Selected navigation item is exposed as selected with a readable destination label; the wordmark and local-control descriptor remain visible. |
| Visual evidence | New light and dark navigation screenshots are recorded from deterministic Compose tests without fabricated device claims. |
| Architecture | BYOK/provider-default/prohibited-execution/cleartext/global-RTL guards remain clean; no runtime or data change is introduced. |
| Quality gate | Secret scan, debug/release build, unit suite and lint succeed in protected CI. |

## Physical UX-8 handoff

The next phase requires real evidence for compact and medium devices, at least one OEM launcher, keyboard/IME open-close, rotation, light/dark perception, Force-RTL, TalkBack, font scale, offline and approved HTTPS loopback/private-LAN cancellation/timeout. The device operator must record actual observed outcomes only. No UX-7 result is treated as a substitute for this matrix.
