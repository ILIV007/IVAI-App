# Task Packet Template

## Metadata

- **ID:**
- **Title:**
- **Owner:**
- **Branch:**
- **Target phase:**
- **Decision or roadmap reference:**

## Goal

State the smallest user or engineering outcome this task must achieve.

## Context files to read

List the repository and specification files required before implementation.

## In scope

List the exact behavior, files, and tests expected to change.

## Out of scope

List nearby features, refactors, integrations, or platform changes explicitly excluded from this task.

## Constraints

State local-first, BYOK, privacy, license, compatibility, RTL/BiDi, dependency, performance, and branch constraints that apply.

## Acceptance criteria

Write observable, testable outcomes. Include expected UI state, persistence/security behavior, failure behavior, and regression coverage when relevant.

## Commands to run

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Add specific test commands, screenshots, device checks, or manual QA scenarios as needed.

## Expected artifacts

List code, tests, docs, screenshots, migrations, or release outputs expected from the task.

## Risks and rollback

Describe data loss, secret exposure, migration, UI regression, network, or release risks and the rollback plan.

## Handoff report

Record changed files, commands and results, screenshots/RTL evidence, security/data impact, known limitations, scope deviations, commit SHA, and next action.
