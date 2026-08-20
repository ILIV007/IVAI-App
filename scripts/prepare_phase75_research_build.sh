#!/usr/bin/env bash
# Prepares a local debug-build handoff package for Phase 7.5 research.
# It never installs the APK, starts the app, adds credentials, sends network traffic,
# or records participant data. Operators must use a clean app state and omit credentials.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_root="${1:-/tmp/ivai-phase75-research}"
commit="$(git -C "$repo_root" rev-parse HEAD)"
short_commit="$(git -C "$repo_root" rev-parse --short HEAD)"
package_dir="$output_root/$short_commit"
prepared_at_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

if [[ -n "$(git -C "$repo_root" status --porcelain)" ]]; then
  echo "Refusing to prepare a research build from a dirty worktree." >&2
  exit 1
fi

: "${JAVA_HOME:?Set JAVA_HOME to JDK 21 before preparing a research build.}"
: "${ANDROID_HOME:?Set ANDROID_HOME before preparing a research build.}"

rm -rf "$package_dir"
mkdir -p "$package_dir"

(
  cd "$repo_root"
  ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain \
    | tee "$package_dir/build-quality.log"
)

apk="$repo_root/app/build/outputs/apk/debug/app-debug.apk"
test_report="$repo_root/app/build/reports/tests/testDebugUnitTest/index.html"
lint_report="$repo_root/app/build/reports/lint-results-debug.html"

[[ -f "$apk" ]] || { echo "Debug APK was not produced." >&2; exit 1; }
[[ -f "$test_report" ]] || { echo "Unit-test report was not produced." >&2; exit 1; }
[[ -f "$lint_report" ]] || { echo "Lint report was not produced." >&2; exit 1; }

cp "$apk" "$package_dir/IVAI-phase75-research-debug.apk"
cp "$test_report" "$package_dir/unit-test-report.html"
cp "$lint_report" "$package_dir/lint-report.html"
cp "$repo_root/docs/PHASE7_5_CONTROLLED_SCENARIO_CARDS.md" "$package_dir/CONTROLLED_SCENARIO_CARDS.md"

(
  cd "$package_dir"
  sha256sum \
    IVAI-phase75-research-debug.apk \
    unit-test-report.html \
    lint-report.html \
    build-quality.log \
    CONTROLLED_SCENARIO_CARDS.md \
    > SHA256SUMS.txt
)

cat > "$package_dir/RESEARCH_PACKAGE_MANIFEST.txt" <<EOF
IVAI Phase 7.5 controlled research package manifest

Format: ivai-phase75-controlled-research-package/v1
Prepared at (UTC): $prepared_at_utc
Commit: $commit
Short commit: $short_commit
Build type: debug only
Release status: not a public Alpha artifact
Quality command: ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain

Integrity procedure:
1. Run sha256sum --check SHA256SUMS.txt from this directory before every session.
2. Confirm the commit and build type above against the intended controlled research build.
3. Run scripts/verify_phase75_research_package.sh against this directory before every session.
4. Do not treat this manifest, a successful command, or the local worksheet as participant/device evidence or Alpha approval.

Checksum-covered artifacts:
- IVAI-phase75-research-debug.apk
- unit-test-report.html
- lint-report.html
- build-quality.log
- CONTROLLED_SCENARIO_CARDS.md (facilitator-only static scenario context)
EOF

cat > "$package_dir/RESEARCH_SESSION_WORKSHEET.md" <<'EOF'
# IVAI Phase 7.5 — Local Session Worksheet

> **Status:** Blank local worksheet. It contains no result and must not be committed as participant or device evidence.
>
> **Package:** Commit: __IVAI_RESEARCH_COMMIT__; verify every checksum in SHA256SUMS.txt before use.

## Session preflight

| Check | Record only after it is actually verified |
|---|---|
| Anonymous session ID | Pending |
| Package manifest and checksums verified | Pending |
| Fresh/cleared local application data | Pending |
| No credential, account, connection, endpoint, model, Combo, Agent target, project file, or prior chat data | Pending |
| Local-only safety statement acknowledged | Pending |
| No provider request, endpoint connection, or Agent write will be initiated | Pending |

## De-identified task observation

| Task | Direct / recoverable / failure / not run | First route | Short non-sensitive observation | Finding ID |
|---|---|---|---|---|
| Change or review target | Pending | Pending | Pending | Pending |
| Review endpoint HTTPS trust | Pending | Pending | Pending | Pending |
| Create or review ordered Combo | Pending | Pending | Pending | Pending |
| Explain Agent write preview and Allow once | Pending | Pending | Pending | Pending |
| Recover from target failure and begin project chat | Pending | Pending | Pending | Pending |

## Device/accessibility observation

| Device class and Android version only | Configuration | Actual result | Non-sensitive screenshot/reference | Finding ID |
|---|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending |

## Transfer rule

Keep this worksheet and any identity mapping outside the repository. Transfer only approved, fully de-identified aggregate outcomes into docs/PHASE7_UIUX_VALIDATION.md. Never transfer names, contact details, device identifiers, credentials, prompts, workspace files, run traces, recordings, or raw network logs.
EOF
sed -i "s/__IVAI_RESEARCH_COMMIT__/$commit/g" "$package_dir/RESEARCH_SESSION_WORKSHEET.md"

cat > "$package_dir/README.txt" <<EOF
IVAI Phase 7.5 controlled research build

Commit: $commit
Build: debug only; not a public Alpha artifact
Purpose: voluntary UX, accessibility, and device evidence collection under docs/PHASE7_5_UX_VALIDATION_PROTOCOL.md

Mandatory session safety:
- Use a clean app state and add no API key, account, provider, endpoint, model, Combo, or Agent target.
- Do not send a request, connect a provider, perform an Agent write, or use a workspace containing personal data.
- Record only de-identified outcomes in the approved validation record; never store raw participant data in the repository.
- Run scripts/verify_phase75_research_package.sh against this package, then review RESEARCH_PACKAGE_MANIFEST.txt before every session.
- Use CONTROLLED_SCENARIO_CARDS.md only to establish hypothetical task context; it is not product state and must not be entered into IVAI.
- This package does not substitute for a signed release, physical-device evidence, or Alpha approval.

Contents:
- IVAI-phase75-research-debug.apk
- SHA256SUMS.txt (all transferred build artifacts)
- RESEARCH_PACKAGE_MANIFEST.txt
- build-quality.log
- unit-test-report.html
- lint-report.html
- RESEARCH_SESSION_WORKSHEET.md (blank local-only worksheet)
- CONTROLLED_SCENARIO_CARDS.md (facilitator-only static scenario cards)
EOF

"$repo_root/scripts/verify_phase75_research_package.sh" "$package_dir"
printf 'Prepared controlled Phase 7.5 research package: %s\n' "$package_dir"
