---
description: Validate Scia Reto .mmd files with the skill scripts
---

Activate the **mmd** skill and **validate** mind map files.

1. Read this skill’s `SKILL.md`. Run `python3 scripts/validate.py` on each path (stdlib Python 3).
2. Scope is the text below (paths or globs). If empty, validate `.mmd` files the user has open or just edited, or ask.
3. Report `OK` or the script’s issues. If asked to fix, switch to `/mmd:edit` (parse → JSON → write → validate again). Do not freehand-patch `.mmd`.

Scope:
$ARGUMENTS
