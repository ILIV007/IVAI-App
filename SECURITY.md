# Security Policy

## Supported versions

Security fixes are applied to the latest commit on the `main` branch until a versioned release policy is introduced.

## Reporting a vulnerability

Do **not** open a public issue for a suspected vulnerability, exposed credential, data-loss path, or unsafe tool-execution path. Instead, contact the repository owner privately through the GitHub profile associated with this repository and include a concise reproduction, affected commit or release, expected impact, and any proposed mitigation.

Please do not include real API keys, private conversations, user data, or other secrets in a report. If a proof of concept requires a credential, use a revoked or clearly synthetic value.

The project aims to acknowledge private reports within seven calendar days. Public disclosure should wait until the repository owner and reporter agree on a remediation timeline.

## Security scope

The following areas are security-sensitive and require explicit review before merge:

| Area | Minimum review focus |
|---|---|
| Provider credentials | Android Keystore use, redaction, no plaintext persistence or logging |
| Network integrations | Official endpoints only, TLS, scoped request logging, cancellation and error handling |
| Local files and exports | App-private storage, canonical paths, schema validation, no secret export |
| Agent tools | Allowlist, argument validation, least privilege, preview and user approval for mutations |
| Dependencies and build | License compatibility, version review, no committed secrets and reproducible CI |

## Non-goals for Alpha

The Alpha does not include shell execution, Accessibility automation, Shizuku, unrestricted local storage, MCP process execution, or background autonomous actions. New capabilities in these areas require a dedicated threat model and decision record.
