# Local Endpoint Trust Mode — Threat Model and Acceptance Criteria

> **Status:** Design gate for a future user-managed local-model connection. This document does not enable a local endpoint, cleartext traffic, model discovery, or any background behavior by itself.

## Decision

IVAI may support **a server the user operates**—for example an Ollama, LM Studio, vLLM or compatible OpenAI-style server—but only as a separately classified connection. It must not weaken the existing remote-HTTPS policy, create a default provider, send a dummy API token, probe a network automatically, scan a LAN, download or run a local inference engine, or expose Shell/Termux/Accessibility/MCP functionality.

The first shippable scope is deliberately narrower than “any LAN HTTP server” and is **HTTPS-only**:

| Connection class | Transport | Initial status | Rationale |
|---|---|---|---|
| Remote cloud | HTTPS | Existing behavior | Uses the current remote endpoint policy and an API-key account. |
| Device loopback | HTTPS | Implemented with explicit local trust mode | The endpoint is on the Android device itself; exact-host validation and user confirmation remain required. |
| Private-LAN literal RFC1918 IPv4 | HTTPS | Implemented with explicit local trust mode | The address class is deterministic and no dynamic discovery or cleartext exception is required. |
| User-named `.local` endpoint | Any | Not in the initial scope | Requires DNS/mDNS behavior and a separate acceptance/security review. |
| Private-LAN literal IP over HTTP | Not in the initial scope | Android network-security XML cannot safely express a dynamic user-entered IP range as a narrow static cleartext allowlist. |
| Arbitrary public HTTP / arbitrary host / public IP | Permanently rejected | Prevents an explicit local mode from becoming a broad cleartext bypass. |

Android 9 and newer disable cleartext by default; the app’s current `minSdk=29` and `targetSdk=36` therefore start from an HTTPS-only posture. Android Network Security Configuration supports a manifest-referenced, static configuration with per-domain cleartext exceptions, while a global cleartext opt-in should be avoided. [1] The platform documentation also notes a localhost-specific implicit configuration only from Android 17/API 37; IVAI targets API 36 and must not depend on that future behavior. [1]

## Assets and threat boundaries

| Asset / property | Must be preserved | Threats to prevent |
|---|---|---|
| User prompts and streamed outputs | Stay inside the chosen destination; never appear in diagnostics or traces by default. | Silent exfiltration to arbitrary HTTP endpoints, DNS-based endpoint substitution, content in logs. |
| Credential state | A local no-auth account records **no secret stored**; cloud accounts retain an opaque vault reference. | Dummy token persisted/sent, vault lookup failure treated as a usable API key, plaintext state. |
| Existing cloud connections | Existing remote HTTPS connections continue unchanged after migration. | Trust-mode downgrade, migration corruption, unwanted default selection. |
| Network policy | Cleartext is permitted only for compile-time allowlisted local host categories and code-level classified modes. | Global `usesCleartextTraffic=true`, arbitrary LAN/public HTTP, network scan. |
| Agent/Router targets | Local models participate only through enabled user-owned connection/account/model records. | Bypass of target validation or profile validation. |
| Trace and diagnostics | Only endpoint class/host-safe metadata, normalized error and timing may be recorded. | Prompt, response, Authorization, model reasoning or raw endpoint path leaks. |

## Proposed persisted contract

### Connection trust mode

A `ProviderConnection` gains a non-secret `endpoint_trust_mode` field with an exhaustive enum:

| Value | Permitted URL shape | Account auth modes | Notes |
|---|---|---|---|
| `REMOTE_HTTPS` | `https://` with non-loopback host | `API_KEY` | Default for all migrated and new cloud connections. |
| `LOCAL_LOOPBACK_HTTPS` | `https://localhost`, `127.0.0.1`, `[::1]` only | `NONE` or `API_KEY` | Requires explicit user confirmation. The Android device, not the developer desktop, is loopback. |
| `LOCAL_LAN_HTTPS` | `https://` literal RFC1918 IPv4 host only | `NONE` or `API_KEY` | Requires explicit user confirmation; physical-device TLS/certificate evidence remains a release gate. |

A connection cannot be persisted with a URL that is inconsistent with its trust mode. Mode changes are destructive in effect: the user reviews and confirms the exact destination again; no existing remote connection is automatically reclassified.

### Account authentication mode

A `ProviderAccount` gains an `auth_mode` with `API_KEY` or `NONE`. To preserve existing Router foreign-key and unique-index shapes without any plaintext token, a no-auth account stores the canonical non-secret marker `no-auth.<account-id>` in its opaque reference column:

| Auth mode | Credential reference column | Vault operation | Transport behavior |
|---|---|---|---|
| `API_KEY` | Required opaque reference | Store/read/clear through `EncryptedSecretVault` | Send a Bearer header only after a user-initiated foreground request. |
| `NONE` | Canonical non-secret `no-auth.<account-id>` marker | No vault record is created, read or cleared. | Do not send an Authorization header. |

The database migration preserves every existing v4 account as `API_KEY` with its existing reference. It does not generate placeholders, dummy API keys, or a fake `ollama` credential.

## UI acceptance criteria

1. The first choice is explicit: Cloud HTTPS, Local device HTTPS endpoint, Private-LAN HTTPS endpoint, or Advanced custom. No provider, endpoint, model or auth mode is preselected.
2. A local selection must present the exact scheme, host, port, trust class, whether a key is stored, and a clear statement that messages go directly to that user-operated server.
3. The primary confirmation label must be explicit, such as **“I trust this local endpoint”**. Dismissal, process death, validation failure or edit after confirmation must not create/enable the connection.
4. No test request happens while typing or saving. A separate user-initiated foreground “Test connection” or “Discover models” action is required in a later scope.
5. The account UI must visibly differentiate **Credential stored**, **Credential missing**, and **No credential required**.
6. Model ID and declared capabilities stay user-selected; no model is inferred from the local server without an explicit discovery action.

## Runtime acceptance criteria

1. `OpenAiCompatibleNetworkGate` validates the connection trust mode before constructing the URL.
2. The `Authorization` header is absent for `NONE` auth and present only for a nonblank key resolved immediately before a foreground request.
3. The protocol remains OpenAI-style streaming Chat Completions; no shell, subprocess, mDNS scan, background polling, HTTP POST tool, or local inference runtime is introduced.
4. No `.local` hostname, DNS/mDNS discovery, raw resolved IP persistence or endpoint scan exists in this scope.
5. All non-2xx, cancellation and I/O failures are normalized without emitting prompt, response or credential data.

## Migration and regression acceptance criteria

1. Room migrates the real legacy v1 fixture through v5, reopens successfully, and creates no local trust or no-auth account implicitly.
2. Existing cloud account references retain `API_KEY` and their Router/Agent targets remain valid after the default-preserving migration.
3. `NONE` authentication persists its canonical non-secret marker; no Vault read/write/clear is attempted.
4. Endpoint policy tests reject public HTTP, userinfo, fragments, `.local` forms, private-LAN HTTP, public hosts in local modes, and local/private hosts in remote mode.
5. Tests accept only exact HTTPS loopback endpoints and HTTPS RFC1918 IPv4 endpoints for the initial mode.
6. The manifest contains no cleartext opt-in or Network Security Configuration exception; the HTTPS-only endpoint policy is tested directly.

## Explicitly excluded

The following remain out of scope for this trust-mode phase: dynamic LAN discovery, arbitrary local-IP HTTP, UPnP, port scanning, a bundled model runtime, model downloading, self-signed certificate bypass, user-installed CA trust changes, network service/background workers, Shell/Termux, Shizuku, Accessibility automation, MCP servers/processes, and backend relay/proxy services.

## References

[1]: https://developer.android.com/privacy-and-security/security-config "Android Developers — Network security configuration"
[2]: https://mas.owasp.org/MASTG/knowledge/android/MASVS-NETWORK/MASTG-KNOW-0014/ "OWASP MASTG — Android Network Security Configuration"
[3]: https://mas.owasp.org/MASTG/tests/android/MASVS-NETWORK/MASTG-TEST-0235/ "OWASP MASTG — Testing cleartext traffic"
[4]: https://docs.ollama.com/openai "Ollama — OpenAI compatibility"
[5]: https://lmstudio.ai/docs/developer/openai-compat "LM Studio — OpenAI compatibility endpoints"
[6]: https://docs.vllm.ai/en/stable/serving/online_serving/ "vLLM — OpenAI-compatible server"
