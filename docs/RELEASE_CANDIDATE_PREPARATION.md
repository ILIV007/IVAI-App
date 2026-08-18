# Local Release Candidate Preparation

> **Status:** This procedure prepares a **local, unsigned Release Candidate evidence package**. It does not sign, tag, upload, publish, or approve a public Alpha or stable release.
>
> **Authority:** The [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md) remains the release decision record. A package prepared here can satisfy deterministic evidence rows for one exact commit; it cannot satisfy physical-device, participant, signing, or owner-approval gates.

## Purpose and Boundary

The Release Candidate helper creates a reproducible local package from a clean checkout. It runs the debug and minified-release build, unit tests, lint, a forced test rerun, repository-integrity checks, and Local-first/Backendless/BYOK/provider-neutral/RTL source scans. The package contains the **unsigned** release APK, not a downloadable release binary. It remains outside the repository under `/tmp` by default and must never be uploaded, attached to a GitHub Release, or presented as stable.

| Package property | Meaning |
|---|---|
| Candidate identity | The manifest binds version, full commit SHA, short SHA, and a deterministic candidate ID. |
| Build evidence | Debug APK, unsigned R8 release APK, mapping, reports, build log, test totals, and scan reports are retained locally. |
| Integrity | Every transferred artifact is SHA-256 covered; the verifier rejects a missing, tampered, misidentified, or non-clean package. |
| Signing state | `apksigner verify` must reject the release APK because it has no signature; this result is captured and checksummed as package evidence. |
| Release boundary | The manifest and README explicitly state that the package is unsigned, untagged, unpublished, and not stable-approved. |

## Prepare a Candidate

Run from a clean checkout of the intended protected-branch commit. The command creates only a local package; it does not modify the source checkout or publish any artifact.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/tmp/android-sdk-ivai
./scripts/prepare_release_candidate.sh
```

The default package directory is:

```text
/tmp/ivai-release-candidates/<version>-rc-<short-commit>/
```

The helper refuses a dirty worktree, repository-integrity failure, tracked local/signing material, failed deterministic gate, unexpected test total, non-zero test failure/error/skip count, missing R8 mapping, or invariant-scan match.

## Verify Before Internal Review

Run the verifier against the exact local directory before an internal release review:

```bash
./scripts/verify_release_candidate_package.sh \
  /tmp/ivai-release-candidates/<version>-rc-<short-commit>
```

The verifier requires the expected artifact inventory, full SHA-256 match, coherent manifest identity, 142 clean unit tests, clean source/repository evidence, an `apksigner`-recorded unsigned state, and explicit unsigned/non-public boundary. A successful verification confirms package integrity only; it is **not** an approval to distribute the APK.

## Gates That Remain Open

| Gate | Why this package cannot close it |
|---|---|
| Participant and heuristic evidence | Voluntary, de-identified card sort, tree test, safety comprehension, and independent heuristic results must be collected in reality. |
| Physical-device matrix | Compact/medium lifecycle, offline, rotation, font scale, Force-RTL, TalkBack, touch-target, and launcher observations require real devices. |
| HTTPS local endpoint evidence | Explicit HTTPS loopback/private-LAN cancellation, timeout, and offline outcomes require the user’s real device/network configuration. |
| Signed artifact | Only the owner-controlled signing workflow may produce and retain the signed candidate plus its checksum. |
| Public provenance | Annotated tag, reviewed release notes, owner approval, GitHub Release, and independent post-download hash verification occur only after every release gate passes. |

> A green local package, CI run, debug APK, or unsigned release APK must never be relabeled as **stable**, **public**, or **approved** while any row above remains open.

## References

- [Release Readiness Checklist](RELEASE_READINESS_CHECKLIST.md)
- [Alpha Release Gate](ALPHA_RELEASE.md)
- [Phase 7.5 Field Kit](PHASE7_5_FIELD_KIT.md)
- [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md)
