#!/usr/bin/env python3
"""Parse an MMD mind map file to JSON on stdout."""

from __future__ import annotations

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import mmd  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="Parse an MMD mind map to JSON")
    parser.add_argument("path", help="path to a .mmd file")
    parser.add_argument("--compact", action="store_true", help="JSON without indentation")
    parser.add_argument(
        "--strict",
        action="store_true",
        help="fail on invalid extras instead of skipping them",
    )
    args = parser.parse_args()
    try:
        tree = mmd.parse_file(args.path, ignore_errors=not args.strict)
        sys.stdout.write(mmd.dumps_tree(tree, pretty=not args.compact))
    except (OSError, ValueError, mmd.MmdError) as error:
        sys.stderr.write("%s\n" % error)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
