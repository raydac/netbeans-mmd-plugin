#!/usr/bin/env python3
"""Pack Java MMD panel icons into a sprite for the browser viewer.

Reads emoticon names from mind-map-swing-panel .../miscicons/icon.lst
and extra icons from .../panel/icons/, writes mmd-icons.png + mmd-icons.json
next to this script.
"""

from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
REPO_SWING = HERE.parent / "mind-map-swing-panel" / "src" / "main" / "resources" / "com" / "igormaznitsa" / "mindmap" / "swing"
MISC = REPO_SWING / "miscicons"
PANEL_ICONS = REPO_SWING / "panel" / "icons"
TILE = 32
COLUMNS = 32

EXTRAS = (
    ("NOTE", "extra_note.png"),
    ("LINK", "extra_uri.png"),
    ("LINK_EMAIL", "extra_email.png"),
    ("FILE", "extra_file.png"),
    ("FILE_WARN", "extra_file_warn.png"),
    ("FILE_MMD", "extra_mmd.png"),
    ("FILE_MMD_WARN", "extra_mmd_warn.png"),
    ("FILE_PLANTUML", "extra_plantuml.png"),
    ("FILE_PLANTUML_WARN", "extra_plantuml_warn.png"),
    ("TOPIC", "extra_topic.png"),
)


def load_tile(path: Path) -> Image.Image:
    image = Image.open(path).convert("RGBA")
    if image.size != (TILE, TILE):
        canvas = Image.new("RGBA", (TILE, TILE), (0, 0, 0, 0))
        canvas.paste(image, (0, 0))
        return canvas
    return image


def icon_names() -> list[str]:
    names = []
    for line in (MISC / "icon.lst").read_text(encoding="utf-8").splitlines():
        name = line.strip()
        if name:
            names.append(name)
    return names


def main() -> None:
    slots: list[tuple[str, Image.Image]] = []
    extras = {}
    for index, (key, filename) in enumerate(EXTRAS):
        extras[key] = index
        slots.append((key, load_tile(PANEL_ICONS / filename)))

    emoticons = icon_names()
    missing = []
    for name in emoticons:
        png = MISC / ("%s.png" % name)
        if not png.is_file():
            missing.append(name)
            continue
        slots.append((name, load_tile(png)))

    if missing:
        raise SystemExit("Missing emoticon PNGs: %s" % ", ".join(missing))

    rows = int(math.ceil(len(slots) / COLUMNS))
    sheet = Image.new("RGBA", (COLUMNS * TILE, rows * TILE), (0, 0, 0, 0))
    ids = []
    for index, (name, tile) in enumerate(slots):
        column = index % COLUMNS
        row = index // COLUMNS
        sheet.paste(tile, (column * TILE, row * TILE))
        ids.append(name)

    png_path = HERE / "mmd-icons.png"
    json_path = HERE / "mmd-icons.json"
    sheet.save(png_path, format="PNG", optimize=True)
    json_path.write_text(
        json.dumps(
            {
                "tile": TILE,
                "columns": COLUMNS,
                "width": sheet.size[0],
                "height": sheet.size[1],
                "extras": extras,
                "ids": ids,
            },
            indent=2,
            ensure_ascii=True,
        )
        + "\n",
        encoding="utf-8",
    )
    print("Wrote %s (%d icons, %dx%d, %d bytes)" % (png_path, len(slots), sheet.size[0], sheet.size[1], png_path.stat().st_size))
    print("Wrote %s" % json_path)


if __name__ == "__main__":
    main()
