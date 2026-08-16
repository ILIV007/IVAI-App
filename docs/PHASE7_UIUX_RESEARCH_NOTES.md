# Phase 7 UI/UX Research Notes

## Sources supplied by the user

| Source | Evidence collected | Design implication for IVAI |
|---|---|---|
| CSS Gradient – Gradient Backgrounds | The page is a curated directory of six free gradient tools (uiGradients, Grabient, Gradient Hunt, WebGradients, CoolHue, GradientButtons) and displays large, expressive palette samples. | Use gradients as **restrained ambient surfaces**: onboarding hero, empty-state halo, small status/identity accents. Do not use them beneath dense interactive content or text without a solid scrim and contrast verification. |
| Aura Design Systems | The requested URL currently redirects to Aura's general AI Website Builder page and supplied no readable design-system catalogue in the browser extraction. | Do not treat the catalogue as a source of specific reusable UI patterns. Use it only as a discovery lead; base the IVAI system on accessible Android Material 3 foundations and independently verified component patterns. |

## Initial product constraints

1. IVAI remains **Local-first, Backendless, and BYOK**. Phase 7 must redesign interaction and visual hierarchy only; it must not add a backend, telemetry, automatic provider selection, new provider adapters, or Agent tools.
2. The existing brand language is deep indigo (`#101432`), emerald/aqua (`#4EE8B5`), and aurora violet (`#AB6AF5`). Semantic color pairs must remain WCAG-AA compliant.
3. The UI must be genuinely RTL/BiDi-safe. Shell-level forced LTR is forbidden; narrow LTR is allowed only in code/protocol content and action/timestamp micro-layouts.
4. The interface must communicate user control: provider, account, endpoint, model, Combo, and Agent are all explicitly user-managed objects.

## Research log

- 2026-08-16: Browser-inspected `https://cssgradient.io/gradient-backgrounds/`.
- 2026-08-16: Browser-inspected `https://aura.build/design-systems/`; page redirected to `https://www.aura.build/design-systems/` and did not provide the expected readable catalogue.

## References

[1]: https://cssgradient.io/gradient-backgrounds/ "CSS Gradient – Gradient Backgrounds"
[2]: https://www.aura.build/design-systems/ "Aura – AI Website Builder"


## UX research and product analytics compatibility

| Tool / method | Verified capability | Fit for IVAI Phase 7 | Decision |
|---|---|---|---|
| Microsoft Clarity Mobile | Official documentation lists Android SDK, Jetpack Compose support, session recordings, heatmaps, labels, filters, masking, and integrations. | Technically applicable, but it is an external behavior-analytics service and therefore introduces telemetry/networking outside the product’s Backendless default. | **Do not integrate in Phase 7.** Consider only as a future, explicit opt-in research build after a separate privacy, consent, data-retention, and threat-model decision. |
| Matomo Android SDK | Official Matomo documentation confirms Android SDK support for screenviews, events, searches, custom dimensions and user identifiers; it also lists heatmaps and session recordings as unavailable for mobile SDKs. | Self-hosting may preserve data ownership, but it still requires an analytics server and outbound telemetry, violating the current Backendless scope. | **Do not integrate in Phase 7.** Possible future owner-hosted, explicit opt-in analytics decision, not an in-product default. |
| OpenReplay Mobile | Vendor documentation/search evidence identifies Android mobile session replay and custom capture controls. | Requires deployment of a replay service and has elevated risk because IVAI handles provider endpoint metadata and potentially sensitive prompts. | **Exclude from Alpha and Phase 7.** Only revisit with explicit consent, field masking, local trace threat model, and separate infrastructure decision. |
| Formbricks / LimeSurvey / Google Forms | External survey mechanisms suitable for recruitment and feedback collection. | Useful for **pre-design discovery** and **post-prototype feedback** without embedding an SDK in the application. | **Use outside the app** with voluntary, anonymized questionnaires; do not embed during Phase 7. |
| kardSort / xSort / equivalent card sorting | Card sorting is a research method for discovering participants’ mental models and information architecture. | Strong fit for determining names and grouping for Provider, Router, Agent, Project, and Settings navigation before visual implementation. | **Use before IA implementation**; use a hosted study or a moderated spreadsheet-based equivalent. |

### Implication

For IVAI, the recommended research stack separates **research operations** from **product runtime**. Use anonymous surveys, moderated task tests, and card sorting outside the application to inform the redesign. Preserve the core product’s Local-first/Backendless default; no session replay, heatmap, analytics SDK, feedback SDK, or automatic network call is added in Phase 7.

## Additional references

[3]: https://learn.microsoft.com/en-us/clarity/mobile-sdk/ "Clarity for Mobile Apps — Microsoft Learn"
[4]: https://matomo.org/faq/general/what-features-of-matomo-analytics-are-supported-when-tracking-mobile-app-analytics-using-android-or-ios-sdk/ "Matomo mobile SDK feature support"
[5]: https://openreplay.com/product/feature/mobile/ "OpenReplay Mobile Session Replay"
[6]: https://www.nngroup.com/articles/card-sorting-definition/ "Card Sorting: Uncover Users' Mental Models — Nielsen Norman Group"


## Android design-system and accessibility evidence

| Source | Verified guidance | Applied decision for IVAI |
|---|---|---|
| Material 3 in Compose | `MaterialTheme` propagates color scheme, typography and shape changes through M3 components; official guidance also supports custom light/dark schemes and adaptive UI direction. | Keep IVAI brand tokens as the primary source of truth rather than importing visual styles ad hoc. Build every screen from a small tokenized component layer over Material 3. Preserve dedicated, brand-authored light and dark schemes rather than enabling wallpaper dynamic color, which could dilute the fixed IVAI identity. |
| Compose Semantics | Semantics provide meaning to accessibility services and tests; default controls carry roles, while custom components need deliberate semantic properties. State descriptions, headings, live regions, and custom actions are available as needed. | Each Phase 7 component needs a semantic contract: role, label/contentDescription, stateDescription for connection/model/run state, heading markers for screen sections, and test tags only as stable test hooks. Use polite live regions only for terminal Agent/stream status—not token-by-token streaming updates. |

### Mandatory design-system consequences

1. Implement an explicit **IVAI token layer** for semantic colors, type, shape, spacing, elevation, icon size, and motion duration. No raw hex or arbitrary dp values in product screen implementations.
2. Use expressive brand gradients only as decorative, non-essential layers; all text, controls and state must remain understandable in a solid-color fallback.
3. Build compact/medium/expanded composition rules, but do not fork product behavior by device size. The same user-controlled provider and Agent entities must remain visible and actionable on each size class.
4. Create screenshot and semantics regression coverage for empty, loading, streaming, error, disconnected, approval-required, and success states—not merely static happy paths.

## Additional references

[7]: https://developer.android.com/develop/ui/compose/designsystems/material3 "Material Design 3 in Compose — Android Developers"
[8]: https://developer.android.com/develop/ui/compose/accessibility/semantics "Semantics — Android Developers"


## Trustworthy Agent and chat UX evidence

| Principle | Evidence | IVAI translation |
|---|---|---|
| Visibility of system status | NN/g defines the principle as keeping users informed through appropriate feedback in reasonable time; feedback prevents duplicate actions and reduces uncertainty. | Replace generic spinners with a persistent execution status: selected target → connecting → streaming / step N → completed / stopped / failed. Disable duplicate send only while the same request is active and always expose Stop. |
| Identity transparency | NN/g recommends a persistent AI indicator. | Maintain a persistent `AI Agent` or `Direct chat` identity chip, plus the active provider/model or Combo label. Do not pretend the system is human. |
| Capability and limitation transparency | NN/g recommends explaining capability limits and surfacing capabilities contextually rather than overwhelming users with an exhaustive tutorial. | Empty states and contextual hints should explain only the next useful step: `Add a provider`, `Select a Combo`, `Allow once to write`, or `Review run trace`. Display bounded tool policy in the Agent profile, not in every chat message. |
| Rationale and recovery | NN/g emphasizes explaining judgment/denial where relevant and offering an alternative. | Provider failure UI must say **which user-selected target failed**, human-readable reason category (auth, timeout, offline, rate limit), whether fallback was attempted, and safe next actions. Approval rejection must keep the proposed write visible with `Denied — no file changed`. |

### Chat and Agent interaction rules

1. The composer shows the target name before send. It never silently chooses Gemini, OpenRouter, a model, Combo, or credential.
2. Streaming messages use a stable status row and a Stop action. The status is visible and semantic, but token-by-token content is not announced as a live region.
3. Agent runs expose a step timeline: planned → running → waiting for approval → completed / canceled / failed. Each step opens a safe trace summary; raw credentials and file observations remain unavailable.
4. The write-approval sheet shows affected project file, diff/preview, reason, `Allow once`, and `Deny`. It never offers permanent approval.
5. Errors are recoverable by design: retry selected target, edit connection, choose another explicit target, or stop. No automated fallback changes a user’s saved configuration.

## Additional references

[9]: https://www.nngroup.com/articles/visibility-system-status/ "Visibility of System Status — Nielsen Norman Group"
[10]: https://www.nngroup.com/articles/dimensions-of-ai-chatbots/ "The 5 Dimensions of Site-Specific AI Chatbots — Nielsen Norman Group"


## Current IVAI UX audit — code and baseline evidence

### What already works and must be preserved

| Area | Existing behavior to preserve |
|---|---|
| Chat targeting | The user can select a direct target or Combo and assign the thread to a project. The selected target is never implicitly chosen. |
| Provider safety | Provider setup exposes preset, custom endpoint, trust mode, account label, model ID, declared capability, credential/no-auth selection and local trust confirmation. The vault and no-discovery constraints are communicated. |
| Router control | Combos are explicit ordered candidate lists; existing cards expose position, provider, account, model and capability. |
| Agent safety | Profiles select an explicit target and bounded tools; run cards, trace cards and a one-time file-write approval UI already exist. |
| State coverage | Chat already has empty, loading, offline and error preview paths; status and message semantics already have some stable test tags. |

### Primary UX friction and redesign opportunity

| Finding | Evidence | Design response |
|---|---|---|
| Chat header contains too many independent horizontal control rows before any conversation. | `ChatsScreen` currently has separate target/project, project filter, and thread-chip rows. The baseline shows multiple chips/actions competing above the empty state. | Collapse context into one compact **Conversation Context Bar**: Target chip + Project chip + overflow. Move project and thread browsing into a dedicated full-height drawer or sheet. |
| Empty chat state has no single clear onboarding decision when no execution target exists. | Baseline shows `No execution target selected`, `No Project`, project controls and `New Chat` at once. | Use a stateful onboarding empty state with exactly one primary action: **Configure provider** when none exist, then **Create/choose target**, then **Start a chat**. |
| Global navigation, project filtering, chat history, theme, and execution status compete in the same sidebar. | `IvaiSidebar` is a dense 320dp drawer containing header, chat action, project chips, search, chat history, destinations, theme and status. | Split navigation by intent: primary navigation as a compact rail/bottom bar depending width; chat history and project filters in a dedicated Chat Drawer; persistent execution status belongs in Chat Context / target hub, not drawer footer. |
| Provider setup is a large single dialog with a high cognitive load. | `AddProviderDialog` mixes family, endpoint, model, capabilities, trust confirmation and API key in one scrolling dialog. | Replace with a **four-step guided sheet**: 1) family, 2) endpoint/trust, 3) account/credential, 4) model/capabilities + review. Cloud and Local use deliberately different safety copy. |
| Router creation cannot express/review precise fallback intent elegantly. | Current `CreateComboDialog` is a checkbox list; ordering is inferred from catalog position. | Add a staged Combo builder with ordered selection, explicit drag/reorder controls, capability compatibility summary and a non-network review page. |
| Agent surface is functionally safe but visually list-heavy. | `AgentsScreen` presents profiles, runs, trace and approvals as one long feed. | Establish an Agent Run workspace: profile selector, run status timeline, trace inspector and approval sheet. Keep profile configuration separate from active run monitoring. |
| Project page is currently information-only. | Existing summary identifies Projects as display-only while Chat and sidebar use projects heavily. | Make Project a first-class local workspace hub showing chats, Agent runs, file activity and quick actions—or deliberately simplify and remove duplicate project selectors. Decision to be confirmed by card sorting. |
| Inconsistent visual primitives and raw gradient use. | Chat uses direct `Brush.linearGradient`, many custom shapes/DP values, while the baseline is pale and fragmented. | Build tokenized primitives (surfaces, status chips, row/actions, cards, page headers) and reserve gradients for non-essential hero/brand layers. |

### Baseline screenshot interpretation

The current Chat baseline has a valid IVAI logo, recognizable brand colors and an accessible high-level layout. However, the initial state makes users parse target selection, project assignment, filters, chat creation, empty-state guidance and a composer simultaneously. The Phase 7 goal is **not to decorate this screen more**; it is to reduce its first-decision count and make the next safe action obvious.


## Phase map artifact

The visual execution map in `docs/phase7_uiux_map.png` was rendered and reviewed. It presents the intended sequential delivery order—Foundation, Core Chat/Navigation, Connections, Bounded Agent, and Validation—and displays Local-first, Backendless, BYOK, and explicit control as constraints over every sub-phase.


## Correction — launcher icon versus in-app identity

The supplied VA image is **launcher-icon artwork only**. It is not the general IVAI logo and must not be reused in the TopBar, navigation, empty states, badges, or general component styling. Phase 7 will establish an independent in-app IVAI wordmark and UI identity. The approved interface palette can remain visually harmonious with the launcher icon’s indigo, emerald/aqua, and violet atmosphere, but it is governed by independent semantic tokens and must not copy the icon’s mark or gradient treatment.
