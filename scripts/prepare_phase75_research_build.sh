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
  ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
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
(
  cd "$package_dir"
  sha256sum IVAI-phase75-research-debug.apk > SHA256SUMS.txt
)

cat > "$package_dir/README.txt" <<EOF
IVAI Phase 7.5 controlled research build

Commit: $commit
Build: debug only; not a public Alpha artifact
Purpose: voluntary UX, accessibility, and device evidence collection under docs/PHASE7_5_UX_VALIDATION_PROTOCOL.md

Mandatory session safety:
- Use a clean app state and add no API key, account, provider, endpoint, model, Combo, or Agent target.
- Do not send a request, connect a provider, perform an Agent write, or use a workspace containing personal data.
- Record only de-identified outcomes in the approved validation record; never store raw participant data in the repository.
- This package does not substitute for a signed release, physical-device evidence, or Alpha approval.

Contents:
- IVAI-phase75-research-debug.apk
- SHA256SUMS.txt
- unit-test-report.html
- lint-report.html
EOF

printf 'Prepared controlled Phase 7.5 research package: %s\n' "$package_dir"
