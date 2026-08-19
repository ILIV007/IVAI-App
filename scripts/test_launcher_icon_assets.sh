#!/usr/bin/env bash
# Verifies the launcher icon resource contract without a physical launcher.
# The approved safe-foreground checksum is intentional: regenerate with
# scripts/render_launcher_assets.py only after a reviewed brand-asset decision,
# then update this contract in the same focused PR. minSdk 29 guarantees that
# adaptive launcher resources are always selected, so legacy bitmap fallbacks
# must not coexist with the unqualified adaptive XML resources.
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RES_DIR="$ROOT_DIR/app/src/main/res"
FOREGROUND_XML="$RES_DIR/drawable/ivai_brand_icon_foreground.xml"
SAFE_FOREGROUND_IMAGE="$RES_DIR/drawable-nodpi/ivai_launcher_foreground_safe.png"

if ! grep -q '@drawable/ivai_launcher_foreground_safe' "$FOREGROUND_XML"; then
    echo "FAIL: adaptive foreground must reference ivai_launcher_foreground_safe" >&2
    exit 1
fi

if ! file "$SAFE_FOREGROUND_IMAGE" | grep -q 'PNG image data, 1200 x 1200'; then
    echo "FAIL: safe-zone foreground must be a 1200×1200 PNG" >&2
    exit 1
fi

cd "$ROOT_DIR"
sha256sum --check --status <<'EOF'
1d4bde91c2fbf6fc78b33f21f3607f60928db86ef1adf36434496d5e40298568  app/src/main/res/drawable-nodpi/ivai_launcher_foreground_safe.png
EOF

for icon in ic_launcher.xml ic_launcher_round.xml; do
    adaptive="$RES_DIR/mipmap-anydpi/$icon"
    if ! test -f "$adaptive" || ! grep -q '<adaptive-icon' "$adaptive"; then
        echo "FAIL: unqualified $icon must be a valid adaptive icon resource" >&2
        exit 1
    fi
    if ! grep -q '@drawable/ivai_launcher_monochrome' "$adaptive"; then
        echo "FAIL: $icon must retain the reviewed monochrome launcher layer" >&2
        exit 1
    fi
done

if find "$RES_DIR" -type f -path "$RES_DIR/mipmap-*/*" -name 'ic_launcher*.webp' -print -quit | grep -q .; then
    echo "FAIL: legacy launcher bitmap fallbacks must not coexist with minSdk 29 adaptive resources" >&2
    exit 1
fi

printf '%s\n' 'PASS: launcher icon safe-zone and adaptive-only resources are valid'
