# P3-01 — One-provider Chat Vertical Slice

## Metadata

| Field | Value |
|---|---|
| Owner | IVAI implementation agent |
| Prerequisite branches | `feat/ui-rtl-foundation` and `feat/local-data-security` must merge to `main` |
| Target phase | Phase 3 — One-provider Chat Vertical Slice |
| Proposed first provider | Google Gemini, as specified by the Phase 3 roadmap |

## Goal

Prove one real, user-owned-provider chat request end-to-end: a secret read from the local vault is used only in the central Network Gate, normalized streaming events update one persisted conversation, cancellation works, and errors are displayed without exposing a secret, Authorization header, raw provider payload, or internal reasoning.

## Preconditions

This task does not begin a real network request until both prerequisite Pull Requests are merged and a user deliberately supplies a provider credential through an approved secure entry flow. The credential must be written through `EncryptedSecretVault`; it must never be copied into source, test fixtures, logs, BuildConfig, Room, screenshots, diagnostics, commits, or Pull Request text.

## Provider-neutral contract

```text
ChatProvider
  validateConnection(request) -> ProviderValidation
  listModels(credentials) -> List<ModelDescriptor>
  streamChat(request) -> Flow<ProviderStreamEvent>

ProviderStreamEvent
  Started(attemptId)
  Delta(text)
  Usage(inputTokens?, outputTokens?)
  Completed(messageId)
  Failed(normalizedError)
  Cancelled
```

The request has an explicit model ID, conversation messages, capability requirements, and cancellation handle. Error variants are provider-neutral: authentication, rate limit, timeout, network unavailable, invalid request, unsupported capability, and unknown. Network diagnostics retain only provider ID, HTTP class/status, latency, opaque attempt ID, and redacted error category.

## Scope

| Included | Excluded |
|---|---|
| Single Gemini adapter, manual model ID, model discovery where officially supported, streaming, non-streaming fallback, cancellation, bounded retry, error normalization, usage parsing, persisted chat integration, and RTL typing/copy tests. | OpenRouter, custom OpenAI-compatible endpoint, Router, fallback across providers, account selection, tool execution, file upload, cloud sync, backend proxy, telemetry, export, or Agent runtime. |

## Security requirements

The Network Gate is the only call site permitted to receive decrypted credential material. It constructs the Authorization request in-memory, redacts sensitive headers before diagnostics, sets provider-specific timeouts, and guarantees cancellation closes the underlying request. The adapter accepts normalized inputs and returns normalized events; it does not read DataStore or Keystore directly.

## Acceptance criteria

| Area | Required outcome |
|---|---|
| Vertical slice | A real chat request uses a vault-held secret, streams visible deltas, persists the final conversation, and restores it after restart. |
| Control | User cancellation stops the underlying call and resolves the UI into a safe terminal state. |
| Errors | Auth, network, rate-limit, timeout, and malformed-response paths normalize without leaking secrets or raw Authorization data. |
| Contract | Contract tests cover deltas, completion, cancellation, usage, capabilities, and normalized errors without live credentials. |
| UI/RTL | Persian/Arabic typing, cursor, selection, copy/paste, Markdown, and mixed LTR technical tokens remain correct during stream updates. |
| Validation | Local Build/Test/Lint and protected-branch CI pass; a manual device test with a user-provided disposable credential is documented separately and never committed. |

## Implementation order

1. Rebase a new provider branch onto merged `main` and verify existing gates.
2. Add a minimal HTTP client and Network Gate with redacted diagnostics.
3. Define the provider-neutral contract and fake-provider tests before the Gemini adapter.
4. Wire manual model selection and vault lookup through the UI state boundary.
5. Add streaming, cancellation, persistence integration, then device-only manual validation.
6. Open a focused Pull Request; do not bundle Router or Agent work.

## References

[1]: https://ai.google.dev/gemini-api/docs "Google AI for Developers — Gemini API documentation"
[2]: https://developer.android.com/topic/performance/threads "Android Developers — threading guidance"
[3]: https://developer.android.com/jetpack/androidx/releases/room "Android Developers — Room persistence guidance"
