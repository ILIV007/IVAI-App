#!/usr/bin/env bash
# Verifies the launcher icon resource contract without a physical launcher.
# The approved asset checksums are intentional: regenerate with
# scripts/render_launcher_assets.py only after a reviewed brand-asset decision,
# then update this contract in the same focused PR.
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
882d6ec24d2b6997fdc9016990e1258b813198921c06ccedc269ff37c893d259  app/src/main/res/mipmap-mdpi/ic_launcher.webp
c28f6a378e1857824741d04dfe18589ec81639061700c3c065d8858ed412a3b0  app/src/main/res/mipmap-mdpi/ic_launcher_round.webp
0f2948ab026c815fb581243f53f5db7dd7078783ac6629c80409a4ff1ce12a43  app/src/main/res/mipmap-hdpi/ic_launcher.webp
7320c09442cf614b0d068ee62b7156248bf6b8592471c89f5cb812413325a205  app/src/main/res/mipmap-hdpi/ic_launcher_round.webp
5b125ad878f19de6c0bfd8155f5d75e6b2587d5792ac12fe0ab161f087597378  app/src/main/res/mipmap-xhdpi/ic_launcher.webp
af74a5d7affd11866983edad0777946b3862e14cebc3024414374641d44ceee6  app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp
880f976b8e323a418ab565d257a32ca99a9568442fd813084fa75032058608db  app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
4d92b640e0450da1e359abbe4b3abe31524e5c5fe9140cc34053bcd0f4d9bb5d  app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp
6421d943596bfa197358e66e6d1e5d3d133b675ea5a6c9c4dcd888746b6d351e  app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
b41beb936cec24386615cd6cd9417d52f3282aec9114805bb6b4c08a0ff9b859  app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
EOF

for density in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
    square="$RES_DIR/mipmap-$density/ic_launcher.webp"
    round="$RES_DIR/mipmap-$density/ic_launcher_round.webp"
    if cmp -s "$square" "$round"; then
        echo "FAIL: square and round fallback icons must be distinct in $density" >&2
        exit 1
    fi
done

printf '%s\n' 'PASS: launcher icon safe-zone and fallback assets are valid'
