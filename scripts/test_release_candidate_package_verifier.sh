#!/usr/bin/env bash
# Regression checks for the local unsigned Release Candidate package verifier.
# Uses only a temporary synthetic fixture; it never creates a signed/public release.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
validator="$repo_root/scripts/verify_release_candidate_package.sh"
fixture="$(mktemp -d "${TMPDIR:-/tmp}/ivai-release-candidate-verifier.XXXXXX")"

cleanup() {
  rm -rf "$fixture"
}
trap cleanup EXIT

commit="$(git -C "$repo_root" rev-parse HEAD)"
short_commit="$(git -C "$repo_root" rev-parse --short HEAD)"
version_name="1.0"
candidate_id="${version_name}-rc-${short_commit}"
unsigned_apk="IVAI-${candidate_id}-unsigned.apk"
debug_apk="IVAI-${candidate_id}-debug.apk"

printf 'synthetic unsigned release artifact\n' > "$fixture/$unsigned_apk"
printf 'synthetic debug artifact\n' > "$fixture/$debug_apk"
printf 'synthetic mapping\n' > "$fixture/mapping.txt"
printf 'synthetic unit-test report\n' > "$fixture/unit-test-report.html"
printf 'synthetic lint report\n' > "$fixture/lint-report.html"
printf 'BUILD SUCCESSFUL\n' > "$fixture/build-quality.log"
printf 'PASS: no hardcoded credential pattern\nPASS: no cleartext/trust bypass\nPASS: no prohibited execution\nPASS: no implicit provider selection\nPASS: no global forced-LTR shell override\n' > "$fixture/security-invariant-scan.txt"
printf 'PASS: git fsck --full --no-reflogs\nPASS: no tracked local/signing secret material\n' > "$fixture/repository-integrity.txt"
printf 'CLEAN\n' > "$fixture/source-state.txt"
printf 'PASS: release APK is unsigned (apksigner verify rejected missing signature)\n' > "$fixture/signing-state.txt"

cat > "$fixture/RELEASE_CANDIDATE_MANIFEST.txt" <<EOF
Format: ivai-local-release-candidate/v1
Candidate ID: $candidate_id
Commit: $commit
Short commit: $short_commit
Version name: $version_name
Build type: minified unsigned release candidate
Release status: local preparation only; not signed, tagged, uploaded, public, or stable-approved
Unit tests: 155 total, 0 failures, 0 errors, 0 skipped
EOF

cat > "$fixture/README.txt" <<'EOF'
Status: unsigned internal evidence package; not a public/stable download
Public/stable release remains blocked until real Phase 7.5 participant/device evidence, owner-controlled signed-artifact provenance, approved checksum, annotated tag, reviewed release notes, and owner approval exist for one exact candidate commit.
EOF

(
  cd "$fixture"
  sha256sum \
    "$unsigned_apk" \
    "$debug_apk" \
    mapping.txt \
    unit-test-report.html \
    lint-report.html \
    build-quality.log \
    security-invariant-scan.txt \
    repository-integrity.txt \
    source-state.txt \
    signing-state.txt \
    > SHA256SUMS.txt
)

"$validator" "$fixture"
printf 'tampered after checksum\n' >> "$fixture/mapping.txt"

if "$validator" "$fixture"; then
  echo "Release Candidate verifier accepted a tampered artifact." >&2
  exit 1
fi

echo "Release Candidate package verifier regression checks passed."
