#!/usr/bin/env bash
# Regression checks for the local Phase 7.5 research-package verifier.
# Uses only a temporary synthetic fixture; it never installs an APK or records study data.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
validator="$repo_root/scripts/verify_phase75_research_package.sh"
fixture="$(mktemp -d "${TMPDIR:-/tmp}/ivai-phase75-verifier.XXXXXX")"

cleanup() {
  rm -rf "$fixture"
}
trap cleanup EXIT

commit="$(git -C "$repo_root" rev-parse HEAD)"
short_commit="$(git -C "$repo_root" rev-parse --short HEAD)"

printf 'synthetic package artifact\n' > "$fixture/IVAI-phase75-research-debug.apk"
printf 'synthetic unit-test report\n' > "$fixture/unit-test-report.html"
printf 'synthetic lint report\n' > "$fixture/lint-report.html"
printf 'BUILD SUCCESSFUL\n' > "$fixture/build-quality.log"
cat > "$fixture/CONTROLLED_SCENARIO_CARDS.md" <<'EOF'
# Phase 7.5 — Controlled Scenario Cards

> These cards are facilitator-only research material; they are **not** product screens, runtime data, test results, or evidence.

Do not create the described connection, credential, target, Combo, Agent, run, approval, failure, project file, or chat content in the application.
EOF

cat > "$fixture/RESEARCH_PACKAGE_MANIFEST.txt" <<EOF
IVAI Phase 7.5 controlled research package manifest

Format: ivai-phase75-controlled-research-package/v1
Prepared at (UTC): 2026-08-18T00:00:00Z
Commit: $commit
Short commit: $short_commit
Build type: debug only
Release status: not a public Alpha artifact
Quality command: ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain
EOF

cat > "$fixture/RESEARCH_SESSION_WORKSHEET.md" <<EOF
> **Status:** Blank local worksheet. It contains no result and must not be committed as participant or device evidence.
> **Package:** Commit: $commit; verify every checksum in SHA256SUMS.txt before use.
EOF

cat > "$fixture/README.txt" <<'EOF'
Build: debug only; not a public Alpha artifact
Use CONTROLLED_SCENARIO_CARDS.md only to establish hypothetical task context; it is not product state and must not be entered into IVAI.
This package does not substitute for a signed release, physical-device evidence, or Alpha approval.
EOF

(
  cd "$fixture"
  sha256sum \
    IVAI-phase75-research-debug.apk \
    unit-test-report.html \
    lint-report.html \
    build-quality.log \
    CONTROLLED_SCENARIO_CARDS.md \
    > SHA256SUMS.txt
)

"$validator" "$fixture"
printf 'tampered after checksum\n' >> "$fixture/lint-report.html"

if "$validator" "$fixture"; then
  echo "Verifier accepted a tampered package artifact." >&2
  exit 1
fi

echo "Phase 7.5 research-package verifier regression checks passed."
