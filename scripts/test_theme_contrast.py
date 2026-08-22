#!/usr/bin/env python3
"""Enforce WCAG AA contrast for IVAI semantic text/action colour pairs."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
COLOR_FILE = ROOT / "app/src/main/java/dev/iliv007/ivai/ui/theme/Color.kt"
SOURCE = COLOR_FILE.read_text(encoding="utf-8")


def color_value(name: str) -> tuple[int, int, int]:
    match = re.search(rf"val\s+{re.escape(name)}\s*=\s*Color\(0xFF([0-9A-Fa-f]{{6}})\)", SOURCE)
    if not match:
        raise ValueError(f"Missing opaque hex Color definition for {name}")
    value = match.group(1)
    return tuple(int(value[index : index + 2], 16) for index in (0, 2, 4))


def luminance(rgb: tuple[int, int, int]) -> float:
    def channel(value: int) -> float:
        normalized = value / 255.0
        return normalized / 12.92 if normalized <= 0.04045 else ((normalized + 0.055) / 1.055) ** 2.4

    red, green, blue = (channel(value) for value in rgb)
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue


def contrast(foreground: str, background: str) -> float:
    first = luminance(color_value(foreground))
    second = luminance(color_value(background))
    return (max(first, second) + 0.05) / (min(first, second) + 0.05)


PAIRS = {
    "light": (
        ("LightActionPrimary", "LightSurface"),
        ("LightActionSecondary", "LightSurface"),
        ("LightActionTertiary", "LightSurface"),
        ("IvaiErrorLight", "LightSurface"),
        ("LightOnActionPrimaryContainer", "LightActionPrimaryContainer"),
        ("LightOnActionSecondaryContainer", "LightActionSecondaryContainer"),
        ("LightOnActionTertiaryContainer", "LightActionTertiaryContainer"),
        ("LightTextPrimary", "LightBackground"),
        ("LightTextPrimary", "LightSurface"),
        ("LightTextPrimary", "LightSurfaceVariant"),
        ("LightTextSecondary", "LightBackground"),
        ("LightTextSecondary", "LightSurface"),
        ("LightTextSecondary", "LightSurfaceVariant"),
        ("LightTextMuted", "LightBackground"),
        ("LightTextMuted", "LightSurface"),
        ("IvaiSuccessLight", "LightSurface"),
        ("IvaiWarningLight", "LightSurface"),
    ),
    "dark": (
        ("DarkActionPrimary", "IvaiBackground"),
        ("DarkActionSecondary", "IvaiBackground"),
        ("DarkActionTertiary", "IvaiBackground"),
        ("IvaiError", "IvaiBackground"),
        ("DarkOnActionPrimaryContainer", "DarkActionPrimaryContainer"),
        ("DarkOnActionSecondaryContainer", "DarkActionSecondaryContainer"),
        ("DarkOnActionTertiaryContainer", "DarkActionTertiaryContainer"),
        ("TextPrimary", "IvaiBackground"),
        ("TextPrimary", "IvaiSurface"),
        ("TextPrimary", "IvaiSurfaceVariant"),
        ("TextSecondary", "IvaiBackground"),
        ("TextSecondary", "IvaiSurface"),
        ("TextSecondary", "IvaiSurfaceVariant"),
        ("TextMuted", "IvaiBackground"),
        ("TextMuted", "IvaiSurface"),
        ("IvaiSuccess", "IvaiBackground"),
        ("IvaiWarning", "IvaiBackground"),
    ),
}

failures: list[str] = []
for theme, pairs in PAIRS.items():
    for foreground, background in pairs:
        ratio = contrast(foreground, background)
        print(f"{theme:5} {foreground:26} on {background:26} {ratio:.2f}:1")
        if ratio < 4.5:
            failures.append(f"{theme}: {foreground} on {background} is {ratio:.2f}:1 (< 4.5:1)")

if failures:
    print("\nFAIL: IVAI semantic contrast regression", file=sys.stderr)
    print("\n".join(failures), file=sys.stderr)
    raise SystemExit(1)

print("\nPASS: all audited IVAI semantic text/action pairs meet 4.5:1")
