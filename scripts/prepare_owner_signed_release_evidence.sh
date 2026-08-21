#!/usr/bin/env bash
# Produces local owner-controlled signing evidence from one verified unsigned RC package.
# It never generates a key, reads a secret into output, tags source, uploads an artifact, or creates a release.
set -Eeuo pipefail
umask 077

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
rc_verifier="$repo_root/scripts/verify_release_candidate_package.sh"
sdk_contract="$repo_root/scripts/check_android_sdk_provisioning_contract.sh"

usage() {
  cat >&2 <<'EOF'
Usage:
  IVAI_SIGNING_KEYSTORE=/absolute/path/owner-release.jks \
  IVAI_SIGNING_KEY_ALIAS=owner-release \
  IVAI_KEYSTORE_PASSWORD_FILE=/absolute/path/keystore-password.txt \
  IVAI_KEY_PASSWORD_FILE=/absolute/path/key-password.txt \
  scripts/prepare_owner_signed_release_evidence.sh \
    --rc-package /absolute/path/to/verified-unsigned-rc \
    --output-root /absolute/path/outside/repository \
    --confirm-external-alpha-gates-closed

The password files are read directly by apksigner and must remain outside the repository.
EOF
  exit 64
}

require_outside_repository() {
  local label="$1"
  local path="$2"
  local normalized
  normalized="$(realpath -m "$path")"
  case "$normalized" in
    "$repo_root"|"$repo_root"/*)
      echo "$label must stay outside the repository: $normalized" >&2
      exit 1
      ;;
  esac
}

rc_package=""
output_root=""
confirmed=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --rc-package)
      [[ $# -ge 2 ]] || usage
      rc_package="$2"
      shift 2
      ;;
    --output-root)
      [[ $# -ge 2 ]] || usage
      output_root="$2"
      shift 2
      ;;
    --confirm-external-alpha-gates-closed)
      confirmed=true
      shift
      ;;
    --help|-h) usage ;;
    *) usage ;;
  esac
done

[[ "$confirmed" == true ]] || {
  echo "Refusing to sign without --confirm-external-alpha-gates-closed." >&2
  exit 1
}
[[ -n "$rc_package" && -n "$output_root" ]] || usage
[[ -d "$rc_package" ]] || { echo "RC package directory does not exist: $rc_package" >&2; exit 1; }
[[ -n "${JAVA_HOME:-}" ]] || { echo "Set JAVA_HOME to JDK 21 before preparing signing evidence." >&2; exit 1; }
[[ -n "${ANDROID_HOME:-}" ]] || { echo "Set ANDROID_HOME before preparing signing evidence." >&2; exit 1; }

require_outside_repository "RC package" "$rc_package"
require_outside_repository "Signing evidence output" "$output_root"

: "${IVAI_SIGNING_KEYSTORE:?Set IVAI_SIGNING_KEYSTORE to an owner-controlled keystore outside the repository.}"
: "${IVAI_SIGNING_KEY_ALIAS:?Set IVAI_SIGNING_KEY_ALIAS to the owner-controlled signing alias.}"
: "${IVAI_KEYSTORE_PASSWORD_FILE:?Set IVAI_KEYSTORE_PASSWORD_FILE to a password file outside the repository.}"
: "${IVAI_KEY_PASSWORD_FILE:?Set IVAI_KEY_PASSWORD_FILE to a password file outside the repository.}"

for sensitive_path in "$IVAI_SIGNING_KEYSTORE" "$IVAI_KEYSTORE_PASSWORD_FILE" "$IVAI_KEY_PASSWORD_FILE"; do
  [[ -f "$sensitive_path" && -r "$sensitive_path" ]] || {
    echo "Owner-controlled signing input is missing or unreadable." >&2
    exit 1
  }
  require_outside_repository "Owner-controlled signing input" "$sensitive_path"
done

rc_package="$(realpath "$rc_package")"
output_root="$(realpath -m "$output_root")"
"$rc_verifier" "$rc_package"

manifest="$rc_package/RELEASE_CANDIDATE_MANIFEST.txt"
commit="$(sed -n 's/^Commit: //p' "$manifest")"
short_commit="$(sed -n 's/^Short commit: //p' "$manifest")"
candidate_id="$(sed -n 's/^Candidate ID: //p' "$manifest")"
version_name="$(sed -n 's/^Version name: //p' "$manifest")"
[[ "$commit" =~ ^[0-9a-f]{40}$ ]] || { echo "Verified RC manifest has an invalid commit." >&2; exit 1; }
[[ "$candidate_id" == "${version_name}-rc-${short_commit}" ]] || { echo "Verified RC manifest has an inconsistent candidate identity." >&2; exit 1; }
[[ "$(git -C "$repo_root" rev-parse HEAD)" == "$commit" ]] || {
  echo "Checkout HEAD does not match verified RC commit; sign only from the exact candidate checkout." >&2
  exit 1
}
[[ -z "$(git -C "$repo_root" status --porcelain)" ]] || {
  echo "Refusing to sign from a dirty worktree." >&2
  exit 1
}

build_tools_version="$("$sdk_contract" --print-build-tools-version)"
apksigner="$ANDROID_HOME/build-tools/$build_tools_version/apksigner"
[[ -x "$apksigner" ]] || {
  echo "Expected apksigner for the repository SDK contract was not found." >&2
  exit 1
}

unsigned_apk="$rc_package/IVAI-${candidate_id}-unsigned.apk"
[[ -s "$unsigned_apk" ]] || { echo "Verified RC unsigned APK is missing." >&2; exit 1; }

package_dir="$output_root/${candidate_id}-owner-signed-evidence"
rm -rf "$package_dir"
mkdir -p "$package_dir"
success=false
cleanup_on_failure() {
  [[ "$success" == true ]] || rm -rf "$package_dir"
}
trap cleanup_on_failure EXIT

signed_apk="$package_dir/IVAI-${candidate_id}-signed.apk"
"$apksigner" sign \
  --ks "$IVAI_SIGNING_KEYSTORE" \
  --ks-key-alias "$IVAI_SIGNING_KEY_ALIAS" \
  --ks-pass "file:$IVAI_KEYSTORE_PASSWORD_FILE" \
  --key-pass "file:$IVAI_KEY_PASSWORD_FILE" \
  --out "$signed_apk" \
  "$unsigned_apk"

"$apksigner" verify --verbose "$signed_apk" > "$package_dir/apksigner-verify.txt"
"$apksigner" verify --verbose --print-certs "$signed_apk" > "$package_dir/apksigner-certificate.txt"
(
  cd "$package_dir"
  sha256sum "$(basename "$signed_apk")" > SHA256SUMS.txt
)
unsigned_sha256="$(sha256sum "$unsigned_apk" | awk '{print $1}')"
signed_sha256="$(sha256sum "$signed_apk" | awk '{print $1}')"
prepared_at_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

cat > "$package_dir/OWNER_SIGNED_EVIDENCE_MANIFEST.txt" <<EOF
IVAI owner-controlled signed release evidence manifest
Format: ivai-owner-signed-release-evidence/v1
Prepared at (UTC): $prepared_at_utc
Candidate ID: $candidate_id
Commit: $commit
Short commit: $short_commit
Version name: $version_name
SDK contract build-tools: $build_tools_version
Source unsigned APK SHA-256: $unsigned_sha256
Signed APK: $(basename "$signed_apk")
Signed APK SHA-256: $signed_sha256
External-gate acknowledgement: supplied by the signing caller; not independently verified by this helper
Publication status: local owner-controlled evidence only; no tag, upload, GitHub Release, or public approval was created

Artifact inventory:
- $(basename "$signed_apk")
- SHA256SUMS.txt
- apksigner-verify.txt
- apksigner-certificate.txt
EOF

cat > "$package_dir/README.txt" <<'EOF'
IVAI owner-controlled signed release evidence

This directory binds one signed APK to one verified unsigned Release Candidate package and source commit. It is local evidence only. This helper never generates or copies a signing key, stores a password, creates a source tag, calls GitHub, uploads an artifact, or approves an Alpha release.

Before any publication, the release owner must independently confirm every field/device/usability/heuristic gate, review this package, create an annotated tag on the approved commit, attach only the signed APK and SHA256SUMS.txt to the reviewed GitHub Release, and independently download/hash-check the public artifact.
EOF

success=true
trap - EXIT
printf 'Prepared local owner-controlled signed release evidence: %s\n' "$package_dir"
