#!/usr/bin/env python3
"""Parse and write Scia Reto MMD mind maps (format version 1.1).

Behaviour follows mind-map-model MindMap / Topic.parse / ModelUtils.
"""

from __future__ import annotations

import html.entities
import json
import os
import re
import tempfile
import unicodedata
from typing import Any, Dict, List, Mapping, Optional, Tuple

FORMAT_VERSION = "1.1"
CANONICAL_HEADER = "[Scia Reto](https://sciareto.org) mind map"
EXTRA_TYPES = ("FILE", "LINK", "NOTE", "TOPIC")
MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!"
UNESCAPE_BR = re.compile(r"(?i)<\s*?br\s*?/?>")
MD_ESCAPED_PATTERN = re.compile(r"(\\[\\`*_{}\[\]()#<>+-.!])")
PATTERN_ATTRIBUTES = re.compile(r"^\s*>\s(.+)$")
PATTERN_ATTRIBUTE = re.compile(r"[,]?[ \t]*(\S+?)[ \t]*=[ \t]*(`+)(.*?)(\2)")
PRE_ENTITY = re.compile(r"&(#x[0-9A-Fa-f]+|#\d+|[A-Za-z][A-Za-z0-9]+);")
URI_ILLEGAL_ASCII = frozenset(" <>\"{}|\\^`")
JAVA_WHITESPACE_ASCII = frozenset(range(0x09, 0x0E)) | frozenset(range(0x1C, 0x20)) | {0x20}
JAVA_WHITESPACE_EXCLUDED = frozenset((0x00A0, 0x2007, 0x202F))


class MmdError(ValueError):
    """Format or validation error."""


class Topic:
    def __init__(self, text: str, parent: Optional["Topic"] = None) -> None:
        self.text = text
        self.parent = parent
        self.children: List[Topic] = []
        self.attributes: Dict[str, str] = {}
        self.extras: Dict[str, str] = {}
        self.snippets: Dict[str, str] = {}
        if parent is not None:
            parent.children.append(self)

    def find_parent_for_depth(self, depth: int) -> Optional["Topic"]:
        result = self.parent
        while depth > 0 and result is not None:
            result = result.parent
            depth -= 1
        return result

    def get_root(self) -> "Topic":
        current = self
        while current.parent is not None:
            current = current.parent
        return current


class MindMap:
    def __init__(self) -> None:
        self.header = CANONICAL_HEADER
        self.attributes: Dict[str, str] = {}
        self.root: Optional[Topic] = None


def is_iso_control(char: str) -> bool:
    code = ord(char)
    return code <= 0x1F or 0x7F <= code <= 0x9F


def is_java_whitespace(char: str) -> bool:
    code = ord(char)
    if code in JAVA_WHITESPACE_ASCII:
        return True
    if code in JAVA_WHITESPACE_EXCLUDED:
        return False
    return unicodedata.category(char) in ("Zs", "Zl", "Zp")


def remove_iso_controls(text: str) -> str:
    return "".join(ch for ch in text if not is_iso_control(ch))


def escape_markdown(text: str) -> str:
    buffer: List[str] = []
    for char in text:
        if char == "\n":
            buffer.append("<br/>")
            continue
        if is_iso_control(char):
            continue
        if char in MD_ESCAPED_CHARS:
            buffer.append("\\")
        buffer.append(char)
    return "".join(buffer)


def unescape_markdown(text: str) -> str:
    unescaped = UNESCAPE_BR.sub("\n", text)
    return MD_ESCAPED_PATTERN.sub(lambda match: match.group(1)[1:], unescaped)


def max_backtick_run(text: Optional[str]) -> int:
    if not text:
        return 0
    longest = 0
    pos = 0
    while True:
        pos = text.find("`", pos)
        if pos < 0:
            return longest
        found = 0
        while pos < len(text) and text[pos] == "`":
            found += 1
            pos += 1
        longest = max(longest, found)


def make_md_code_block(text: str) -> str:
    ticks = "`" * (max_backtick_run(text) + 1)
    return ticks + text + ticks


def escape_pre(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace('"', "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    )


def unescape_pre(text: str) -> str:
    def replace_entity(match: re.Match[str]) -> str:
        body = match.group(1)
        try:
            if body.startswith("#x") or body.startswith("#X"):
                code = int(body[2:], 16)
            elif body.startswith("#"):
                code = int(body[1:], 10)
            else:
                mapped = html.entities.name2codepoint.get(body)
                if mapped is None:
                    return match.group(0)
                code = mapped
            if code == 0 or code > 0x10FFFF or 0xD800 <= code <= 0xDFFF:
                return match.group(0)
            return chr(code)
        except ValueError:
            return match.group(0)

    return PRE_ENTITY.sub(replace_entity, text)


def fill_map_by_attributes(line: str, target: Dict[str, str]) -> bool:
    stripped = line.rstrip("\r\n")
    matched = PATTERN_ATTRIBUTES.match(stripped)
    if matched is None:
        return False
    for item in PATTERN_ATTRIBUTE.finditer(matched.group(1)):
        target[item.group(1)] = item.group(3)
    return True


def attributes_as_string(values: Mapping[str, str]) -> str:
    parts = []
    for key in sorted(values):
        parts.append("%s=%s" % (key, make_md_code_block(values[key])))
    return ",".join(parts)


def is_header_delimiter(line: str) -> bool:
    core = line.replace("\r", "")
    return bool(core) and all(char == "-" for char in core)


def split_header(text: str) -> Tuple[str, str]:
    index = 0
    length = len(text)
    while index < length:
        newline_at = text.find("\n", index)
        if newline_at < 0:
            line = text[index:]
            rest_start = length
        else:
            line = text[index:newline_at]
            rest_start = newline_at + 1
        if is_header_delimiter(line):
            return text[:index], text[rest_start:]
        if newline_at < 0:
            break
        index = rest_start
    raise MmdError("Wrong format of mind map, end of header is not found")


def first_header_title(header: str) -> str:
    for raw_line in header.split("\n"):
        line = raw_line.replace("\r", "").rstrip()
        if not line or line.lstrip().startswith("> "):
            continue
        return line.rstrip(" ")
    return CANONICAL_HEADER


def skip_whitespace(text: str, index: int) -> int:
    length = len(text)
    while index < length and (is_java_whitespace(text[index]) or is_iso_control(text[index])):
        index += 1
    return index


def skip_to_newline(text: str, index: int) -> int:
    newline_at = text.find("\n", index)
    return len(text) if newline_at < 0 else newline_at + 1


def line_is_all_char(line: str, char: str) -> bool:
    seen = False
    body = line[:-1] if line.endswith("\n") else line
    for item in body:
        if item == "\r":
            continue
        if item != char:
            return False
        seen = True
    return seen


def find_pre_end(text: str, start: int) -> Optional[int]:
    if start < 0 or not text.startswith("<pre>", start):
        return None
    index = start + 5
    length = len(text)
    while index < length:
        if text[index] == ">" and index >= 5 and text.startswith("</pre>", index - 5):
            return index + 1
        index += 1
    return None


def find_snippet_closer(text: str, body_start: int) -> Tuple[bool, int, int]:
    length = len(text)
    index = body_start
    line_start = index == 0 or (index > 0 and text[index - 1] == "\n")
    line_start_position = index if line_start else -1
    backtick_count = 0
    detected_spaces = 0
    found = False

    while not found and index < length:
        char = text[index]
        index += 1
        if char == "`":
            if detected_spaces == 0 and (backtick_count > 0 or line_start):
                backtick_count += 1
            else:
                backtick_count = 0
            line_start = False
        elif char == "\n":
            if backtick_count == 3:
                found = True
            else:
                line_start_position = index
                backtick_count = 0
            line_start = True
            detected_spaces = 0
        else:
            if is_java_whitespace(char):
                detected_spaces += 1
            elif not is_iso_control(char):
                backtick_count = 0
            line_start = False

    if found or backtick_count == 3:
        closer_end = index if found else length
        return True, line_start_position, closer_end
    return False, body_start, length


def java_uri_ok(value: str) -> bool:
    for char in value:
        code = ord(char)
        if code < 128 and (code < 0x20 or char in URI_ILLEGAL_ASCII):
            return False
    index = 0
    while True:
        percent_at = value.find("%", index)
        if percent_at < 0:
            return True
        hex_part = value[percent_at + 1 : percent_at + 3]
        if len(hex_part) < 2 or any(char not in "0123456789abcdefABCDEF" for char in hex_part):
            return False
        index = percent_at + 3


def preprocess_extra(extra_type: str, raw: str) -> Optional[str]:
    if extra_type in ("FILE", "LINK"):
        trimmed = raw.strip()
        return trimmed if java_uri_ok(trimmed) else None
    if extra_type == "TOPIC":
        return raw.strip()
    return raw


def attach_heading(current: Optional[Topic], depth: int, detected_level: int, title: str) -> Tuple[Optional[Topic], int]:
    if detected_level == depth + 1:
        return Topic(title, current), detected_level
    if detected_level == depth:
        parent = None if current is None else current.parent
        return Topic(title, parent), depth
    if detected_level < depth:
        if current is None:
            return current, depth
        parent = current.find_parent_for_depth(depth - detected_level)
        return Topic(title, parent), detected_level
    if detected_level > depth + 1 and current is not None:
        return Topic(title, current), detected_level
    return current, depth


def parse_heading(text: str, index: int) -> Tuple[int, str, bool, int]:
    length = len(text)
    level = 0
    while index < length and text[index] == "#":
        level += 1
        index += 1
    if index < length and is_java_whitespace(text[index]) and text[index] != "\n":
        index += 1
    title_start = index
    while index < length and text[index] != "\n":
        index += 1
    had_newline = index < length
    title_raw = text[title_start:index]
    if had_newline:
        index += 1
    return level, title_raw, had_newline, index


def parse_body(body: str, ignore_errors: bool) -> Optional[Topic]:
    index = 0
    length = len(body)
    current: Optional[Topic] = None
    depth = 0
    extra_type: Optional[str] = None
    snippet_language: Optional[str] = None
    snippet_body: Optional[List[str]] = None

    while index < length:
        if snippet_language is None:
            index = skip_whitespace(body, index)
            if index >= length:
                break

        char = body[index]

        if snippet_language is not None:
            found, body_end, closer_end = find_snippet_closer(body, index)
            if found and current is not None and snippet_body is not None:
                snippet_body.append(body[index:body_end])
                current.snippets[snippet_language.strip()] = "".join(snippet_body)
            snippet_language = None
            snippet_body = None
            if found:
                index = closer_end
            else:
                index = length
            extra_type = None
            continue

        if char == "#":
            extra_type = None
            detected_level, title_raw, had_newline, index = parse_heading(body, index)
            if not had_newline and title_raw == "":
                break
            title = unescape_markdown(remove_iso_controls(title_raw))
            current, depth = attach_heading(current, depth, detected_level, title)
            continue

        if char == ">" and index + 1 < length and body[index + 1] == " ":
            line_end = skip_to_newline(body, index)
            if current is not None:
                fill_map_by_attributes(body[index:line_end], current.attributes)
            extra_type = None
            index = line_end
            continue

        if char == "-" and index + 1 < length and body[index + 1] == " ":
            line_end = skip_to_newline(body, index)
            name = body[index + 1:line_end].strip()
            extra_type = name if name in EXTRA_TYPES else None
            index = line_end
            continue

        if char == "<":
            if body.startswith("<pre>", index):
                pre_end = find_pre_end(body, index)
                if pre_end is None:
                    extra_type = None
                    index = skip_to_newline(body, index)
                    continue
                raw = body[index:pre_end]
                if current is not None and extra_type is not None:
                    inner = unescape_pre(raw[5:-6])
                    prepared = preprocess_extra(extra_type, inner)
                    if prepared is not None:
                        current.extras[extra_type] = prepared
                    elif not ignore_errors:
                        raise MmdError("Detected invalid extra data %s" % extra_type)
                extra_type = None
                index = pre_end
                continue
            extra_type = None
            index = skip_to_newline(body, index)
            continue

        if body.startswith("```", index):
            line_end = skip_to_newline(body, index)
            fence_line = body[index:line_end]
            if line_is_all_char(fence_line, "`"):
                extra_type = None
                index = line_end
                continue
            if current is not None:
                snippet_language = fence_line[3:]
                snippet_body = []
            extra_type = None
            index = line_end
            continue

        extra_type = None
        index = skip_to_newline(body, index)

    return None if current is None else current.get_root()


def parse_mmd(text: str, ignore_errors: bool = True) -> MindMap:
    if text.startswith("\ufeff"):
        text = text[1:]
    header, body = split_header(text)
    mind_map = MindMap()
    mind_map.header = first_header_title(header)
    for raw_line in header.split("\n"):
        fill_map_by_attributes(raw_line.replace("\r", ""), mind_map.attributes)
    mind_map.root = parse_body(body, ignore_errors)
    mind_map.attributes["__version__"] = FORMAT_VERSION
    return mind_map


def parse_file(path: str, ignore_errors: bool = True) -> MindMap:
    with open(path, "r", encoding="utf-8-sig", newline="") as handle:
        return parse_mmd(handle.read(), ignore_errors)


def topic_to_dict(topic: Optional[Topic]) -> Optional[Dict[str, Any]]:
    if topic is None:
        return None
    return {
        "text": topic.text,
        "attributes": dict(topic.attributes),
        "extras": dict(topic.extras),
        "snippets": dict(topic.snippets),
        "children": [topic_to_dict(child) for child in topic.children],
    }


def mind_map_to_dict(mind_map: MindMap) -> Dict[str, Any]:
    return {
        "header": mind_map.header,
        "attributes": dict(mind_map.attributes),
        "root": topic_to_dict(mind_map.root),
    }


def _json_scalar_to_str(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, (dict, list)):
        raise MmdError("JSON tree fields must be scalars, not nested objects")
    return str(value)


def _string_map(value: Any, field: str) -> Dict[str, str]:
    if value is None:
        return {}
    if not isinstance(value, Mapping):
        raise MmdError("%s must be an object" % field)
    return {str(key): _json_scalar_to_str(item) for key, item in value.items()}


def topic_from_dict(data: Optional[Mapping[str, Any]], parent: Optional[Topic] = None) -> Optional[Topic]:
    if data is None:
        return None
    if not isinstance(data, Mapping):
        raise MmdError("topic JSON must be an object")
    topic = Topic(_json_scalar_to_str(data.get("text", "")), parent)
    topic.attributes.update(_string_map(data.get("attributes"), "attributes"))
    extras = _string_map(data.get("extras"), "extras")
    for extra_name, extra_value in extras.items():
        if extra_name not in EXTRA_TYPES:
            continue
        prepared = preprocess_extra(extra_name, extra_value)
        if prepared is not None:
            topic.extras[extra_name] = prepared
    topic.snippets.update(_string_map(data.get("snippets"), "snippets"))
    children = data.get("children") or []
    if not isinstance(children, list):
        raise MmdError("children must be an array")
    for child in children:
        topic_from_dict(child, topic)
    return topic


def mind_map_from_dict(data: Mapping[str, Any]) -> MindMap:
    if not isinstance(data, Mapping):
        raise MmdError("JSON tree must be an object")
    mind_map = MindMap()
    header = data.get("header")
    if header:
        mind_map.header = _json_scalar_to_str(header)
    mind_map.attributes.update(_string_map(data.get("attributes"), "attributes"))
    mind_map.attributes["__version__"] = FORMAT_VERSION
    root = data.get("root")
    try:
        mind_map.root = topic_from_dict(root)
    except RecursionError as error:
        raise MmdError("mind map is too deeply nested") from error
    return mind_map


def write_topic(topic: Topic, level: int, out: List[str]) -> None:
    out.append("\n")
    out.append("#" * level)
    out.append(" ")
    out.append(escape_markdown(topic.text))
    out.append("\n")

    attributes_to_write = dict(topic.attributes)
    if attributes_to_write:
        out.append("> ")
        out.append(attributes_as_string(attributes_to_write))
        out.append("\n\n")

    for extra_type in sorted(topic.extras):
        out.append("- ")
        out.append(extra_type)
        out.append("\n")
        out.append("<pre>")
        out.append(escape_pre(topic.extras[extra_type]))
        out.append("</pre>\n")

    for language in sorted(topic.snippets):
        body = topic.snippets[language]
        out.append("```")
        out.append(language)
        out.append("\n")
        out.append(body)
        if not body.endswith("\n"):
            out.append("\n")
        out.append("```\n")

    for child in topic.children:
        write_topic(child, level + 1, out)


def write_mmd(mind_map: MindMap) -> str:
    attributes = dict(mind_map.attributes)
    attributes["__version__"] = FORMAT_VERSION
    parts = [CANONICAL_HEADER, "   \n> ", attributes_as_string(attributes), "\n---\n"]
    try:
        if mind_map.root is not None:
            write_topic(mind_map.root, 1, parts)
    except RecursionError as error:
        raise MmdError("mind map is too deeply nested") from error
    return "".join(parts)


def write_file(path: str, text: str) -> None:
    directory = os.path.dirname(os.path.abspath(path)) or "."
    handle, tmp_path = tempfile.mkstemp(prefix=".mmd-write-", suffix=".tmp", dir=directory)
    try:
        with os.fdopen(handle, "w", encoding="utf-8", newline="\n") as output:
            output.write(text)
            output.flush()
            os.fsync(output.fileno())
        os.replace(tmp_path, path)
        tmp_path = ""
    finally:
        if tmp_path:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass


def dumps_tree(mind_map: MindMap, pretty: bool = True) -> str:
    try:
        payload = mind_map_to_dict(mind_map)
    except RecursionError as error:
        raise MmdError("mind map is too deeply nested") from error
    if pretty:
        return json.dumps(payload, ensure_ascii=False, indent=2) + "\n"
    return json.dumps(payload, ensure_ascii=False) + "\n"


def loads_tree(text: str) -> MindMap:
    try:
        data = json.loads(text)
    except json.JSONDecodeError as error:
        raise MmdError("invalid JSON tree: %s" % error) from error
    if not isinstance(data, dict):
        raise MmdError("JSON tree must be an object")
    return mind_map_from_dict(data)


def normalize_topic(topic: Optional[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    if topic is None:
        return None
    extras = topic.get("extras") or {}
    snippets = topic.get("snippets") or {}
    return {
        "text": topic.get("text", ""),
        "attributes": dict(topic.get("attributes") or {}),
        "extras": {key: extras[key] for key in sorted(extras)},
        "snippets": {key: snippets[key] for key in sorted(snippets)},
        "children": [normalize_topic(child) for child in topic.get("children") or []],
    }


def comparable_tree(mind_map: MindMap) -> Dict[str, Any]:
    attributes = dict(mind_map.attributes)
    attributes["__version__"] = FORMAT_VERSION
    return {
        "attributes": attributes,
        "root": normalize_topic(topic_to_dict(mind_map.root)),
    }


def collect_issues(mind_map: MindMap) -> List[str]:
    issues: List[str] = []
    if "__version__" not in mind_map.attributes:
        issues.append("missing map attribute __version__")

    def walk(topic: Optional[Topic], heading_level: int) -> None:
        if topic is None:
            return
        if heading_level == 1 and topic.parent is not None:
            issues.append("root topic has a parent")
        for extra_type in topic.extras:
            if extra_type not in EXTRA_TYPES:
                issues.append("unknown extra type %s on %r" % (extra_type, topic.text))
            elif extra_type in ("FILE", "LINK") and not java_uri_ok(topic.extras[extra_type]):
                issues.append("invalid %s URI on %r" % (extra_type, topic.text))
        for child in topic.children:
            walk(child, heading_level + 1)

    walk(mind_map.root, 1)
    return issues


def validate_mmd(text: str) -> List[str]:
    issues: List[str] = []
    try:
        parsed = parse_mmd(text, ignore_errors=True)
    except MmdError as error:
        return [str(error)]
    try:
        issues.extend(collect_issues(parsed))
        rewritten = write_mmd(parsed)
        again = parse_mmd(rewritten, ignore_errors=True)
    except RecursionError:
        issues.append("mind map is too deeply nested")
        return issues
    except MmdError as error:
        issues.append("canonical write failed to parse: %s" % error)
        return issues
    try:
        if comparable_tree(parsed) != comparable_tree(again):
            issues.append("round-trip tree mismatch after canonical write")
    except RecursionError:
        issues.append("mind map is too deeply nested")
    return issues
