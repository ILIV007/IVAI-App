#!/usr/bin/env bash
# Prepares a local, unsigned IVAI Release Candidate evidence package.
# It never signs, tags, uploads, publishes, installs, or declares a public/stable release.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_root="${1:-/tmp/ivai-release-candidates}"
commit="$(git -C "$repo_root" rev-parse HEAD)"
short_commit="$(git -C "$repo_root" rev-parse --short HEAD)"
version_name="$(sed -n 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"\([^"]*\)"/\1/p' "$repo_root/app/build.gradle.kts")"
prepared_at_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

[[ -n "$version_name" ]] || { echo "Unable to determine versionName from app/build.gradle.kts." >&2; exit 1; }
[[ -n "${JAVA_HOME:-}" ]] || { echo "Set JAVA_HOME to JDK 21 before preparing a Release Candidate." >&2; exit 1; }
[[ -n "${ANDROID_HOME:-}" ]] || { echo "Set ANDROID_HOME before preparing a Release Candidate." >&2; exit 1; }

output_root_absolute="$(realpath -m "$output_root")"
case "$output_root_absolute" in
  "$repo_root"|"$repo_root"/*)
    echo "Release Candidate output must stay outside the repository." >&2
    exit 1
    ;;
esac

if [[ -n "$(git -C "$repo_root" status --porcelain)" ]]; then
  echo "Refusing to prepare a Release Candidate from a dirty worktree." >&2
  exit 1
fi

git -C "$repo_root" diff --check
git -C "$repo_root" fsck --full --no-reflogs >/dev/null
if git -C "$repo_root" ls-files | grep -E '(^|/)(local\.properties|.*\.(jks|keystore|p12|pem)|.*\.env)$' >/dev/null; then
  echo "Tracked local configuration or signing material is not permitted in a Release Candidate checkout." >&2
  exit 1
fi

candidate_id="${version_name}-rc-${short_commit}"
package_dir="$output_root_absolute/$candidate_id"
rm -rf "$package_dir"
mkdir -p "$package_dir"

(
  cd "$repo_root"
  ./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain \
    | tee "$package_dir/build-quality.log"
  ./gradlew testDebugUnitTest --rerun-tasks --no-daemon --console=plain \
    | tee -a "$package_dir/build-quality.log"
)

apk_debug="$repo_root/app/build/outputs/apk/debug/app-debug.apk"
apk_release="$repo_root/app/build/outputs/apk/release/app-release-unsigned.apk"
mapping="$repo_root/app/build/outputs/mapping/release/mapping.txt"
test_report="$repo_root/app/build/reports/tests/testDebugUnitTest/index.html"
lint_report="$repo_root/app/build/reports/lint-results-debug.html"
for artifact in "$apk_debug" "$apk_release" "$mapping" "$test_report" "$lint_report"; do
  [[ -f "$artifact" ]] || { echo "Expected build artifact was not produced: $artifact" >&2; exit 1; }
done

build_tools_version="${IVAI_BUILD_TOOLS_VERSION:-37.0.0}"
apksigner="$ANDROID_HOME/build-tools/$build_tools_version/apksigner"
[[ -x "$apksigner" ]] || { echo "Expected Android build-tools apksigner was not found: $apksigner (set IVAI_BUILD_TOOLS_VERSION only for a compatible installed build-tools version)." >&2; exit 1; }
if "$apksigner" verify --verbose "$apk_release" >/dev/null 2>&1; then
  echo "Release Candidate release APK is signed; this local unsigned-candidate helper refuses signed artifacts." >&2
  exit 1
fi
printf 'PASS: release APK is unsigned (apksigner verify rejected missing signature)\n' > "$package_dir/signing-state.txt"

xml_list="$package_dir/test-result-files.txt"
find "$repo_root/app/build/test-results/testDebugUnitTest" -name 'TEST-*.xml' -print > "$xml_list"
[[ -s "$xml_list" ]] || { echo "Unit-test XML results were not produced." >&2; exit 1; }
sum_attribute() {
  local attribute="$1"
  xargs -a "$xml_list" grep -h '<testsuite' \
    | sed -n "s/.*$attribute=\"\([0-9][0-9]*\)\".*/\1/p" \
    | awk '{sum += $1} END {print sum + 0}'
}
tests="$(sum_attribute tests)"
failures="$(sum_attribute failures)"
errors="$(sum_attribute errors)"
skipped="$(sum_attribute skipped)"
[[ "$failures" -eq 0 && "$errors" -eq 0 && "$skipped" -eq 0 ]] || {
  echo "Unit-test result is not clean: tests=$tests failures=$failures errors=$errors skipped=$skipped" >&2
  exit 1
}
[[ "$tests" -eq 158 ]] || {
  echo "Unexpected unit-test baseline: expected 158, found $tests." >&2
  exit 1
}
rm -f "$xml_list"

scan_report="$package_dir/security-invariant-scan.txt"
scan_must_be_empty() {
  local label="$1"
  local pattern="$2"
  if grep -RIn --exclude-dir=build -E "$pattern" "$repo_root/app/src/main"; then
    echo "Invariant scan failed: $label" >&2
    return 1
  fi
  printf 'PASS: %s\n' "$label"
}
{
  scan_must_be_empty "no hardcoded credential pattern" 'AIza[A-Za-z0-9_-]{35}|sk-[A-Za-z0-9]{20,}|api_key[[:space:]]*=[[:space:]]*"[^"]'
  scan_must_be_empty "no cleartext/trust bypass" 'usesCleartextTraffic.*true|trustAll|X509TrustManager'
  scan_must_be_empty "no prohibited execution" 'Runtime\.exec|ProcessBuilder|AccessibilityService|WorkManager'
  scan_must_be_empty "no implicit provider selection" 'defaultProvider[[:space:]]*=|autoSelect|implicitProvider'
  rtl_files="$(find "$repo_root/app/src/main/java" -type f \( -name MainActivity.kt -o -name Theme.kt \) -print)"
  if [[ -n "$rtl_files" ]] && printf '%s\n' "$rtl_files" | xargs grep -n 'LocalLayoutDirection provides LayoutDirection.Ltr'; then
    echo "Invariant scan failed: global forced-LTR shell override" >&2
    exit 1
  fi
  printf 'PASS: no global forced-LTR shell override\n'
} > "$scan_report"

source_state="$package_dir/source-state.txt"
if [[ -n "$(git -C "$repo_root" status --porcelain)" ]]; then
  echo "Worktree became dirty while preparing the Release Candidate." >&2
  exit 1
fi
printf 'CLEAN\n' > "$source_state"
printf 'PASS: git fsck --full --no-reflogs\nPASS: no tracked local/signing secret material\n' > "$package_dir/repository-integrity.txt"

release_name="IVAI-${candidate_id}-unsigned.apk"
cp "$apk_debug" "$package_dir/IVAI-${candidate_id}-debug.apk"
cp "$apk_release" "$package_dir/$release_name"
cp "$mapping" "$package_dir/mapping.txt"
cp "$test_report" "$package_dir/unit-test-report.html"
cp "$lint_report" "$package_dir/lint-report.html"

cat > "$package_dir/RELEASE_CANDIDATE_MANIFEST.txt" <<EOF
IVAI local Release Candidate manifest

Format: ivai-local-release-candidate/v1
Prepared at (UTC): $prepared_at_utc
Candidate ID: $candidate_id
Commit: $commit
Short commit: $short_commit
Version name: $version_name
Build type: minified unsigned release candidate
Release status: local preparation only; not signed, tagged, uploaded, public, or stable-approved
Quality commands: ./gradlew clean assembleDebug assembleRelease testDebugUnitTest lintDebug --no-daemon --console=plain; ./gradlew testDebugUnitTest --rerun-tasks --no-daemon --console=plain
Unit tests: $tests total, $failures failures, $errors errors, $skipped skipped

Artifact inventory:
- $release_name
- IVAI-${candidate_id}-debug.apk
- mapping.txt
- unit-test-report.html
- lint-report.html
- build-quality.log
- security-invariant-scan.txt
- repository-integrity.txt
- source-state.txt
- signing-state.txt

Release boundary:
This package is deterministic Release Candidate preparation only. It does not replace physical-device evidence, participant/heuristic evidence, owner-controlled signing, SHA-256 for a signed artifact, annotated source tag, reviewed release notes, owner approval, or public release authorization.
EOF

cat > "$package_dir/README.txt" <<EOF
IVAI local Release Candidate preparation package

Candidate: $candidate_id
Commit: $commit
Status: unsigned internal evidence package; not a public/stable download

Use:
- Verify the package with scripts/verify_release_candidate_package.sh before an internal review.
- Keep this package outside the repository and do not upload or publish either APK.
- Treat the release APK as unsigned deterministic build evidence only; do not distribute it as an Alpha or stable binary.

Public/stable release remains blocked until real Phase 7.5 participant/device evidence, owner-controlled signed-artifact provenance, approved checksum, annotated tag, reviewed release notes, and owner approval exist for one exact candidate commit.
EOF

(
  cd "$package_dir"
  sha256sum \
    "$release_name" \
    "IVAI-${candidate_id}-debug.apk" \
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

"$repo_root/scripts/verify_release_candidate_package.sh" "$package_dir"
printf 'Prepared local unsigned Release Candidate package: %s\n' "$package_dir"
