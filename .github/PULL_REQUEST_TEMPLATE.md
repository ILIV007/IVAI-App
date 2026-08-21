# IVAI Pull Request

## Task packet and goal

- **Task ID / roadmap phase:**
- **Goal:**
- **Roadmap, audit, or decision reference:**

## Scope

- **In scope:**
- **Deliberately out of scope:**
- **Scope deviation:** None / explain explicitly.
- **Provider/runtime/network behavior changed:** No / explain explicitly.

## Validation

- [ ] `bash scripts/check_android_sdk_provisioning_contract.sh`
- [ ] `bash scripts/test_android_sdk_provisioning_contract.sh`
- [ ] Relevant package/helper regression scripts passed.
- [ ] `./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain`
- [ ] Focused regressions were added or updated for confirmed behavioral changes.
- [ ] `git diff --check` passed.
- [ ] UI change includes English/LTR and mixed RTL/BiDi verification when applicable.
- [ ] Test total/baseline impact: None / explain and align active RC helper, verifier, fixture, and documentation.

## Security, data, and compatibility

- [ ] No API key, secret, user data, `local.properties`, signing material, or generated output is included.
- [ ] Credential, network, endpoint-trust, file, export, tool, migration, permission, dependency, and license impact were reviewed.
- [ ] Local-first, Backendless, BYOK, explicit-target, and approval-first boundaries remain intact.
- [ ] Data migration / rollback impact: None / explain.
- [ ] Provider-neutrality and release-signing/publication impact: None / explain.

## Evidence and handoff

- **Commands and results:**
- **Screenshots, semantics, test report, or package evidence:**
- **Known limitations / deferred physical gates:**
- **Reviewer focus:**

> A green PR does not authorize signing, tagging, uploading, or publishing an Alpha release. Those actions remain subject to the active release policy and owner approval.
