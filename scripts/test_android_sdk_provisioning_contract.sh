#!/usr/bin/env bash
# Regression checks for the compileSdk / CI SDK / Release Candidate build-tools contract.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
contract="$repo_root/scripts/check_android_sdk_provisioning_contract.sh"

[[ "$("$contract" --print-platform-package)" == "platforms;android-37.1" ]] || {
  echo "SDK contract did not report the expected API 37.1 platform package." >&2
  exit 1
}
[[ "$("$contract" --print-build-tools-version)" == "37.0.0" ]] || {
  echo "SDK contract did not report the expected build-tools version." >&2
  exit 1
}
"$contract" | grep -Fxq 'PASS: compileSdk API 37.1, CI platforms;android-37.1, and build-tools 37.0.0 are aligned.'

if "$contract" --unsupported-option >/dev/null 2>&1; then
  echo "SDK contract accepted an unsupported option." >&2
  exit 1
fi

printf 'Android SDK provisioning contract regression checks passed.\n'
