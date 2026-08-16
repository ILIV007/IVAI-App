# GitHub Alpha Release Checklist

This document governs the first public GitHub Alpha release of IVAI. A debug APK, a green pull request, or a successful local build is **not** by itself a release authorization.

## Release decision

The current repository is **not yet approved for a public GitHub Alpha release**. The implementation is buildable and protected by CI, but the remaining hardening and evidence items below must be closed or explicitly accepted by the repository owner before creating a tag, GitHub Release, or downloadable APK.

| Gate | Required evidence | Current status |
|---|---|---|
| Protected CI | Secret scan and Android build/unit-test/lint green on the release commit. | Required at release time. |
| Agent safety | Target validation, one-time approval, cancellation, budgets, restart recovery, and trace tests. | Implemented; rerun on release candidate. |
| Provider-neutral UX | No implicit provider/target; empty states and registry-derived target selection. | Implemented; rerun UI checks on release candidate. |
| Safe read-only Agent scope | Workspace read/list/search tools have bounded path, size, preview, trace, and test coverage, or are explicitly excluded from the release scope. | Pending scope decision/implementation. |
| RTL and accessibility | Force-RTL, mixed BiDi, TalkBack/semantics, touch-target, contrast, and screenshot evidence. | Pending. |
| Device matrix | Fresh install, upgrade, restart, offline, and rotation checks on the agreed Android API/device matrix. | Pending. |
| Migration/recovery | Legacy upgrade, reopen, corrupted import, delete-all-data, and local reset evidence. | Partially automated; release evidence pending. |
| Performance/network | Streaming, cancellation, timeout, offline, 429/fallback, and no-duplicate-side-effect evidence. | Pending release evidence. |
| Signed artifact | Release-signed APK, reproducible build command, SHA-256, and retained signing provenance. | Pending. |
| Release notes | Version, commit, limitations, install guidance, privacy/security notice, and rollback guidance. | Pending. |

## Required release artifacts

A candidate release must contain only artifacts generated from the approved release commit.

| Artifact | Requirement |
|---|---|
| Signed APK | Build from the release commit with the owner-approved signing configuration. Do not publish `app-debug.apk` as the Alpha release binary. |
| SHA-256 file | Generate and publish a checksum for every downloadable binary. |
| Release notes | State the exact tag, commit, compatible Android range, validation summary, known limitations, privacy/security boundaries, and upgrade/rollback guidance. |
| Source tag | Create an annotated, immutable version tag after all gates are green. |
| Validation record | Preserve CI links and the local/device evidence used to approve the release. |

## Candidate build procedure

The following commands describe the required verification shape. They do not create a public release and must be run with the owner-approved signing configuration only.

```bash
./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain

# After release signing is configured and reviewed:
./gradlew assembleRelease --no-daemon --console=plain
sha256sum app/build/outputs/apk/release/*.apk > SHA256SUMS.txt
```

The signing key, keystore password, alias, and related secret material must never be committed, printed in logs, copied into release notes, or placed in a GitHub issue/PR. Store them only through the repository owner's approved secure release workflow.

## Required release-note content

Release notes must include the following sections:

1. **What this Alpha is:** a local-first, backendless, BYOK Android Agent Harness.
2. **What users control:** providers, accounts, endpoints, models, Combos, execution targets, Agent profiles, and local project workspace.
3. **Security boundaries:** secrets remain device-local; writes require Allow once; no automatic write replay after restart; no Shell, Termux, Accessibility automation, MCP server, or backend proxy.
4. **Known limitations:** all unfinished items from the gate table and any accepted product limitations.
5. **Installation and verification:** Android compatibility, APK checksum verification, and how to report a non-sensitive issue.
6. **Upgrade and rollback:** local-data warning, backup/export guidance, and the prior release/tag used for rollback.

## GitHub Release creation gate

Create a GitHub Release only after all release gates are green and the owner approves the final signed artifact. The release must use the approved source tag, attach the signed APK and `SHA256SUMS.txt`, and use reviewed release notes. Until then, keep the repository in the pre-release preparation state and do not publish a binary.
