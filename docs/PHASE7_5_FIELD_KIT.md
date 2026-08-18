# Phase 7.5 — Controlled Research and Device Field Kit

> **Status:** Preparation only. No participant session, device result, screenshot, finding, or release approval is recorded by this document.
>
> **Authoritative protocol:** [Phase 7.5 UX Validation Protocol](PHASE7_5_UX_VALIDATION_PROTOCOL.md). This kit operationalizes the protocol; it does not replace its decision rules.

## Purpose and Boundaries

This kit enables a facilitator to prepare a reproducible debug-build package, conduct voluntary and de-identified UX/accessibility sessions, and transfer only permitted outcomes into [Phase 7 UX Validation Record](PHASE7_UIUX_VALIDATION.md). It does not add telemetry, analytics, a backend, background work, participant tracking, or runtime behavior.

> **Never use real API keys, provider accounts, endpoints, prompts, workspace files, run traces, personal device identifiers, screen recordings, or actual Agent writes in a research session.** The research build is a controlled local prototype, not an Alpha artifact.

## 1. Prepare a Controlled Build

Run the following command from a clean checkout with JDK 21 and Android SDK configured:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/tmp/android-sdk-ivai
./scripts/prepare_phase75_research_build.sh
```

The helper runs the clean build, unit tests, and lint; then writes a local package under `/tmp/ivai-phase75-research/<commit>/` containing a debug APK, build-quality log, test/lint reports, a manifest, full-artifact SHA-256 checksums, and a blank local worksheet. The package is intentionally outside the repository and must not be committed or published.

Before any participant receives a device, the facilitator must confirm all of the following:

| Check | Required outcome |
|---|---|
| Build provenance | `RESEARCH_PACKAGE_MANIFEST.txt` commit matches the intended research build and declares debug-only status. |
| Integrity | `sha256sum --check SHA256SUMS.txt` succeeds for the APK, test/lint reports, and build-quality log. |
| Quality provenance | `build-quality.log` records the successful clean debug/test/lint command used for this package. |
| App state | Fresh/cleared local app data; no account, credential, provider connection, endpoint, Combo, Agent target, project file, or prior chat data. |
| Network and write safety | No real provider request, local endpoint connection, or Agent write will be initiated. |
| Facilitator record | The blank local worksheet is used only with anonymous IDs and permitted non-sensitive outcomes; identity mapping remains outside the repository. |

## 2. Opening Script and Consent

Use the following neutral statement before every session:

> “This is a local prototype. Participation is voluntary. Nothing you do will send a request, use a credential, or write a file. We will not collect your name, contact details, device ID, prompts, files, or recordings. You may stop at any time.”

Assign a neutral ID such as `P01`. Keep any contact mapping outside the repository. Do not enter participant names or raw quotations containing sensitive information into GitHub or the app.

## 3. Moderated Task Order

Randomize the five task prompts per participant. Do not give route hints before a first attempt. Record direct success separately from recoverable success.

| Task | Prompt | What to record |
|---:|---|---|
| 1 | “Show where you would review or change the model or Combo a chat will use.” | First path; whether the participant states target choice is explicit. |
| 2 | “Show where you would review an endpoint’s HTTPS trust setting before saving a connection.” | First path; confirmation point identified. |
| 3 | “Show where you would create and review an ordered fallback Combo.” | First path; whether automatic discovery is incorrectly expected. |
| 4 | “An Agent asks to write a project file. Explain what you would check and what Allow once means.” | Explanation of bounded preview/path and one-time decision. |
| 5 | “A target fails. Show how you would recover, then begin a chat in a selected local project.” | Recovery path and project-chat route; no claim that a message sends automatically. |

A safety-critical misunderstanding of active target, write approval, or recovery is a P1 finding. Stop the scenario rather than letting a participant enter credentials or attempt a real write.

## 4. Device and Accessibility Sweep

Use at least one compact and one medium physical Android device. For each configuration, use no sensitive data and capture only non-sensitive screenshots when appropriate.

| Area | Required check | Pass condition |
|---|---|---|
| Installation/lifecycle | Fresh install, upgrade, restart, rotation, local-data reset | No crash, loss, silent migration, or unexpected execution. |
| Layout | Portrait/landscape; dark/light; default/large font scale | No task-critical clipping, overlap, or unreachable control. |
| BiDi | Force-RTL with English/Persian/Arabic mixed content | Natural direction is preserved; only code/footer exceptions remain LTR. |
| TalkBack | Linear swipe and explore-by-touch for target, approval, status, and destructive-data controls | Critical control is reachable once and has concise spoken meaning. |
| Network safety | Offline and explicit HTTPS loopback/private-LAN cancellation/timeout | Safe recovery; no HTTP, discovery, scanning, implicit trust, or duplicate side effect. |

## 5. Recording and Exit

Transfer only approved, de-identified aggregate outcomes from the local worksheet to the validation record. Retain the package manifest, checksum verification result, and raw worksheet outside the repository. The Phase 7.5 exit gate remains blocked until actual study/device evidence is present, all P0/P1 findings are closed, and the final full regression gate passes.

| Situation | Required disposition |
|---|---|
| P0 or P1 found | Block Phase 7 completion; create a focused remediation increment, retest, and record new evidence. |
| P2/P3 found | Record in the validation record; prioritize separately without weakening safety gates. |
| No actual participants/devices | Leave results pending; do not infer pass status from JVM, Roborazzi, or this kit. |
| All field gates pass | Re-run final regression and proceed to owner-controlled Phase 6 signing/release evidence. |
