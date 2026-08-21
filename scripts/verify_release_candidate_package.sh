#!/usr/bin/env bash
# Verifies a local, unsigned IVAI Release Candidate evidence package.
# It never signs, tags, uploads, installs, or declares a public/stable release.
set -Eeuo pipefail

usage() {
  echo "Usage: $0 <release-candidate-package-directory>" >&2
  exit 64
}

package_dir="${1:-}"
[[ -n "$package_dir" ]] || usage
[[ -d "$package_dir" ]] || { echo "Package directory does not exist: $package_dir" >&2; exit 1; }

required_files=(
  "RELEASE_CANDIDATE_MANIFEST.txt"
  "README.txt"
  "SHA256SUMS.txt"
  "mapping.txt"
  "unit-test-report.html"
  "lint-report.html"
  "build-quality.log"
  "security-invariant-scan.txt"
  "repository-integrity.txt"
  "source-state.txt"
  "signing-state.txt"
)
for file_name in "${required_files[@]}"; do
  [[ -s "$package_dir/$file_name" ]] || {
    echo "Required candidate artifact is missing or empty: $file_name" >&2
    exit 1
  }
done

manifest="$package_dir/RELEASE_CANDIDATE_MANIFEST.txt"
readme="$package_dir/README.txt"
checksums="$package_dir/SHA256SUMS.txt"

require_manifest_line() {
  local expected="$1"
  grep -Fxq "$expected" "$manifest" || {
    echo "Release Candidate manifest is missing required line: $expected" >&2
    exit 1
  }
}

require_manifest_line "Format: ivai-local-release-candidate/v1"
require_manifest_line "Build type: minified unsigned release candidate"
require_manifest_line "Release status: local preparation only; not signed, tagged, uploaded, public, or stable-approved"
require_manifest_line "Unit tests: 160 total, 0 failures, 0 errors, 0 skipped"

commit="$(sed -n 's/^Commit: //p' "$manifest")"
short_commit="$(sed -n 's/^Short commit: //p' "$manifest")"
candidate_id="$(sed -n 's/^Candidate ID: //p' "$manifest")"
version_name="$(sed -n 's/^Version name: //p' "$manifest")"
[[ "$commit" =~ ^[0-9a-f]{40}$ ]] || { echo "Candidate manifest has an invalid commit SHA." >&2; exit 1; }
[[ "$short_commit" =~ ^[0-9a-f]{7,40}$ ]] || { echo "Candidate manifest has an invalid short commit SHA." >&2; exit 1; }
[[ "$commit" == "$short_commit"* ]] || { echo "Candidate short commit does not match full commit." >&2; exit 1; }
[[ "$version_name" =~ ^[0-9]+(\.[0-9]+)*([-.][0-9A-Za-z.]+)?$ ]] || { echo "Candidate manifest has an invalid version name." >&2; exit 1; }
[[ "$candidate_id" == "${version_name}-rc-${short_commit}" ]] || { echo "Candidate ID does not match version and short commit." >&2; exit 1; }

unsigned_apk="IVAI-${candidate_id}-unsigned.apk"
debug_apk="IVAI-${candidate_id}-debug.apk"
[[ -s "$package_dir/$unsigned_apk" ]] || { echo "Unsigned release APK is missing." >&2; exit 1; }
[[ -s "$package_dir/$debug_apk" ]] || { echo "Debug APK is missing." >&2; exit 1; }

expected_checksum_artifacts=(
  "$unsigned_apk"
  "$debug_apk"
  "mapping.txt"
  "unit-test-report.html"
  "lint-report.html"
  "build-quality.log"
  "security-invariant-scan.txt"
  "repository-integrity.txt"
  "source-state.txt"
  "signing-state.txt"
)
mapfile -t checksum_artifacts < <(awk '{print $2}' "$checksums")
[[ "${#checksum_artifacts[@]}" -eq "${#expected_checksum_artifacts[@]}" ]] || {
  echo "Checksum file does not contain the expected artifact count." >&2
  exit 1
}
for index in "${!expected_checksum_artifacts[@]}"; do
  [[ "${checksum_artifacts[$index]}" == "${expected_checksum_artifacts[$index]}" ]] || {
    echo "Checksum file has an unexpected artifact inventory." >&2
    exit 1
  }
done
(
  cd "$package_dir"
  sha256sum --check --status SHA256SUMS.txt
) || {
  echo "Release Candidate checksum verification failed." >&2
  exit 1
}

grep -Fxq 'CLEAN' "$package_dir/source-state.txt" || {
  echo "Candidate source-state evidence is not clean." >&2
  exit 1
}
grep -Fxq 'PASS: release APK is unsigned (apksigner verify rejected missing signature)' "$package_dir/signing-state.txt" || {
  echo "Candidate signing-state evidence is incomplete or does not prove an unsigned APK." >&2
  exit 1
}
grep -Fq 'PASS: git fsck --full --no-reflogs' "$package_dir/repository-integrity.txt" || {
  echo "Candidate repository-integrity evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no tracked local/signing secret material' "$package_dir/repository-integrity.txt" || {
  echo "Candidate tracked-secret review evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no hardcoded credential pattern' "$package_dir/security-invariant-scan.txt" || {
  echo "Candidate security-invariant evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no cleartext/trust bypass' "$package_dir/security-invariant-scan.txt" || {
  echo "Candidate transport-invariant evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no prohibited execution' "$package_dir/security-invariant-scan.txt" || {
  echo "Candidate execution-invariant evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no implicit provider selection' "$package_dir/security-invariant-scan.txt" || {
  echo "Candidate provider-neutral evidence is incomplete." >&2
  exit 1
}
grep -Fq 'PASS: no global forced-LTR shell override' "$package_dir/security-invariant-scan.txt" || {
  echo "Candidate RTL evidence is incomplete." >&2
  exit 1
}
grep -Fq 'Status: unsigned internal evidence package; not a public/stable download' "$readme" || {
  echo "Candidate README is missing the unsigned/non-public boundary." >&2
  exit 1
}
grep -Fq 'Public/stable release remains blocked until real Phase 7.5 participant/device evidence' "$readme" || {
  echo "Candidate README is missing the physical-evidence boundary." >&2
  exit 1
}

echo "Verified local unsigned IVAI Release Candidate for commit $commit."
