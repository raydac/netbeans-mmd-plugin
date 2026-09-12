#!/usr/bin/env python3
"""Validate an MMD mind map (structural checks + canonical round-trip)."""

from __future__ import annotations

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import mmd  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate an MMD mind map")
    parser.add_argument("path", help="path to a .mmd file")
    args = parser.parse_args()
    try:
        with open(args.path, "r", encoding="utf-8", newline="") as handle:
            text = handle.read()
    except OSError as error:
        sys.stderr.write("%s\n" % error)
        return 1
    issues = mmd.validate_mmd(text)
    if issues:
        for issue in issues:
            sys.stderr.write("%s\n" % issue)
        return 1
    sys.stdout.write("OK\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
