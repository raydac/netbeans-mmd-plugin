#!/usr/bin/env python3
"""Write an MMD mind map from a JSON tree."""

from __future__ import annotations

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import mmd  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="Write an MMD mind map from JSON")
    parser.add_argument(
        "--in",
        dest="source",
        default="-",
        help="JSON tree file, or - for stdin (default: stdin)",
    )
    parser.add_argument(
        "--out",
        dest="target",
        default="-",
        help="output .mmd path, or - for stdout (default: stdout)",
    )
    args = parser.parse_args()
    try:
        if args.source == "-":
            payload = sys.stdin.read()
        else:
            with open(args.source, "r", encoding="utf-8") as handle:
                payload = handle.read()
        text = mmd.write_mmd(mmd.loads_tree(payload))
        if args.target == "-":
            sys.stdout.write(text)
        else:
            with open(args.target, "w", encoding="utf-8", newline="\n") as handle:
                handle.write(text)
    except (OSError, ValueError, mmd.MmdError) as error:
        sys.stderr.write("%s\n" % error)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
