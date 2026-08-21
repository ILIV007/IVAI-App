#!/usr/bin/env bash
# Regression checks for the owner-signed evidence helper without generating a key or signature.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
helper="$repo_root/scripts/prepare_owner_signed_release_evidence.sh"

if "$helper" --help >/tmp/ivai-owner-signing-help.stdout 2>/tmp/ivai-owner-signing-help.stderr; then
  echo "Owner-signing helper accepted --help as a signing operation." >&2
  exit 1
fi
grep -Fq 'Usage:' /tmp/ivai-owner-signing-help.stderr

if "$helper" --rc-package /tmp/nonexistent-rc --output-root /tmp/nonexistent-output >/tmp/ivai-owner-signing-no-confirm.stdout 2>/tmp/ivai-owner-signing-no-confirm.stderr; then
  echo "Owner-signing helper proceeded without external-gate confirmation." >&2
  exit 1
fi
grep -Fxq 'Refusing to sign without --confirm-external-alpha-gates-closed.' /tmp/ivai-owner-signing-no-confirm.stderr

if grep -nE '\b(gh|curl|wget)\b|git[[:space:]]+tag|git[[:space:]]+push' "$helper"; then
  echo "Owner-signing helper contains publication or source-tag automation." >&2
  exit 1
fi
if grep -nE 'keytool|storepass|keypass' "$helper"; then
  echo "Owner-signing helper may generate or expose signing secrets." >&2
  exit 1
fi
grep -Fq -- '--ks-pass "file:$IVAI_KEYSTORE_PASSWORD_FILE"' "$helper" || {
  echo "Owner-signing helper does not use the required file-based keystore password handoff." >&2
  exit 1
}
grep -Fq -- '--key-pass "file:$IVAI_KEY_PASSWORD_FILE"' "$helper" || {
  echo "Owner-signing helper does not use the required file-based key password handoff." >&2
  exit 1
}

echo 'Owner-signed release evidence helper regression checks passed.'
