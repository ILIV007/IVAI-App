#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOC="$ROOT_DIR/docs/PHASE7_R8_PROVIDER_MODEL_TEST_READINESS.md"
SOURCE_ROOT="$ROOT_DIR/app/src/main"

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  exit 1
}

[ -f "$DOC" ] || fail "R8 Provider/Model test readiness record is missing"

required_phrases=(
  'Status: planning/readiness only.'
  'Phase 7.5 research/device evidence'
  'first signed Alpha release gates'
  'Run one test'
  'Stop test'
  'no retry, fallback, discovery, or background continuation'
  'must never contain API keys'
  'Model discovery and endpoint probing'
)

for phrase in "${required_phrases[@]}"; do
  grep -Fq "$phrase" "$DOC" || fail "R8 readiness record is missing required contract: $phrase"
done

forbidden_runtime_symbols=(
  'fun testProviderConnection'
  'fun testModelConnection'
  'class ProviderTestRunner'
  'class ProviderModelTestRunner'
  'button_test_provider'
  'button_test_model'
  'button_confirm_provider_test'
  'button_confirm_model_test'
)

for symbol in "${forbidden_runtime_symbols[@]}"; do
  if grep -RInF --exclude-dir=build "$symbol" "$SOURCE_ROOT" >/dev/null; then
    fail "R8 runtime symbol is present before Phase 7.5/Alpha gate: $symbol"
  fi
done

printf '%s\n' 'PASS: R8 Provider/Model test readiness is gated and no runtime implementation is present'
