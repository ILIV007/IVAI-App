#!/usr/bin/env bash
# Verifies the adaptive launcher icon resource contract without a physical launcher.
# minSdk 29 guarantees that adaptive launcher resources are always selected.
# The foreground must remain a symbol-only vector: full compositions, backgrounds,
# glows, and bitmap wrappers belong to neither the foreground nor themed icon mask.
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RES_DIR="$ROOT_DIR/app/src/main/res"
FOREGROUND_XML="$RES_DIR/drawable/ivai_launcher_foreground.xml"
MONOCHROME_XML="$RES_DIR/drawable/ivai_launcher_monochrome.xml"
BACKGROUND_XML="$RES_DIR/drawable/ivai_brand_icon_background.xml"

for resource in "$FOREGROUND_XML" "$MONOCHROME_XML" "$BACKGROUND_XML"; do
    if ! test -f "$resource"; then
        echo "FAIL: required launcher layer is missing: ${resource#$ROOT_DIR/}" >&2
        exit 1
    fi
done

for vector in "$FOREGROUND_XML" "$MONOCHROME_XML"; do
    if ! grep -q '<vector' "$vector" || ! grep -q '<path' "$vector"; then
        echo "FAIL: ${vector#$ROOT_DIR/} must be a vector with a visible path" >&2
        exit 1
    fi
    if grep -q '<bitmap' "$vector"; then
        echo "FAIL: ${vector#$ROOT_DIR/} must be symbol-only and cannot wrap a bitmap composition" >&2
        exit 1
    fi
    if ! grep -q 'android:scaleX="0.70"' "$vector" || ! grep -q 'android:scaleY="0.70"' "$vector" || \
        ! grep -q 'android:pivotX="59.5"' "$vector" || ! grep -q 'android:pivotY="54"' "$vector" || \
        ! grep -q 'android:translateX="-5.5"' "$vector"; then
        echo "FAIL: ${vector#$ROOT_DIR/} must keep its VA mark optically centred within the adaptive safe zone" >&2
        exit 1
    fi
done

if ! grep -q '#101432' "$BACKGROUND_XML"; then
    echo "FAIL: launcher background must match the reviewed IVAI splash/system chrome color" >&2
    exit 1
fi

for icon in ic_launcher.xml ic_launcher_round.xml; do
    adaptive="$RES_DIR/mipmap-anydpi/$icon"
    if ! test -f "$adaptive" || ! grep -q '<adaptive-icon' "$adaptive"; then
        echo "FAIL: unqualified $icon must be a valid adaptive icon resource" >&2
        exit 1
    fi
    if ! grep -q '@drawable/ivai_brand_icon_background' "$adaptive"; then
        echo "FAIL: $icon must retain the independent adaptive background layer" >&2
        exit 1
    fi
    if ! grep -q '@drawable/ivai_launcher_foreground' "$adaptive"; then
        echo "FAIL: $icon must use the dedicated symbol-only foreground vector" >&2
        exit 1
    fi
    if ! grep -q '@drawable/ivai_launcher_monochrome' "$adaptive"; then
        echo "FAIL: $icon must retain the matching monochrome launcher layer" >&2
        exit 1
    fi
done

for retired in \
    "$RES_DIR/drawable/ivai_brand_icon_foreground.xml" \
    "$RES_DIR/drawable-nodpi/ivai_launcher_foreground_safe.png"; do
    if test -e "$retired"; then
        echo "FAIL: retired composite foreground must not coexist with the symbol-only vector" >&2
        exit 1
    fi
done

if find "$RES_DIR" -type f -path "$RES_DIR/mipmap-*/*" -name 'ic_launcher*.webp' -print -quit | grep -q .; then
    echo "FAIL: legacy launcher bitmap fallbacks must not coexist with minSdk 29 adaptive resources" >&2
    exit 1
fi

printf '%s\n' 'PASS: launcher icon uses reviewed symbol-only adaptive vector layers'
