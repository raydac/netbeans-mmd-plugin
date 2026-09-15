#!/usr/bin/env python3
"""Build a compact mmd-viewer.min.js with an optional embedded icon atlas.

Reads mmd-viewer.js and mmd-icons.json next to this script. By default the
atlas is compacted and inlined so the packed file does not fetch JSON.
mmd-icons.png is still loaded from beside the script.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
SOURCE = HERE / "mmd-viewer.js"
ATLAS = HERE / "mmd-icons.json"
OUTPUT = HERE / "mmd-viewer.min.js"
ATLAS_ASSIGN = "var EMBEDDED_ATLAS = null;"
IDENT_CHARS = set("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$")
IDENT_START = set("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$")
KEYWORD_BEFORE_REGEX = {
    "return",
    "throw",
    "case",
    "typeof",
    "void",
    "delete",
    "else",
    "do",
    "in",
    "instanceof",
    "new",
    "yield",
    "await",
}
REGEX_PUNCT = set("([{,;=!&|?:~^+-*%<>")
MULTI_PUNCT = (
    ">>>",
    "===",
    "!==",
    "<<=",
    ">>=",
    "**=",
    "&&=",
    "||=",
    "??=",
    "==",
    "!=",
    "<=",
    ">=",
    "<<",
    ">>",
    "&&",
    "||",
    "??",
    "++",
    "--",
    "+=",
    "-=",
    "*=",
    "/=",
    "%=",
    "&=",
    "|=",
    "^=",
    "=>",
    "**",
    "?.",
)


def compact_atlas(path: Path) -> str:
    data = json.loads(path.read_text(encoding="utf-8"))
    text = json.dumps(data, separators=(",", ":"), ensure_ascii=True)
    return text.replace("<", "\\u003c").replace(">", "\\u003e")


def inject_atlas(source: str, atlas_js: str | None) -> str:
    if ATLAS_ASSIGN not in source:
        raise SystemExit("Could not find %r in %s" % (ATLAS_ASSIGN, SOURCE.name))
    if atlas_js is None:
        return source
    return source.replace(ATLAS_ASSIGN, "var EMBEDDED_ATLAS = %s;" % atlas_js, 1)


def extract_license(source: str) -> tuple[str, str]:
    stripped = source.lstrip()
    if stripped.startswith("/*"):
        end = stripped.find("*/")
        if end >= 0:
            comment = stripped[: end + 2].strip()
            rest = stripped[end + 2 :]
            if "Copyright" in comment or "Apache" in comment:
                return comment + "\n", rest
    return "", source


def is_ident(ch: str) -> bool:
    return ch in IDENT_CHARS


def read_while(source: str, index: int, pred) -> tuple[str, int]:
    start = index
    while index < len(source) and pred(source[index]):
        index += 1
    return source[start:index], index


def read_string(source: str, index: int, quote: str) -> tuple[str, int]:
    chars = [quote]
    index += 1
    while index < len(source):
        ch = source[index]
        chars.append(ch)
        index += 1
        if ch == "\\" and index < len(source):
            chars.append(source[index])
            index += 1
            continue
        if ch == quote:
            return "".join(chars), index
    raise SystemExit("Unterminated string at %d" % (index,))


def read_template(source: str, index: int) -> tuple[str, int]:
    chars = ["`"]
    index += 1
    while index < len(source):
        ch = source[index]
        chars.append(ch)
        index += 1
        if ch == "\\" and index < len(source):
            chars.append(source[index])
            index += 1
            continue
        if ch == "`":
            return "".join(chars), index
        if ch == "$" and index < len(source) and source[index] == "{":
            chars.append("{")
            index += 1
            depth = 1
            while index < len(source) and depth:
                inner = source[index]
                if inner in "'\"":
                    piece, index = read_string(source, index, inner)
                    chars.append(piece)
                    continue
                if inner == "`":
                    piece, index = read_template(source, index)
                    chars.append(piece)
                    continue
                chars.append(inner)
                index += 1
                if inner == "{":
                    depth += 1
                elif inner == "}":
                    depth -= 1
    raise SystemExit("Unterminated template string")


def read_regex(source: str, index: int) -> tuple[str, int]:
    chars = ["/"]
    index += 1
    in_class = False
    while index < len(source):
        ch = source[index]
        chars.append(ch)
        index += 1
        if ch == "\\" and index < len(source):
            chars.append(source[index])
            index += 1
            continue
        if ch == "[" and not in_class:
            in_class = True
            continue
        if ch == "]" and in_class:
            in_class = False
            continue
        if ch == "/" and not in_class:
            flags, index = read_while(source, index, str.isalpha)
            chars.append(flags)
            return "".join(chars), index
    raise SystemExit("Unterminated regular expression")


def read_number(source: str, index: int) -> tuple[str, int]:
    start = index
    if source.startswith("0x", index) or source.startswith("0X", index):
        index += 2
        digits, index = read_while(source, index, lambda ch: ch in "0123456789abcdefABCDEF")
        return source[start:index], index
    if source[index] == ".":
        index += 1
        _, index = read_while(source, index, str.isdigit)
    else:
        _, index = read_while(source, index, str.isdigit)
        if index < len(source) and source[index] == ".":
            index += 1
            _, index = read_while(source, index, str.isdigit)
    if index < len(source) and source[index] in "eE":
        index += 1
        if index < len(source) and source[index] in "+-":
            index += 1
        _, index = read_while(source, index, str.isdigit)
    return source[start:index], index


def read_punct(source: str, index: int) -> tuple[str, int]:
    for token in MULTI_PUNCT:
        if source.startswith(token, index):
            return token, index + len(token)
    return source[index], index + 1


def skip_line_comment(source: str, index: int) -> int:
    while index < len(source) and source[index] not in "\r\n":
        index += 1
    return index


def skip_block_comment(source: str, index: int) -> int:
    end = source.find("*/", index + 2)
    if end < 0:
        raise SystemExit("Unterminated block comment")
    return end + 2


def tokenize(source: str) -> list[tuple[str, str]]:
    tokens: list[tuple[str, str]] = []
    index = 0
    length = len(source)
    last_kind = ""
    last_value = ""
    while index < length:
        ch = source[index]
        if ch in " \t\r\n\f\v":
            index += 1
            continue
        if ch == "/" and index + 1 < length:
            nxt = source[index + 1]
            if nxt == "/":
                index = skip_line_comment(source, index + 2)
                continue
            if nxt == "*":
                index = skip_block_comment(source, index)
                continue
            regex = False
            if last_kind in ("", "punct") and (not last_value or last_value[-1] in REGEX_PUNCT):
                regex = True
            elif last_kind == "ident" and last_value in KEYWORD_BEFORE_REGEX:
                regex = True
            if regex:
                value, index = read_regex(source, index)
                tokens.append(("regex", value))
                last_kind, last_value = "regex", value
                continue
        if ch in "'\"":
            value, index = read_string(source, index, ch)
            tokens.append(("string", value))
            last_kind, last_value = "string", value
            continue
        if ch == "`":
            value, index = read_template(source, index)
            tokens.append(("string", value))
            last_kind, last_value = "string", value
            continue
        if ch.isdigit() or (ch == "." and index + 1 < length and source[index + 1].isdigit()):
            value, index = read_number(source, index)
            tokens.append(("number", value))
            last_kind, last_value = "number", value
            continue
        if ch in IDENT_START:
            value, index = read_while(source, index, is_ident)
            tokens.append(("ident", value))
            last_kind, last_value = "ident", value
            continue
        value, index = read_punct(source, index)
        tokens.append(("punct", value))
        last_kind, last_value = "punct", value
    return tokens


def needs_space(prev_kind: str, prev: str, kind: str, value: str) -> bool:
    if not prev:
        return False
    if prev_kind in ("ident", "number") and kind in ("ident", "number"):
        return True
    if prev_kind == "ident" and kind == "regex":
        return True
    if prev_kind == "ident" and kind == "string":
        return True
    if prev[-1] == "+" and value[:1] == "+":
        return True
    if prev[-1] == "-" and value[:1] == "-":
        return True
    if prev[-1] == "/" and value[:1] in "/*":
        return True
    if prev_kind == "number" and value[:1] == ".":
        return True
    return False


def minify_js(source: str) -> str:
    license_text, body = extract_license(source)
    out: list[str] = []
    prev_kind = ""
    prev = ""
    for kind, value in tokenize(body):
        if needs_space(prev_kind, prev, kind, value):
            out.append(" ")
        out.append(value)
        prev_kind, prev = kind, value
    packed = "".join(out)
    return license_text + packed + "\n"


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Pack a compact MMD viewer script")
    parser.add_argument("-o", "--output", type=Path, default=OUTPUT)
    parser.add_argument("--no-embed-atlas", action="store_true")
    parser.add_argument("--source", type=Path, default=SOURCE)
    parser.add_argument("--atlas", type=Path, default=ATLAS)
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> None:
    args = parse_args(argv or sys.argv[1:])
    source = args.source.read_text(encoding="utf-8")
    atlas_js = None
    if not args.no_embed_atlas:
        if not args.atlas.is_file():
            raise SystemExit("Missing atlas file: %s" % args.atlas)
        atlas_js = compact_atlas(args.atlas)
    packed = minify_js(inject_atlas(source, atlas_js))
    args.output.write_text(packed, encoding="utf-8")
    src_size = args.source.stat().st_size
    out_size = args.output.stat().st_size
    extra = " (atlas embedded, %d bytes compact)" % len(atlas_js.encode("utf-8")) if atlas_js else ""
    print("Wrote %s (%d -> %d bytes)%s" % (args.output, src_size, out_size, extra))


if __name__ == "__main__":
    main()
