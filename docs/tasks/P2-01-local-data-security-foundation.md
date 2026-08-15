# P2-01 — Local Data and Security Foundation

## Metadata

| Field | Value |
|---|---|
| Owner | IVAI implementation agent |
| Branch | `feat/local-data-security` |
| Base | `origin/main`; rebase after Phase 1 UI/RTL merges |
| Target phase | Phase 2 — Local Data and Security |
| Roadmap reference | `docs/ROADMAP.md`, Phase 2 |

## Goal

Establish a versioned, local-only persistence foundation for workspace projects, conversation threads, and messages. Establish a Keystore-backed ciphertext boundary for future provider credentials without adding a provider, credential-entry UI, network call, backup, export, or telemetry.

## Design decisions

Room `2.8.4` is selected for the relational workspace store, its KSP compiler, committed schema export, and migration testing support. DataStore Preferences `1.2.1` holds a small encrypted vault record and non-secret local settings. The Android Keystore owns non-exportable AES/GCM key material; only a versioned ciphertext envelope may enter DataStore. The code exposes cipher and vault interfaces so JVM tests never need a hardware-backed Keystore. [1] [2]

## In scope

| Area | Included work |
|---|---|
| Build | Add KSP, Room runtime/compiler/ktx/testing, Room schema export, and DataStore Preferences dependencies. |
| Local data | Add Room entities, DAOs, database version `1`, converters, repository boundary, and migrations/schema tests. |
| Vault boundary | Add AES-GCM envelope model, `SecretCipher` contract, Android Keystore implementation, DataStore-backed encrypted vault, redacted metadata, and destructive clear. |
| Tests | In-memory Room DAO/repository tests, migration/schema validation, fake-cipher vault tests, corruption/invalid-envelope rejection, and no-plaintext persistence checks. |
| Documentation | Record schema, ownership, sensitive-data boundary, deletion behavior, and phase limitations. |

## Out of scope

No real provider key entry screen, API request, OpenRouter/Gemini adapter, Room-to-UI migration, encrypted backup, export/import, Google Drive, app-wide theme persistence wiring, agent tools, or telemetry is included. No raw secret, prompt, response, or internal reasoning is logged.

## Security constraints

The vault must never return or persist a raw secret outside its caller boundary. Ciphertext uses a distinct versioned envelope containing only version, base64 IV, and base64 ciphertext. Android Keystore aliases are internal constants; logs contain only redacted label and state. A corrupted envelope must fail closed and not silently replace data. Deleting a vault entry removes its DataStore ciphertext and deletes the corresponding Keystore alias when safe to do so.

## Acceptance criteria

| Area | Required outcome |
|---|---|
| Schema | Room database version `1` exports schemas into the repository and defines project, thread, and message relationships. |
| Data integrity | Thread deletion cascades to messages; invalid foreign references cannot be persisted; repository maps records without dropping BiDi/code metadata. |
| Process state | Repository `Flow` emits deterministic snapshots after upsert/delete operations. |
| Vault | Tests demonstrate persisted values are ciphertext envelopes, not plaintext; invalid envelopes are rejected; clear removes the stored record. |
| Keystore | The Android implementation uses `AndroidKeyStore` AES/GCM and no deprecated encrypted-preferences API. |
| Validation | `assembleDebug`, `testDebugUnitTest`, `lintDebug`, schema/migration tests, diff check, and PR CI pass. |

## Risks and rollback

Room and KSP change the build graph; validate immediately after adding dependencies. Keystore behavior cannot be fully exercised in a JVM test, so its cryptographic boundary is isolated behind a fakeable contract and later receives device validation before a release. No user data exists yet; rollback consists of reverting the feature branch and deleting the v1 app database/vault in a development install.

## References

[1]: https://developer.android.com/jetpack/androidx/releases/room "Android Developers — Room release notes and dependency guidance"
[2]: https://developer.android.com/jetpack/androidx/releases/datastore "Android Developers — DataStore release notes and dependency guidance"
[3]: https://kotlinlang.org/docs/ksp-quickstart.html "Kotlin — KSP quickstart"
