#!/usr/bin/env bash
# Guards Phase 8.0 as architecture-only preparation until the documented Phase 7.5
# and first-Alpha prerequisites permit a future, focused runtime subphase.
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

matrix="docs/PHASE8_0_CAPABILITY_CONSENT_MATRIX.md"
architecture="docs/SKILLS_MCP_FUTURE_ARCHITECTURE.md"
blueprint="docs/SKILLS_MCP_UI_BLUEPRINT.md"
roadmap="docs/ROADMAP.md"
threat_model="docs/PHASE8_0_SKILLS_MCP_THREAT_MODEL.md"
prototype_spec="docs/PHASE8_0_PROTOTYPE_REVIEW_SPECIFICATION.md"

for required_file in "$matrix" "$architecture" "$blueprint" "$roadmap" "$threat_model" "$prototype_spec"; do
  [[ -f "$required_file" ]] || {
    echo "Phase 8.0 guard failed: missing $required_file" >&2
    exit 1
  }
done

require_text() {
  local file="$1"
  local text="$2"
  grep -Fq -- "$text" "$file" || {
    echo "Phase 8.0 guard failed: missing required text in $file: $text" >&2
    exit 1
  }
}

require_text "$matrix" "Architecture-only preparation"
require_text "$matrix" "Phase 7.5 participant/device evidence and the first signed Alpha release gates are closed"
require_text "$matrix" "Draft"
require_text "$matrix" "Declared"
require_text "$matrix" "Reviewed"
require_text "$matrix" "Scoped"
require_text "$matrix" "Invocation pending"
require_text "$matrix" "Disabled / revoked"
require_text "$matrix" "Profile review"
require_text "$matrix" "Capability allowlist"
require_text "$matrix" "Per-invocation consent"
require_text "$matrix" "No request, no fallback, no replay after restart."
require_text "$matrix" "A copy of a token, prompt, file, raw argument, resource value, server response, or telemetry event."
require_text "$threat_model" "Planning-only threat model"
require_text "$threat_model" "TM-01"
require_text "$threat_model" "TM-12"
require_text "$threat_model" "A Skill description claims it can choose a stronger Provider"
require_text "$threat_model" "A future request must introduce a named phase"
require_text "$prototype_spec" "Planning-only prototype specification"
require_text "$prototype_spec" "PR-01"
require_text "$prototype_spec" "PR-12"
require_text "$prototype_spec" "This Skill grants no new authority and does not run by itself"
require_text "$prototype_spec" "A static mock may clarify a future decision"
require_text "$architecture" "PHASE8_0_CAPABILITY_CONSENT_MATRIX.md"
require_text "$architecture" "PHASE8_0_SKILLS_MCP_THREAT_MODEL.md"
require_text "$architecture" "PHASE8_0_PROTOTYPE_REVIEW_SPECIFICATION.md"
require_text "$blueprint" "PHASE8_0_CAPABILITY_CONSENT_MATRIX.md"
require_text "$blueprint" "PHASE8_0_SKILLS_MCP_THREAT_MODEL.md"
require_text "$blueprint" "PHASE8_0_PROTOTYPE_REVIEW_SPECIFICATION.md"
require_text "$roadmap" "PHASE8_0_CAPABILITY_CONSENT_MATRIX.md"
require_text "$roadmap" "PHASE8_0_SKILLS_MCP_THREAT_MODEL.md"
require_text "$roadmap" "PHASE8_0_PROTOTYPE_REVIEW_SPECIFICATION.md"

if git grep -nI -E -i '\b(skill|mcp)\b' -- app/src/main; then
  echo "Phase 8.0 guard failed: runtime Skills/MCP code exists before an approved focused subphase." >&2
  exit 1
fi

printf '%s\n' 'PASS: Phase 8.0 consent matrix, threat model, prototype specification, and cross-links are present'
printf '%s\n' 'PASS: no production Skills/MCP runtime implementation exists'
