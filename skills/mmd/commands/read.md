---
description: Read Scia Reto .mmd maps as knowledge — parse, do not rewrite unless asked
---

Activate the **mmd** skill and **read** mind maps as project knowledge.

1. Read this skill’s `SKILL.md` and use `scripts/parse.py` (stdlib Python 3). Do not freehand-parse `.mmd`.
2. Scope is the text below (paths, globs, “maps in this module”). If empty, find relevant `.mmd` files or ask.
3. Parse each map to JSON. Summarize structure, extras (FILE/LINK/NOTE/TOPIC), and jumps. Use that as context for the user’s question.
4. If a needed note is encrypted (`extras.note.encrypted`), follow the skill: list with `crypto.py list`, ask for the password, decrypt on stdin. Do not guess. Do not treat ciphertext as text.
5. Do **not** write `.mmd` unless the user asked to change a map — then switch to `/mmd:edit`.

Scope:
$ARGUMENTS
