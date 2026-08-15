# IVAI Agent Map

## Project intent

IVAI is a native Android, local-first, BYOK AI workspace. The current implementation is a Compose UI Skeleton. The repository is the source of truth for code, build instructions, and accepted technical decisions.

## Read first

1. `README.md` for environment and build commands.
2. `IVAI_PROJECT_MASTER.md` for product boundaries and current phase.
3. `docs/ARCHITECTURE.md`, `docs/SECURITY.md`, and `docs/RTL_BIDI.md` for changes in those areas.
4. `docs/TASK_TEMPLATE.md` before beginning a new task.

## Stable rules

- Use one implementation owner per task and work on a focused branch.
- Run `assembleDebug`, `testDebugUnitTest`, and `lintDebug` before handoff when applicable.
- Do not add a dependency, architecture layer, network call, secret store, agent tool, or feature outside the roadmap without an approved decision record.
- Do not commit secrets, `local.properties`, build output, or user data.
- Keep the Alpha local-first: no mandatory backend or default telemetry.
- Reference projects are for clean-room behavioral analysis only; do not copy incompatible code or prompts.

## Current phase boundary

Phase 0 repository governance is being completed. The next eligible product work is Phase 1 UI/RTL closeout, followed by local data/security before any real Provider integration.
