#!/usr/bin/env bash
# Verifies a local Phase 7.5 controlled-research package.
# It never installs an APK, opens the app, sends network traffic, or records participant data.
set -Eeuo pipefail

usage() {
  echo "Usage: $0 <phase75-package-directory>" >&2
  exit 64
}

package_dir="${1:-}"
[[ -n "$package_dir" ]] || usage
[[ -d "$package_dir" ]] || { echo "Package directory does not exist: $package_dir" >&2; exit 1; }

required_files=(
  "IVAI-phase75-research-debug.apk"
  "unit-test-report.html"
  "lint-report.html"
  "build-quality.log"
  "SHA256SUMS.txt"
  "RESEARCH_PACKAGE_MANIFEST.txt"
  "RESEARCH_SESSION_WORKSHEET.md"
  "README.txt"
)

for file_name in "${required_files[@]}"; do
  [[ -s "$package_dir/$file_name" ]] || {
    echo "Required package artifact is missing or empty: $file_name" >&2
    exit 1
  }
done

manifest="$package_dir/RESEARCH_PACKAGE_MANIFEST.txt"
worksheet="$package_dir/RESEARCH_SESSION_WORKSHEET.md"
readme="$package_dir/README.txt"
checksums="$package_dir/SHA256SUMS.txt"

require_manifest_line() {
  local expected="$1"
  grep -Fxq "$expected" "$manifest" || {
    echo "Package manifest is missing required line: $expected" >&2
    exit 1
  }
}

require_manifest_line "Format: ivai-phase75-controlled-research-package/v1"
require_manifest_line "Build type: debug only"
require_manifest_line "Release status: not a public Alpha artifact"
require_manifest_line "Quality command: ./gradlew clean assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain"

commit="$(sed -n 's/^Commit: //p' "$manifest")"
short_commit="$(sed -n 's/^Short commit: //p' "$manifest")"
[[ "$commit" =~ ^[0-9a-f]{40}$ ]] || {
  echo "Package manifest has an invalid commit SHA." >&2
  exit 1
}
[[ "$short_commit" =~ ^[0-9a-f]{7,40}$ ]] || {
  echo "Package manifest has an invalid short commit SHA." >&2
  exit 1
}
[[ "$commit" == "$short_commit"* ]] || {
  echo "Package manifest short commit does not match the full commit." >&2
  exit 1
}

expected_checksum_artifacts=(
  "IVAI-phase75-research-debug.apk"
  "unit-test-report.html"
  "lint-report.html"
  "build-quality.log"
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
  echo "Package checksum verification failed." >&2
  exit 1
}

grep -Fqx "> **Package:** Commit: $commit; verify every checksum in SHA256SUMS.txt before use." "$worksheet" || {
  echo "Worksheet does not match the package manifest commit." >&2
  exit 1
}
grep -Fq 'Blank local worksheet. It contains no result' "$worksheet" || {
  echo "Worksheet is missing its blank-result boundary." >&2
  exit 1
}
grep -Fq 'must not be committed as participant or device evidence' "$worksheet" || {
  echo "Worksheet is missing its repository boundary." >&2
  exit 1
}
grep -Fq 'Build: debug only; not a public Alpha artifact' "$readme" || {
  echo "README is missing the debug-only Alpha boundary." >&2
  exit 1
}
grep -Fq 'This package does not substitute for a signed release, physical-device evidence, or Alpha approval.' "$readme" || {
  echo "README is missing the Phase 7.5 evidence boundary." >&2
  exit 1
}

echo "Verified controlled Phase 7.5 research package for commit $commit."
