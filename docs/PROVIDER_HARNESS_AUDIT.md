# Provider Harness and UI/UX Audit

> **Status:** Review completed against `main` at `16a36f1`. This document is a planning and safety record; it does not authorize automatic network access, a default provider, a public Alpha release, or local cleartext endpoint access.

## Executive finding

IVAI remains aligned with its central product principle: it is a **local-first, backendless, BYOK Agent Harness**. Connections, accounts, models, Combos, workspace records, and Agent traces are local, while credentials are referenced from Room and stored through the Keystore-backed vault. The Provider Registry, Router, Combo model, and explicit execution targets are the correct foundation.

The current implementation is, however, still an **early Provider Management surface** rather than the complete harness described in the roadmap. It supports `GEMINI`, `OPENROUTER`, and `CUSTOM_OPENAI_COMPATIBLE`, but the connection form is a one-shot setup flow with a Gemini preselection, one required API key, one manual model, and hard-coded `TEXT,STREAMING` capabilities. It lacks a non-default preset catalog, robust model/capability management, endpoint trust modes, a local-server path, and a guided UX that separates cloud API configuration from a user-operated local inference server.

## Roadmap alignment

| Roadmap requirement | Current evidence | Assessment | Required action |
|---|---|---|---|
| User controls Provider, Account, Endpoint, Model and Combo | Room-backed records, Registry, Router and Combo validation exist. | **Aligned foundation** | Preserve this ownership model; presets may only prefill non-secret metadata. |
| No default provider or single-provider product identity | Sidebar is provider-neutral, but the add-provider dialog initially selects Gemini. | **UI deviation** | Start from an unselected connection mode or preset choice; never create or privilege a provider automatically. |
| BYOK with secret isolation | Secret Vault uses a credential reference; raw secret is accepted only at the save boundary. | **Aligned** | Keep API keys optional only for trusted local-server modes; never persist a placeholder or plaintext key. |
| Famous providers should be easy to configure | Only Gemini, OpenRouter, and generic custom OpenAI-compatible choices are visible; user must enter endpoint and model manually. | **Gap** | Add a local preset catalog whose entries map to installed adapters and prefill only URL/protocol/capability hints. |
| User may use local models | Current policy rejects `localhost`, loopback, and all HTTP endpoints; the OpenAI-compatible gate requires HTTPS and a Bearer token. | **Explicitly deferred, not implemented** | Add a separate local-server trust policy, no-secret credential mode, Android cleartext allowlist, warning UX, test coverage, and release evidence. |
| Provider-neutral Agent and Chat target | Runtime validates persisted Direct Model/Combo targets; no hidden fallback is injected. | **Aligned** | Keep preset metadata out of target selection semantics; the user still picks model or Combo. |
| Accurate model capabilities | New provider connection writes one manual model with fixed `TEXT,STREAMING`. | **Gap** | Make capabilities editable/discovered and fail closed for Agent tool calls when a model lacks required capability. |

## Confirmed implementation constraints

The present `OpenAiCompatibleNetworkGate` sends only the OpenAI-style streaming Chat Completions protocol to `baseUrl + /chat/completions`, always attaches a Bearer credential, and currently requires HTTPS. That is sufficient for a controlled subset of compatible cloud APIs, but not for an unauthenticated local runtime such as Ollama or an HTTP loopback server.

The existing `ProviderEndpointPolicy` correctly blocks risky endpoint shapes, including userinfo, fragments, and loopback addresses, rather than silently allowing a local bypass. This must remain the default. Local endpoint support must be a distinct, explicit opt-in mode—not a relaxation of the remote policy.

## Recommended provider catalog architecture

The next implementation should not create a new adapter kind for every cloud vendor. Instead, it should introduce a **provider preset catalog** that maps a user-selected preset to an existing execution protocol, an endpoint template, credential requirement, documentation URL, and initial capability hint. A preset is local metadata only: it must never create a connection automatically, contact a network endpoint, ship a credential, select a model, or make a provider the default.

| Catalog group | First preset set | Execution protocol | Endpoint behavior | Credential behavior |
|---|---|---|---|---|
| Managed direct | Gemini, OpenRouter | Existing installed adapters | Fixed by adapter | User supplies one API key per account. |
| Cloud OpenAI-compatible | OpenAI, Groq, Mistral, Together, DeepSeek, Fireworks | Existing custom OpenAI-compatible Chat Completions adapter | HTTPS endpoint prefilled but visible and reviewable | User supplies one API key per account. |
| Local OpenAI-compatible | Ollama, LM Studio, vLLM/custom local server | Same protocol only after a dedicated local trust mode is complete | Explicit endpoint supplied or confirmed by user; never auto-probed on dialog open | No key or a user-provided optional key, depending on server policy. |

The proposed cloud endpoint templates are based on official provider documentation: Groq `https://api.groq.com/openai/v1`, Mistral `https://api.mistral.ai/v1`, Together `https://api.together.ai/v1`, DeepSeek `https://api.deepseek.com`, and Fireworks `https://api.fireworks.ai/inference/v1`. These are **configuration hints**, not guarantees that every provider/model supports every IVAI capability. The existing protocol must retain error normalization and model capability checks. [1] [2] [3] [4] [5]

## Local-model trust boundary

LM Studio and Ollama both expose OpenAI-compatible chat endpoints on a server controlled by the user; the official examples use `http://localhost:1234/v1` and `http://localhost:11434/v1/` respectively. [6] [7] Android loopback refers to the Android device/emulator itself, not automatically to the user’s desktop. A user-operated LAN server is therefore a materially different trust boundary and must not be silently treated as local.

Before enabling any local-server connection, IVAI must implement all of the following:

1. Introduce a persisted, reviewable endpoint trust mode distinguishing remote HTTPS, device loopback, and user-approved private-LAN endpoints. Public HTTP and arbitrary cleartext hosts remain blocked.
2. Configure Android cleartext traffic narrowly and only for the explicit local-server mode; the global default remains HTTPS-only.
3. Require a confirmation screen that presents the exact host, port, transport, local/LAN classification, absence or presence of authentication, and the warning that prompts are sent to that server.
4. Permit a credential-less account only for an explicitly selected local mode. Do not save dummy tokens such as `ollama`; the account must record that no secret is stored.
5. Make model discovery a user-triggered foreground action with cancellation, timeout, sanitized diagnostics, and no automatic polling.
6. Keep local-server network operations inside the same foreground network gate and Run/Attempt Trace policy. No hidden LAN discovery, background scanning, or daemon management is allowed.
7. Add unit and device tests for endpoint classification, cleartext policy, prompt/credential redaction, cancellation, error normalization, and rejection of public HTTP or unapproved LAN endpoints.

## UI/UX review and target flow

The current dialog asks for provider kind, name, one endpoint, one account label, one model ID, and one API key simultaneously. This is technically functional but creates cognitive load and fails to explain protocol or security differences.

The next UI phase should be a deterministic in-app flow, not an image-only mockup:

1. **Choose connection family:** Managed provider, Cloud OpenAI-compatible preset, Local model server, or Advanced custom endpoint. No choice is preselected.
2. **Choose preset or advanced mode:** Presets show name, protocol, endpoint host, credential expectation, and documentation link. The endpoint remains reviewable; advanced custom stays available.
3. **Configure connection:** Set a local display name and inspect the endpoint. For local mode, confirm the explicit network-trust warning before proceeding.
4. **Configure account:** Use an API-key field only when that preset requires one. For local credential-less mode, show an explicit “no credential will be stored” state.
5. **Add or discover models:** Present manual entry and a separately invoked model discovery action. Capabilities are explicit and editable; tool-capable Agent use is gated by actual declared capability.
6. **Review and save:** Summarize what will stay local, the endpoint destination, credential state, and the fact that no connection is tested until the user explicitly requests it.

The list screen should show compact provider cards with a protocol badge, endpoint trust badge, credential status, enabled model count, and primary action. A disabled or incomplete connection should never be selectable as a Chat/Combo/Agent target.

## Scope guard for the immediate next phase

The immediate implementation phase is limited to the **provider preset contract and the foundation for a guided UI**. It must not add a provider credential, automatic network request, model download, local inference engine, Shell/Termux integration, broad storage access, background discovery, or a blanket cleartext permission. Local endpoint transport support is a separate security-sensitive subphase after its threat model, persistence migration, and Android network-security behavior are approved and tested.

## Sources

[1]: https://console.groq.com/docs/openai "Groq OpenAI Compatibility"
[2]: https://docs.mistral.ai/resources/migration-guides "Mistral migration guide"
[3]: https://docs.together.ai/docs/inference/openai-compatibility "Together OpenAI compatibility"
[4]: https://api-docs.deepseek.com/ "DeepSeek API Docs"
[5]: https://docs.fireworks.ai/tools-sdks/openai-compatibility "Fireworks OpenAI compatibility"
[6]: https://lmstudio.ai/docs/developer/openai-compat "LM Studio OpenAI Compatibility Endpoints"
[7]: https://docs.ollama.com/openai "Ollama OpenAI compatibility"
[8]: https://docs.vllm.ai/en/stable/serving/online_serving/ "vLLM OpenAI-Compatible Server"
