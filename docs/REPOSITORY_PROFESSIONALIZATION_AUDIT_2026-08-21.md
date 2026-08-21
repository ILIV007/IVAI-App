# Repository Professionalization Audit — 21 August 2026

> **Scope:** This audit improves repository presentation, documentation navigation, contributor hygiene, GitHub governance, and maintenance automation. It does not add a provider default, network request, credential, backend, telemetry, runtime feature, signed artifact, tag, upload, or public Alpha release.

## Confirmed Starting Gaps

The repository already had protected CI, security policy, issue forms, a pull-request template, release tooling, and substantial product documentation. However, the public entry point still described an obsolete toolchain, lacked a role-based documentation index, and linked to dated audits as current release guidance. Contributor instructions did not describe the API 37.1 provisioning contract or active deterministic guard set. Repository hygiene lacked `.editorconfig` and `.gitattributes`; the public GitHub metadata was generic, wiki was enabled without an intended documentation role, and the repository allowed merge methods that contradicted the established squash-focused workflow.

## Remediation Record

| Area | Change | Deterministic boundary |
|---|---|---|
| Public identity | GitHub description now identifies IVAI as a Local-first, backendless, BYOK Android AI Agent Harness; relevant Android/privacy topics are set. | No product behavior changed. |
| GitHub governance | Issues remain enabled; unused wiki is disabled; squash merge is the sole allowed merge method; merged head branches are deleted automatically. | Protected review/CI requirements remain authoritative. |
| Secret hygiene | GitHub secret scanning and push protection are enabled in addition to the existing gitleaks workflow. | No credential is introduced or scanned into this record. |
| README | Rewritten as the current product, build, release, documentation, and contribution entry point. Toolchain references align to API 37.1/build-tools 37.0.0 and active 160-test baseline. | No physical/release gate is claimed closed. |
| Documentation discovery | `docs/README.md` provides role-based navigation and distinguishes active policies from historical evidence. | Historical decision records are preserved. |
| Contribution workflow | Contributor guidance and PR template now reference the SDK contract, focused guards, baseline alignment, explicit scope boundaries, and release boundary. | A green PR remains insufficient for signing or publication. |
| Repository hygiene | `.editorconfig`, `.gitattributes`, and expanded `.gitignore` enforce portable text normalization and prevent local signing/environment/build artifacts from being tracked. | Ignore rules do not replace credential review or secret scanning. |
| Dependency maintenance | Dependabot is configured with a constrained weekly check for Gradle and GitHub Actions, each limited to two open pull requests. | Updates remain ordinary protected pull requests requiring review and CI. |

## Validation Expectations

The accompanying PR must pass repository text checks, documentation link checks, all existing deterministic scripts, debug/release-R8 build, 160 clean unit tests, lint, Release Candidate verification, and protected GitHub CI. The current `main` CI gates are Secret scan plus Android build/unit-test/lint.

## External Gates That Remain Open

This professionalization pass does not replace the Phase 7.5 protocol. Usability/heuristic evidence, compact and medium device matrix, Force-RTL, TalkBack, real HTTPS loopback/private-LAN cancellation and timeout, owner-controlled signing, annotated tag, reviewed release notes, independent checksum check, and owner approval remain **Pending** before public Alpha publication.[1] [2]

## References

[1]: ROADMAP.md "Roadmap"
[2]: RELEASE_READINESS_CHECKLIST.md "Release Readiness Checklist"
