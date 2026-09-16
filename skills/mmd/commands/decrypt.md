---
description: List and decrypt encrypted MMD topic notes after asking for the password
---

Activate the **mmd** skill and work with **encrypted topic notes**.

1. Read this skill’s `SKILL.md` section **Encrypted notes**.
2. Scope is the text below (`.mmd` path). If empty, ask which file.
3. `python3 scripts/crypto.py list` that file. Show topic paths and hints. **Ask the user for the password.** Do not guess. Do not put the password on the command line.
4. Decrypt with password on stdin (`--password-stdin`). If exit `1`, say the password is wrong or the ciphertext is corrupt and ask again. Do not invent plaintext.
5. Never write the password into `.mmd`, JSON, logs, or project notes. If the user also wants the map updated, re-encrypt with the same password on write.

Scope:
$ARGUMENTS
