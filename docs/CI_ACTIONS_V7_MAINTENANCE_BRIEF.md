# CI Actions v7 Maintenance Brief

> **Status:** Deterministically complete in [PR #141](https://github.com/ILIV007/IVAI-App/pull/141), squash-merged as `7c089975a1e6c97cff2ef0bab6b813530c7499c9`. This is a workflow-only maintenance increment; it does not change IVAI Android application behavior or release approval.

## Validation Outcome

The focused current-main PR passed the protected **Secret scan** and **Build, unit test, and lint** workflow. This validates the v7 workflow update on GitHub-hosted CI, including its existing Android build/test/lint and repository guards. It does **not** create an Android release artifact, execute physical device/accessibility/network validation, change signing status, or close any Alpha gate.

## Goal

Advance the repository workflow from `actions/checkout@v5` to `actions/checkout@v7` in both jobs and from `actions/upload-artifact@v4` to `actions/upload-artifact@v7` in the failure-report step.

## Scope and Compatibility

The official `checkout` documentation states that v7 is ESM-based and requires an Actions Runner version at least `v2.327.1`; it also adds safer defaults for privileged fork-checkout triggers. The repository uses GitHub-hosted `ubuntu-latest` and only `pull_request`/`push` triggers, with no unsafe fork-checkout opt-in, so no workflow-input change is required.[1]

The official `upload-artifact` documentation states that v7 runs on Node 24 and requires the same runner floor. The existing IVAI failure-report step uses supported `path` and `if-no-files-found: ignore` inputs and does not opt into hidden-file uploads, so no artifact-policy change is required.[2]

| In scope | Deliberately unchanged |
|---|---|
| Three reviewed `uses:` references in `.github/workflows/android-quality.yml` | Android source/resources/tests, Gradle/JDK/SDK configuration, CI commands, report paths, artifact name, failure-only upload condition, permissions, branch protection, Provider/Router/Agent/Data runtime, Room/vault, network policy, backend/telemetry, signing, UX, physical validation, Alpha status. |

## Acceptance Gate

The update is acceptable only when the fresh focused PR passes the protected **Secret scan** and **Build, unit test, and lint** checks. The latter must still execute the existing architecture guards, debug/minified-release builds, unit tests and lint. A green historical Dependabot workflow alone is insufficient because both original PRs were behind current `main`.

## Deferred

Real-device, accessibility, network, research, signing and release gates remain governed by the Phase 7.5/UX-8 record and are not advanced by this workflow dependency update.

## References

[1]: https://github.com/actions/checkout "actions/checkout official documentation"
[2]: https://github.com/actions/upload-artifact "actions/upload-artifact official documentation"
