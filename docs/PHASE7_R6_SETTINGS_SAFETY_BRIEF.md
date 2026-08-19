# Phase 7 R6 — Settings Safety: Local Data Deletion Confirmation

## Goal

R6 adds a clear, foreground-only confirmation step before IVAI deletes all local data. The confirmation must make the destructive consequence legible, enumerate only the data categories that the existing resetter actually clears, and keep **Cancel** as the non-destructive default path.

> **Boundary:** R6 changes only the Settings presentation before the already-existing `onDeleteAllLocalData` callback. `LocalDataResetter`, Room cleanup, project-file cleanup, encrypted-vault cleanup, error propagation, provider handling, network policy, and Android backup policy remain unchanged.

## Confirmed Local Reset Scope

| Existing reset operation | User-facing confirmation category |
|---|---|
| `ProjectWorkspace.deleteAllProjectFiles()` | Local workspace project files |
| `LocalWorkspaceRepository.deleteAllWorkspaceData()` | Local database records, including chats, projects, Agent records, Connections, Accounts, Models, Combos, and related local state |
| `EncryptedSecretVault.clearAll()` | Encrypted stored provider credentials |

The confirmation must not claim that IVAI deletes operating-system backups, external files, remote provider data, an account, a cloud copy, or data belonging to another application.

## Interaction Contract

1. Selecting **Delete all local data** opens a confirmation dialog; it must not invoke the reset callback.
2. The dialog identifies the action as permanent and lists the three verified categories above.
3. **Cancel** dismisses the dialog without invoking the reset callback and is the non-destructive/default action.
4. The destructive confirmation action uses explicit copy, invokes the existing callback exactly once, and dismisses the dialog.
5. Dismissing by back/outside behaves as cancel. No timed confirmation, forced re-authentication, remote request, telemetry, or background reset is introduced.

## Acceptance Gate

| Gate | Requirement |
|---|---|
| Test-first behavior | A regression proves initial button click does not reset, Cancel/back does not reset, and explicit destructive confirmation invokes exactly once. |
| Honest disclosure | Copy lists only existing project-file, database-record, and encrypted-credential cleanup categories. |
| Accessibility | Dialog heading, action labels, and destructive action have semantic identifiers; Cancel remains visibly available and non-destructive. |
| Visual evidence | A dark-state Roborazzi capture verifies hierarchy, warning copy, and action distinction. |
| Architecture | No resetter, ViewModel, schema, vault, provider, transport, backend, analytics, or backup behavior changes. |
| Quality | Full debug/release/unit/lint gate plus contrast, launcher, provider-neutral, RTL, Phase 8.0, package, and security guards pass. |

## Deliberately Deferred

| Item | Reason |
|---|---|
| Export/backup before deletion | Requires separate user-owned archive format and restore guarantees; R6 must not imply one exists. |
| Typed-name or biometric confirmation | Not required to close the no-warning defect and would expand authentication/lockout accessibility scope. |
| Undo/recycle bin | Incompatible with the existing permanent local reset contract; requires a separate persistence/data-retention design. |
| Completion toast or optimistic reset state | Existing callback has no completion signal. R6 must not fabricate success while reset may still fail asynchronously. |
| Physical-device/assistive evidence | TalkBack, font-scale, Force-RTL, rotation, lifecycle and fresh-install evidence remain Phase 7.5/Alpha gates. |
