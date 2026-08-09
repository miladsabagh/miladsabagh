#!/usr/bin/env python3
"""تولید آیکون‌های PNG برنامه برای اندروید ۷ و ۸ (پیش از آیکون‌های تطبیقی).

اجرا: python3 tools/generate_launcher_icons.py
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw

BACKGROUND = (59, 47, 20, 255)
GOLD = (212, 175, 55, 255)
GOLD_LIGHT = (246, 220, 154, 255)
GEM = (233, 185, 73, 255)

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

SUPERSAMPLE = 8
RES_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "app", "src", "main", "res")


def draw_icon(size: int, round_shape: bool) -> Image.Image:
    canvas = size * SUPERSAMPLE
    image = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    if round_shape:
        draw.ellipse([0, 0, canvas - 1, canvas - 1], fill=BACKGROUND)
    else:
        radius = int(canvas * 0.22)
        draw.rounded_rectangle([0, 0, canvas - 1, canvas - 1], radius=radius, fill=BACKGROUND)

    # حلقه انگشتر
    ring_center = (canvas * 0.5, canvas * 0.62)
    ring_radius = canvas * 0.24
    ring_width = int(canvas * 0.075)
    draw.ellipse(
        [
            ring_center[0] - ring_radius,
            ring_center[1] - ring_radius,
            ring_center[0] + ring_radius,
            ring_center[1] + ring_radius,
        ],
        outline=GOLD,
        width=ring_width,
    )

    # نگین بالای حلقه
    gem_center_y = canvas * 0.28
    gem_half_width = canvas * 0.115
    gem_half_height = canvas * 0.105
    draw.polygon(
        [
            (canvas * 0.5, gem_center_y - gem_half_height),
            (canvas * 0.5 + gem_half_width, gem_center_y),
            (canvas * 0.5, gem_center_y + gem_half_height),
            (canvas * 0.5 - gem_half_width, gem_center_y),
        ],
        fill=GEM,
    )
    draw.polygon(
        [
            (canvas * 0.5, gem_center_y - gem_half_height),
            (canvas * 0.5 + gem_half_width, gem_center_y),
            (canvas * 0.5, gem_center_y),
        ],
        fill=GOLD_LIGHT,
    )

    return image.resize((size, size), Image.LANCZOS)


def main() -> None:
    for density, size in DENSITIES.items():
        directory = os.path.join(RES_DIR, f"mipmap-{density}")
        os.makedirs(directory, exist_ok=True)
        draw_icon(size, round_shape=False).save(os.path.join(directory, "ic_launcher.png"))
        draw_icon(size, round_shape=True).save(os.path.join(directory, "ic_launcher_round.png"))
        print(f"mipmap-{density}: {size}x{size}")


if __name__ == "__main__":
    main()
