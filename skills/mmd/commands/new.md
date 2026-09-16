---
description: Create a new Scia Reto .mmd mind map via JSON and validate.py
---

Activate the **mmd** skill and **create** a new `.mmd` file.

1. Read this skill’s `SKILL.md` (section **New map**). Build the tree as JSON, then `scripts/write.py` and `scripts/validate.py`. Do not freehand-write the file.
2. Scope is the text below (output path and root/topic outline). If the path is missing, ask. Do not overwrite an existing map unless the user said to replace it.
3. Use the minimal JSON shape from the skill. Add children, extras, and `uid` / jumps only as requested. Set map `showJumps` to `"true"` if you add TOPIC extras.
4. Validate. Proceed only on `OK`.

Scope:
$ARGUMENTS
