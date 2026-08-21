#!/usr/bin/env bash
# Verifies that Gradle compileSdk, CI SDK provisioning, and Release Candidate build-tools agree.
# This script has no network, signing, installation, or artifact-publication behavior.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
gradle_file="$repo_root/app/build.gradle.kts"
workflow_file="$repo_root/.github/workflows/android-quality.yml"

usage() {
  echo "Usage: $0 [--print-platform-package|--print-build-tools-version]" >&2
  exit 64
}

compile_sdk_release="$(sed -n 's/^[[:space:]]*compileSdk[[:space:]]*{[[:space:]]*version[[:space:]]*=[[:space:]]*release(\([0-9][0-9]*\)).*/\1/p' "$gradle_file")"
compile_sdk_minor="$(sed -n 's/.*minorApiLevel[[:space:]]*=[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$gradle_file")"

[[ "$compile_sdk_release" =~ ^[0-9]+$ ]] || {
  echo "Unable to determine compileSdk release API from app/build.gradle.kts." >&2
  exit 1
}
[[ "$compile_sdk_minor" =~ ^[0-9]+$ ]] || {
  echo "Unable to determine compileSdk minor API level from app/build.gradle.kts." >&2
  exit 1
}

platform_package="platforms;android-${compile_sdk_release}.${compile_sdk_minor}"
# Build-tools is intentionally pinned because the unsigned Release Candidate helper invokes apksigner.
build_tools_version="37.0.0"
build_tools_package="build-tools;${build_tools_version}"

if ! grep -Fq "\"$platform_package\"" "$workflow_file"; then
  echo "CI provisioning does not include required compileSdk package: $platform_package" >&2
  exit 1
fi
if ! grep -Fq "\"$build_tools_package\"" "$workflow_file"; then
  echo "CI provisioning does not include required Release Candidate build-tools package: $build_tools_package" >&2
  exit 1
fi

case "${1:-}" in
  "")
    printf 'PASS: compileSdk API %s.%s, CI %s, and build-tools %s are aligned.\n' \
      "$compile_sdk_release" "$compile_sdk_minor" "$platform_package" "$build_tools_version"
    ;;
  --print-platform-package)
    printf '%s\n' "$platform_package"
    ;;
  --print-build-tools-version)
    printf '%s\n' "$build_tools_version"
    ;;
  *) usage ;;
esac
