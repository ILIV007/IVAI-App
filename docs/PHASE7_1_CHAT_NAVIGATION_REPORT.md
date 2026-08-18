# Phase 7.1 — Adaptive Navigation and Chat Foundation

> **Status:** Validated locally and ready for an independent pull request. This phase is a **UI-only shell and Chat Foundation change**. It deliberately does not alter provider adapters, endpoint policy, credentials, router selection, agent execution, persistence schema, data migration, or network behavior.

Phase 7.1 applies the Phase 7 blueprint to the highest-frequency product flow: entering the app, understanding the current execution target, recovering from an empty or unconfigured state, and safely composing a Chat request. The design retains IVAI’s local-first and BYOK posture by making a user-managed target explicit rather than inventing a default provider or automatically selecting a model.

## Delivered UI contracts

| Area | Implemented contract |
|---|---|
| Adaptive shell | The five existing destinations now adapt by available width: a compact bottom bar below `600dp`, a medium rail from `600dp` to `839dp`, and an expanded rail from `840dp`. Existing destination identifiers are preserved. |
| Navigation vocabulary | User-facing labels are **Chat**, **Agents**, **Workspace**, **Connections**, and **Settings**. The underlying `NavDestination` routes and test tags remain stable. |
| Chat-only history | Conversation history moved to a dedicated Chat Session Drawer with local search, project filtering, thread selection, local thread deletion, and new-chat creation. It is not a global navigation drawer. |
| Target-first Chat | One context row surfaces the selected Combo or direct target and project context before a user sends a message. A target is never inferred or auto-selected. |
| Safe onboarding | The screen presents the next safe action for each state: create a chat, open Connections when no target is available, choose an explicit target, or begin a ready chat. |
| Composer state | Sending is disabled without an explicit target. During streaming, the action changes to a semantic, text-labelled **Stop** control with the description `Stop streaming`. |
| Brand boundary | The top bar uses an independent IVAI wordmark. Launcher artwork is not reused in in-app UI. |

## Accessibility and testability

The new interactive surfaces are accompanied by stable test tags and, where icon-only or non-textual, meaningful content descriptions. Important examples include `button_open_chat_sessions`, `chat_session_drawer`, `chat_session_search`, `button_send_message`, `ivai_wordmark`, and the unchanged `nav_item_*` destination tags. The streaming test asserts both the visible `Stop` label and the `Stop streaming` semantics, so the control cannot silently regress into an unlabeled or inaccessible action.

| Coverage | Evidence |
|---|---|
| Navigation breakpoints | `ChatFoundationTest` verifies compact, medium, and expanded cutovers at `599dp`, `600dp`, `839dp`, and `840dp`. |
| Safe Chat states | `ChatFoundationTest` verifies no-thread onboarding, no-target routing to Connections, and target-ready streaming. |
| Streaming affordance | The target-ready streaming test asserts the execution banner, composer, `Stop` label, `Stop streaming` description, and records a dark-theme Roborazzi capture. |
| Existing navigation regression | `ExampleRobolectricTest` was updated from the removed global-sidebar toggle to the persistent compact destination bar on Pixel 8. It verifies all five destinations and returns to Chat. |
| Shell screenshot | `IvaiScreenshotTest` verifies the visible IVAI wordmark and its content description on the adaptive main shell. |

## Visual review

A dark-theme record-mode capture of the streaming scenario was reviewed at `app/build/roborazzi/phase71_chat_streaming_dark.png`. It confirms the target-first hierarchy, a visible streaming status banner, transcript/composer separation, and mixed Persian/Arabic/English message content. The review originally found the compact icon-only stop glyph unreliable in the renderer; the action was changed to an explicit text-labelled **Stop** control and test assertions now enforce its visual and semantic contract.

## Validation record

| Gate | Result |
|---|---|
| Clean debug build | `./gradlew clean assembleDebug` succeeded. |
| Unit test suite | `./gradlew testDebugUnitTest` succeeded with **90 tests**, **0 failures**, **0 errors**, and **0 skipped**. |
| Lint | `./gradlew lintDebug` succeeded with **0 Error/Fatal** findings. The report contains 19 non-fatal warnings, unchanged as a release-blocking threshold because the project gate is zero Error/Fatal. |
| Whitespace | `git diff --check` passed. |
| Secret scan | No common provider credential pattern or `sk-` credential pattern was found in `app/src/main`. |
| Transport/trust scan | No new cleartext-traffic exception, trust-all implementation, or `X509TrustManager` use was found in `app/src/main`. |
| Launcher-only scan | No launcher brand reference was found in Java/Kotlin production or test sources. |

## Regression resolved during validation

The first clean validation surfaced one stale assertion in `ExampleRobolectricTest`: it still expected the removed `button_sidebar_toggle` control. The test was updated to verify the compact adaptive destination bar and to click persistent `nav_item_*` controls directly. The focused regression test passed, followed by a new clean build, complete unit-test run, and lint run.

## Explicitly deferred

This phase intentionally leaves the following work to subsequent Phase 7 increments: the complete Provider and Combo Setup redesign (Phase 7.2), detailed Workspace and Agent experience redesign, device-level TalkBack and Force-RTL evidence, physical-device font-scale testing, and any release packaging work. No analytics, session replay, embedded survey, remote backend, provider discovery, LAN scanning, cleartext exception, or automatic network operation is included.

## Related planning documents

The implementation follows the established [Phase 7 UI/UX Blueprint](PHASE7_UIUX_BLUEPRINT.md) and [Phase 7 UI/UX Execution Plan](PHASE7_UIUX_EXECUTION_PLAN.md). The next planned increment is **Phase 7.2 — Provider and Combo Setup Experience**, delivered through its own independent pull request.
