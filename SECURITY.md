# Security Policy

## Supported pre-release line

Security fixes are applied to the latest commit on the protected `main` branch until a versioned release policy is introduced. IVAI is currently in **pre-release Alpha preparation**: a public Alpha binary has not been approved or published. A green build or debug APK is not a release authorization; see [the Alpha release checklist](docs/ALPHA_RELEASE.md) for the remaining evidence and owner-approval gates.

## Reporting a vulnerability

Do **not** open a public issue for a suspected vulnerability, exposed credential, data-loss path, unsafe tool-execution path, or release-integrity problem. Report it privately through the repository owner's GitHub profile or an agreed private channel. Include a concise, redacted reproduction; the affected commit or release; the expected impact; and any proposed mitigation.

> **Never include an API key, credential, authorization header, secret-vault content, private conversation, workspace file, prompt, run trace, device identifier, screen recording, or raw network log in a report, issue, pull request, screenshot, or commit.** If a proof of concept requires a credential, use a revoked or clearly synthetic value and revoke any inadvertently exposed credential immediately.

The project aims to acknowledge private reports within seven calendar days. Public disclosure should wait until the repository owner and reporter agree on a remediation timeline.

## Security scope

Reports are particularly valuable when they concern a violation of IVAI's Local-first, Backendless, or BYOK boundaries.

| Area | Minimum review focus |
|---|---|
| Provider credentials | Android Keystore use, opaque credential references, redaction, and no plaintext persistence, logging, export, or import. |
| Provider and endpoint policy | HTTPS-only validation, explicit local trust mode and confirmation, no endpoint discovery/scanning, no trust bypass, and no implicit provider/model/Combo selection. |
| Local files and exports | App-private workspace confinement, canonical paths, schema validation, no secret export, no unintended backup/device transfer, and safe local-data deletion. |
| Agent tools | Explicit allowlist, argument validation, bounded execution, least privilege, preview and **Allow once** approval for a mutation, restart-safe recovery, cancellation, and trace safety. |
| UI safety and accessibility | Explicit execution target, approval, recovery, and destructive-local-data actions; inaccessible task-critical controls; or misleading safety copy. |
| Dependencies, CI, and releases | License compatibility, version review, no committed secrets, reproducible CI, protected-branch integrity, signed-artifact provenance, and checksum verification. |

## Intentional Alpha boundaries

The following are deliberate Alpha boundaries, not security defects by themselves: no central IVAI backend, no mandatory account, no analytics pipeline, no Shell/Termux/Shizuku execution, no Accessibility automation, no MCP process/server execution, no autonomous background agents, no unrestricted storage access, no local-model inference, no cleartext HTTP, and no `.local`/mDNS discovery or network scanning. Any future capability in these areas requires a dedicated threat model and decision record.

## Disclosure and remediation

The repository owner assesses credible reports, determines whether a focused remediation is required, and validates a fix through the protected pull-request workflow. IVAI makes no bounty commitment. A remediation does not by itself approve a release: a public Alpha still requires all release gates, including real-device evidence and owner-controlled signing.
