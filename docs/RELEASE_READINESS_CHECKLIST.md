# IVAI Release Readiness Checklist

> **Purpose:** This checklist governs the decision to declare an IVAI build stable enough for a public GitHub Alpha release. It is intentionally stricter than a successful debug build: every release blocker must have traceable evidence from the candidate commit.
>
> **Current decision:** **Not approved for public download.** This checklist begins with no completion claim. A controlled debug build may be used only for the de-identified Phase 7.5 research protocol; it is not an Alpha binary and must not be committed or published as a release.
>
> **Non-negotiable architecture:** IVAI remains **Local-first, Backendless, BYOK, and Provider-neutral**. Any gate failure that weakens those boundaries blocks release and requires a focused remediation/retest increment.

## 1. How to Use This Checklist

The release owner records the candidate commit and applies every row in order. A row is **Pass** only when the named evidence exists for that exact candidate; an older successful run is useful context but is not final release evidence. A **Blocked** row cannot be waived by a green JVM test, a screenshot alone, or a debug APK. A **Deferred** row is acceptable only when it is explicitly outside the Alpha scope and does not weaken a P0/P1 safety, accessibility, privacy, or data-integrity requirement.

| State | Meaning | Release effect |
|---|---|---|
| Pass | Evidence exists for the candidate and meets the pass condition. | The row may be closed. |
| Pending | Required evidence has not yet been collected. | Blocks public Alpha if the row is a release gate. |
| Blocked | A failure, unsafe ambiguity, missing owner approval, or failed validation exists. | Blocks release until remediated and retested. |
| Deferred | Intentionally outside Alpha with a documented reason and no release-gate impact. | Does not close a P0/P1 release gate. |
| Not applicable | The release owner documents why the row cannot apply to the candidate. | Requires reviewer confirmation. |

## 2. Candidate Identity and Governance

Record these fields before running any quality command. The candidate must be generated from a clean checkout of a protected-branch commit, and the source commit must remain recoverable through an annotated tag only after all gates are green.

| Check | Evidence to retain | Pass condition | Initial state |
|---|---|---|---|
| Candidate commit | Full SHA and `git status --short` output | Clean worktree; source is `main` or an approved release branch. | Pending |
| Toolchain provenance | JDK version, Android SDK/platform/build-tools, Gradle and AGP versions | Matches repository instructions and protected CI: JDK 21, Android platform 36.1, Gradle/AGP versions pinned in source. | Pending |
| Protected workflow | PR URL, approvals, CI links, protection snapshot | Required Secret scan and Android quality jobs pass; linear history and branch protection remain enabled. | Pending |
| Scope review | PR description and changed-file list | No unreviewed provider, Agent, data, permission, network, migration, dependency, or release-signing scope expansion. | Pending |
| Repository integrity | `git fsck --full --no-reflogs`, tracked-file review | No corruption; no tracked key, certificate, `local.properties`, or environment-secret file. | Pending |
| Release owner approval | Explicit recorded owner decision | Owner approves the exact signed artifact only after every blocker below passes. | Pending |

## 3. Build, Test, Lint, and Reproducibility

Run all commands from the candidate checkout with the documented JDK and Android SDK. The full quality gate must pass before inspecting generated artifacts. A forced test rerun prevents cache-only evidence from being mistaken for execution evidence.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/path/to/android-sdk

./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
./gradlew testDebugUnitTest --rerun-tasks --no-daemon --console=plain
git diff --check
git status --short
```

| Check | Evidence to retain | Pass condition | Initial state |
|---|---|---|---|
| Clean debug quality gate | Console log and generated reports | `BUILD SUCCESSFUL`; debug APK, unit-test report, and lint report exist. | Pending |
| Independent unit execution | Forced-rerun log and report totals | Zero failures, errors, and skipped tests. Current baseline is 136 tests and must not regress without a documented review. | Pending |
| Android lint | `lintDebug` XML/HTML report | Zero Error and Fatal findings; every Warning has an owner, disposition, and no hidden safety impact. | Pending |
| Whitespace and source state | `git diff --check`, `git status --short` | No whitespace error; clean candidate worktree. | Pending |
| Screenshot and semantics regression | Roborazzi/JVM reports and test output | Existing representative UI regression tests pass; evidence is not substituted for device accessibility testing. | Pending |
| Reproducible signed build | Owner-controlled release command and retained provenance | A release-signed APK is built from the approved commit without committing or printing signing secrets. | Pending |

## 4. Local-first, Backendless, BYOK, and Provider-Neutral Invariants

The following source scans are minimum deterministic checks. A matching line must be reviewed rather than ignored: descriptive copy and Android XML namespaces can be legitimate, but executable behavior that weakens a boundary is a release blocker.

```bash
# Secrets and literal credential assignments
# Cleartext/trust bypasses
# Implicit provider selection
# Hosted analytics/telemetry SDK imports
# Background/prohibited execution
# Global forced-LTR shell override
```

| Check | Pass condition | Release severity | Initial state |
|---|---|---|---|
| No hardcoded secrets | No real API key, credential, bearer token, or literal credential assignment appears in tracked source or release artifact review. | P0 | Pending |
| Keystore and Room boundary | Credentials remain Keystore-protected; Room holds opaque credential references only; export/import remains secret-free. | P0 | Pending |
| No central backend or telemetry | No IVAI server, proxy, mandatory login, analytics SDK, session replay, or remote error-log integration is introduced. | P0 | Pending |
| No implicit target/provider | No default provider/model/Combo/Agent target is selected or changed automatically. | P0 | Pending |
| HTTPS and explicit trust policy | No cleartext opt-in, trust-all implementation, hostname-verifier bypass, HTTP endpoint, `.local` discovery, mDNS, or LAN scanning exists. | P0 | Pending |
| Local endpoint constraints | Loopback/private-LAN use is HTTPS-only, explicitly classified, and locally confirmed; cancellation and timeout are later verified on device. | P0 | Pending |
| No prohibited execution | No Shell/Termux/Shizuku, process execution, Accessibility automation, MCP process/server execution, autonomous background Agent, or unrestricted storage capability is introduced. | P0 | Pending |
| Workspace and data isolation | Project operations stay app-private and project-bounded; traversal, unsafe export, backup, and device-transfer paths are rejected. | P0 | Pending |
| No global forced LTR | Application shell/theme preserves natural layout direction; allowed LTR is bounded to code/terminal/footer contexts only. | P1 | Pending |

## 5. Provider, Router, and Agent Safety

This section evaluates task-critical behavior that must remain explicit even when the UI is visually stable. Each row requires regression evidence and, where stated, later field evidence.

| Check | Pass condition | Release severity | Initial state |
|---|---|---|---|
| Provider setup | User explicitly chooses provider family, endpoint trust, account/auth mode, model/capabilities, and final save. | P0 | Pending |
| Router and Combo | Direct target or ordered Combo is user-selected; fallback is sequential, capability-aware, locally traced, and has no duplicate side effect. | P0 | Pending |
| Target failure recovery | UI explains recovery and never auto-changes provider, target, permission, or local file. A non-empty streamed assistant partial that ends before completion is retained locally with a visible incomplete marker; no fallback follows visible content. | P0 | Pending |
| Agent profile | Agent target is registry-derived and valid/enabled; profile creation waits for explicit final review. | P0 | Pending |
| Agent limits and cancellation | Step, tool-call, and runtime ceilings; trace; terminal state; and cancellation all pass regression coverage. | P0 | Pending |
| Workspace reads | Read/list/search tools remain app-private, project-bound, bounded, and do not persist sensitive observations in trace. | P0 | Pending |
| File mutation | Write has a bounded path/preview and an explicit **Allow once** or deny decision; no always-allow and no replay after restart. | P0 | Pending |
| Process-death recovery | Unresolved approval denies safely and interrupted work stops without a write. | P0 | Pending |

## 6. UI, Accessibility, and Phase 7.5 Research

Automated Compose semantics and Roborazzi screenshots establish deterministic coverage but do not close this section. All physical-device and participant findings are de-identified, retain no credential/prompt/file/media data, and remain outside the product runtime. The protocol, field kit, and record are authoritative for collection rules.[1]

| Check | Evidence to retain | Pass condition | Initial state |
|---|---|---|---|
| Visual-system boundary | Screenshot/semantic review | VA artwork remains launcher-only; product UI uses the independent IVAI wordmark and indigo/emerald/violet system. | Pending |
| Contrast and touch targets | Semantic/visual review plus device checks | Text/control contrast and task-critical touch targets are usable in dark/light modes. | Pending |
| BiDi/RTL | Mixed English/Persian/Arabic review and Force-RTL device evidence | Natural flow remains readable; only bounded code/terminal/footer LTR exceptions exist. | Pending |
| TalkBack | Compact and medium device notes/screenshots without sensitive content | Linear swipe and touch exploration reach each critical control once with concise spoken meaning and relevant status. | Pending |
| Card sort | De-identified study aggregation | At least 70% agreement for each primary navigation category, or label/placement changes are retested. | Pending |
| Tree test | De-identified task aggregation | At least 80% direct success for each critical task, or the hierarchy is revised and retested. | Pending |
| Safety comprehension | Moderated de-identified outcomes | Every participant identifies active target, explains one-time approval, and recovers a failed target without moderator rescue. | Pending |
| Heuristic review | Independent reviewer findings and reconciliation | No unresolved P0/P1 usability or accessibility finding. | Pending |

## 7. Physical-Device and Network Matrix

A JVM, emulator, screenshot test, or CI result cannot be substituted for this matrix. Use at least one compact and one medium physical Android device. Do not record device identifiers, real credentials, personal files, prompts, screen recordings, or raw network logs. Android recommends combining automated checks with manual accessibility-service testing.[2]

| Matrix dimension | Required evidence | Pass condition | Initial state |
|---|---|---|---|
| Fresh install and local reset | Compact + medium device records | No crash, silent migration/data loss, unexpected execution, or unsafe residual local state. | Pending |
| Upgrade and restart | Upgrade from prior debug/candidate, then restart | No corruption, unexpected replay, migration failure, or loss of protected local data. | Pending |
| Rotation, layout, and theme | Portrait/landscape; dark/light; default/large font scale | No task-critical clipping, overlap, or unreachable control. | Pending |
| Offline behavior | Controlled offline task paths | Safe error/recovery; no hidden retry, duplicated side effect, or provider/target change. | Pending |
| Force-RTL and mixed text | System Force-RTL with English/Persian/Arabic content | No task-critical direction or readability failure. | Pending |
| TalkBack | Swipe order and explore-by-touch | Every task-critical control is reachable and understandable. | Pending |
| HTTPS loopback/private-LAN | Explicit user-configured HTTPS only; cancellation, timeout, offline | Safe recovery; no HTTP, discovery, scanning, trust bypass, or duplicate side effect. | Pending |
| Themed launcher icon | Supported OEM launcher/device observation | Adaptive monochrome launcher behavior is acceptable; launcher-only artwork does not affect product UI. | Pending |

## 8. Release Artifact and Download Publication

The repository must not turn `app-debug.apk` into a downloadable Alpha binary. Only a candidate that passes every P0/P1 gate and receives owner approval may be published. The release is produced through an annotated immutable tag and contains exactly the signed artifact, checksum, and reviewed notes described below.[3]

| Check | Evidence to retain | Pass condition | Initial state |
|---|---|---|---|
| Release signing | Owner-controlled signing configuration and build provenance | Signed APK is generated without committing, printing, or uploading signing material. | Pending |
| Artifact integrity | SHA-256 file generated from signed APK | Checksum matches the downloadable artifact exactly. | Pending |
| Source provenance | Annotated immutable tag | Tag points to the approved candidate commit after all gates pass. | Pending |
| Release notes | Reviewed notes | State version, commit, compatible Android range, validation summary, privacy/security boundaries, limitations, install verification, upgrade/rollback guidance, and non-sensitive reporting path. | Pending |
| GitHub Release | Draft/review record and final public release URL | Only approved signed APK and `SHA256SUMS.txt` are attached; debug/research APK is not attached. | Pending |
| Post-publication verification | Independent download/hash check | Downloaded file hash matches published checksum; link and notes are correct. | Pending |

## 9. Final Decision Record

The release owner and reviewer complete this table only after every preceding release-gate row is Pass. Any P0/P1 Pending or Blocked item means the result remains **Not approved**.

| Decision field | Record |
|---|---|
| Candidate SHA and tag | Pending |
| Deterministic quality gate | Pending |
| Field/device/UX evidence | Pending |
| Unresolved P0/P1 findings | Pending |
| Signed artifact SHA-256 | Pending |
| Owner approval | Pending |
| Public GitHub Alpha release | **Not approved** |

## References

[1] [Phase 7.5 UX Validation and Hardening Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md)

[2] [Android Developers, “Test your app’s accessibility”](https://developer.android.com/guide/topics/ui/accessibility/testing)

[3] [GitHub Alpha Release Checklist](ALPHA_RELEASE.md)
