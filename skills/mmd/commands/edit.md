---
description: Edit a Scia Reto .mmd map via parse → JSON → write → validate
---

Activate the **mmd** skill and **edit** mind map files.

1. Read this skill’s `SKILL.md`. Use only `scripts/parse.py`, `scripts/write.py`, and `scripts/validate.py`. Do not freehand-write `.mmd`.
2. Scope is the text below (file paths and what to change). If empty, ask which map and what to change.
3. Parse → edit **only** the JSON tree (titles, attributes, extras, snippets, children) → write → validate. Proceed only when validate prints `OK`.
4. Encrypted notes: if you must read or change one, ask for the password and use `crypto.py` on stdin. Re-encrypt on write. Never put the password on the command line or in files.
5. Keep `showJumps` `"true"` when TOPIC extras / `topicLinkUID` exist. Emoticon ids: [MMD_Format.MD](../MMD_Format.MD) Appendix A.

Scope:
$ARGUMENTS
