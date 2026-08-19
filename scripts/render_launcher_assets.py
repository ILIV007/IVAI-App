#!/usr/bin/env python3
"""Render safe-zone launcher assets from the approved IVAI reference composition."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
REFERENCE = ROOT / "scripts" / "assets" / "ivai_brand_reference.png"
SAFE_FOREGROUND = RES / "drawable-nodpi" / "ivai_launcher_foreground_safe.png"
BACKGROUND_RGB = (0x10, 0x18, 0x32)
SAFE_CANVAS_SIZE = 1200
REFERENCE_INSET = 200
def main() -> None:
    reference = Image.open(REFERENCE).convert("RGB")
    if reference.size != (800, 800):
        raise ValueError(f"Expected approved reference to be 800×800, got {reference.size}")

    safe = Image.new("RGB", (SAFE_CANVAS_SIZE, SAFE_CANVAS_SIZE), BACKGROUND_RGB)
    safe.paste(reference, (REFERENCE_INSET, REFERENCE_INSET))
    safe.save(SAFE_FOREGROUND, "PNG", optimize=True)


if __name__ == "__main__":
    main()
