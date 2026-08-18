#!/usr/bin/env bash
# Guards IVAI's provider-neutral repository identity without removing active provider adapters
# or Android build dependencies that require vendor repository/plugin coordinates.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

require_absent() {
  local label="$1"
  local pattern="$2"
  if git grep -n -i -E "$pattern" -- .; then
    echo "Provider-neutral branding guard failed: $label" >&2
    exit 1
  fi
  printf 'PASS: %s\n' "$label"
}

[[ ! -e GEMINI.md ]] || {
  echo "Provider-neutral branding guard failed: legacy vendor-named development guide exists." >&2
  exit 1
}
printf 'PASS: legacy vendor-named development guide is absent\n'

require_absent "no AI Studio branding" 'Google[[:space:]]*AI[[:space:]]*Studio'
require_absent "no vendor display prefix" 'Google[[:space:]]+Gemini'
require_absent "no single-provider product identity" 'Gemini-[f]irst'
require_absent "no IDE template branding" 'Android[[:space:]]+Studio'
require_absent "no stale services passthrough" 'google[S]ervices\.missing\.passthrough'
require_absent "no server-side adapter metadata capability" 'MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_[A]PI'

metadata_capability="$(sed -n 's/.*"majorCapabilities"[[:space:]]*:[[:space:]]*\["\([^"]*\)"\].*/\1/p' metadata.json)"
[[ "$metadata_capability" == "MAJOR_CAPABILITY_LOCAL_BYOK_AGENT_HARNESS" ]] || {
  echo "Provider-neutral branding guard failed: metadata capability is not local BYOK Agent Harness." >&2
  exit 1
}
printf 'PASS: metadata advertises local BYOK Agent Harness\n'

[[ -f DEVELOPMENT_GUIDE.md ]] || {
  echo "Provider-neutral branding guard failed: generic development guide is missing." >&2
  exit 1
}
[[ -f app/src/main/java/dev/iliv007/ivai/provider/gemini/GeminiChatProvider.kt ]] || {
  echo "Provider-neutral branding guard failed: active proof adapter is unexpectedly absent." >&2
  exit 1
}
git grep -q 'ProviderAdapterRegistry' -- app/src/main/java/dev/iliv007/ivai || {
  echo "Provider-neutral branding guard failed: provider adapter registry is missing." >&2
  exit 1
}
printf 'PASS: generic guide and explicit provider adapter registry remain present\n'
