# Contributing to IVAI

## Development model

IVAI is developed through small, reviewable task packets. Each task has one implementation owner at a time. Do not make concurrent edits to the same file or introduce a feature that is outside the accepted roadmap without a decision record.

All changes begin from the current `main` branch on a focused branch named with a clear prefix such as `fix/`, `feat/`, `docs/`, or `chore/`. Submit changes through a pull request; direct pushes to `main` are not part of the normal workflow.

## Before opening a pull request

Run the applicable checks with a full JDK 17+ and Android SDK configured:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

A UI change must include English/LTR and mixed RTL/BiDi verification when it affects message content, composer behavior, code blocks, URLs, model IDs, or navigation. A data, secret, network, file, tool, migration, or dependency change must explain the security and rollback impact in the pull request.

## Commit and scope rules

Use focused commit messages such as `fix:`, `feat:`, `docs:`, `test:`, or `chore:`. Do not commit API keys, `local.properties`, generated build output, screenshots containing user data, or unrelated refactors. The current Alpha remains local-first and BYOK; it does not include a central backend, telemetry by default, unrestricted automation, shell access, MCP process execution, or local model inference.

## Review expectations

The pull request description must state the task goal, in-scope and out-of-scope work, commands run, test results, screenshot or RTL evidence when relevant, security/data impact, known limitations, and follow-up work. Reviewers should reject undocumented scope expansion and changes that cannot be validated locally.

## License and references

Contributions are made under Apache-2.0. Reference projects may be used only for behavioral analysis and clean-room implementation; do not copy code, prompts, assets, or components with incompatible licenses.
