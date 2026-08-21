# Contributing to IVAI

Thank you for improving IVAI. This repository is a **Local-first, Backendless, BYOK Android Agent Harness**. Every contribution must preserve explicit user control of provider, account, endpoint, model, Combo, execution target, Agent profile, local data, and one-time write approval.

## Development model

IVAI uses small, reviewable task packets. Begin from current `main` on one focused branch named `fix/`, `feat/`, `docs/`, `test/`, or `chore/`. Submit a pull request; direct pushes to protected `main` are not the normal workflow. Do not make concurrent edits to the same file or expand a task outside the accepted roadmap without a decision record.

> **Alpha boundary:** A green build, debug APK, unsigned candidate, or documentation update is not authorization to create a signed APK, tag, upload, GitHub Release, or public Alpha artifact. Read [Alpha Release Policy](docs/ALPHA_RELEASE.md) and [Release Readiness Checklist](docs/RELEASE_READINESS_CHECKLIST.md).

## Local setup

Use **JDK 21** and an Android SDK containing `platforms;android-37.1` and `build-tools;37.0.0`.

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
```

Do not commit `local.properties`, keystores, password files, credentials, private prompts, workspace files, device identifiers, or generated build outputs. `.editorconfig` and `.gitattributes` define repository text/binary hygiene; do not bypass them with unrelated formatting churn.

## Before opening a pull request

Run the applicable checks with the same toolchain and contract as protected CI:

```bash
bash scripts/check_android_sdk_provisioning_contract.sh
bash scripts/test_android_sdk_provisioning_contract.sh
bash scripts/test_phase75_research_package_verifier.sh
bash scripts/test_release_candidate_package_verifier.sh
bash scripts/test_owner_signed_release_evidence_helper.sh
bash scripts/check_provider_neutral_branding.sh
bash scripts/check_rtl_bounded_exceptions.sh
bash scripts/check_phase80_architecture_readiness.sh
bash scripts/check_provider_model_test_readiness.sh
./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain
git diff --check
```

The active deterministic baseline is **160 unit tests with zero failures, errors, and skips**. Do not change that baseline merely to make a command pass; any intentional test-count change must include focused regression evidence and aligned Release Candidate helper, verifier, fixture, and active documentation updates.

A UI change must include English/LTR and mixed RTL/BiDi verification when it affects message content, composer behavior, code blocks, URLs, model IDs, or navigation. A change to data, secret, network, file access, tools, migrations, permissions, dependencies, release tooling, or provider behavior must describe security, compatibility, and rollback impact.

## Scope and commit rules

Use focused conventional prefixes such as `fix:`, `feat:`, `docs:`, `test:`, or `chore:`. Keep implementation, test, and documentation changes together only when they serve one coherent safety contract. Do not combine a provider/runtime feature with unrelated hardening or a UI redesign with an unrelated data-layer change.

Do not introduce a central backend, analytics by default, implicit provider/model selection, cleartext transport, unrestricted automation, shell access, MCP process execution, local-model inference, background agents, or public release automation without a separately approved roadmap phase and decision record.

## Pull-request expectations

Complete the repository pull-request template with the task goal, roadmap/decision reference, in-scope and out-of-scope work, commands run, test results, security/data impact, migration/rollback impact, known limitations, and reviewer focus. Include screenshots or semantics/RTL evidence where relevant. Reviewers should reject undocumented scope expansion and changes that cannot be validated deterministically.

The protected `main` workflow requires review and both CI gates: Secret scan plus Android build/unit-test/lint. Repository history uses squash merge for focused, auditable changes.

## Security, conduct, and support

Do **not** open a public issue for a suspected vulnerability, exposed credential, data-loss path, unsafe execution path, or release-integrity problem. Follow the private process in [SECURITY.md](SECURITY.md). Community behavior rules are in [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). General documentation entry points are listed in [docs/README.md](docs/README.md).

## License

Contributions are made under Apache-2.0. Reference projects may be used only for behavioral analysis and clean-room implementation; do not copy code, prompts, assets, or components with incompatible licenses.
