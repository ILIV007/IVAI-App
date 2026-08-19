#!/usr/bin/env python3
"""Render safe-zone launcher assets from the approved IVAI reference composition."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
REFERENCE = ROOT / "scripts" / "assets" / "ivai_brand_reference.png"
SAFE_FOREGROUND = RES / "drawable-nodpi" / "ivai_launcher_foreground_safe.png"
BACKGROUND_RGB = (0x10, 0x18, 0x32)
SAFE_CANVAS_SIZE = 1200
REFERENCE_INSET = 200
DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def main() -> None:
    reference = Image.open(REFERENCE).convert("RGB")
    if reference.size != (800, 800):
        raise ValueError(f"Expected approved reference to be 800×800, got {reference.size}")

    safe = Image.new("RGB", (SAFE_CANVAS_SIZE, SAFE_CANVAS_SIZE), BACKGROUND_RGB)
    safe.paste(reference, (REFERENCE_INSET, REFERENCE_INSET))
    safe.save(SAFE_FOREGROUND, "PNG", optimize=True)

    for density, size in DENSITIES.items():
        density_dir = RES / density
        square = safe.resize((size, size), Image.Resampling.LANCZOS)
        square.save(density_dir / "ic_launcher.webp", "WEBP", lossless=True, method=6)

        round_icon = square.convert("RGBA")
        mask = Image.new("L", (size, size), 0)
        inset = max(1, round(size * 0.04))
        ImageDraw.Draw(mask).ellipse((inset, inset, size - inset - 1, size - inset - 1), fill=255)
        round_icon.putalpha(mask)
        round_icon.save(density_dir / "ic_launcher_round.webp", "WEBP", lossless=True, method=6)


if __name__ == "__main__":
    main()
