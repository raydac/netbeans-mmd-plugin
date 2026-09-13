---
name: mmd
description: Parse, create, edit, and validate Scia Reto MMD mind maps (.mmd). Decrypt encrypted topic notes after asking the user for the password. Use when the user asks about MMD files, mind maps, Scia Reto maps, topic extras, encrypted notes, emoticons, or converting a map to or from JSON.
---

# MMD mind maps

Work with `.mmd` files through the scripts in this skill. Do **not** freehand-write MMD except a trivial empty map, then still run `validate.py`.

This folder is self-contained: `SKILL.md`, [reference.md](reference.md), [MMD_Format.MD](MMD_Format.MD), and `scripts/`. Copy the whole folder.

Scripts are stdlib Python 3. Run them from this skill directory, or pass the script path from the workspace root.

```bash
python3 scripts/parse.py path.mmd
python3 scripts/write.py --in tree.json --out path.mmd
python3 scripts/validate.py path.mmd
python3 scripts/crypto.py list path.mmd
```

From this repository (before copying the skill elsewhere):

```bash
python3 skills/mmd/scripts/parse.py path.mmd
```

## Workflow

1. **Parse** an existing map → JSON on stdout.
2. **Edit** only that JSON (text, attributes, extras, snippets, children).
3. **Write** JSON back to `.mmd`.
4. **Validate**. If it prints issues, fix the JSON or the file and run validate again. Proceed only on `OK`.

```bash
python3 scripts/parse.py notes.mmd > /tmp/tree.json
# edit /tmp/tree.json
python3 scripts/write.py --in /tmp/tree.json --out notes.mmd
python3 scripts/validate.py notes.mmd
```

`--in -` / `--out -` read and write stdio. `--strict` on parse fails on invalid extras.

## Encrypted notes

A topic is encrypted when `attributes["extras.note.encrypted"]` is `true` (case-insensitive). The `extras.NOTE` value is then ciphertext, not readable text. `extras.note.encrypted.hint` is an optional password hint stored in clear.

**If that note text is needed for the task, ask the user for the password and decrypt. Do not guess the password. Do not treat ciphertext as plaintext.**

1. List encrypted notes (path + hint + ciphertext):

```bash
python3 scripts/crypto.py list path.mmd
```

2. Ask the user for the password. Show the topic path and the hint when present. The Scia Reto editor trims surrounding whitespace on the password; do the same.
3. Decrypt. Put the password on stdin, not on the command line:

```bash
printf '%s\n' "$PASSWORD" | python3 scripts/crypto.py decrypt --password-stdin --text 'CIPHERTEXT'
```

4. If the script exits `1`, the password is wrong or the ciphertext is corrupt. Tell the user and ask again. Do not invent plaintext.
5. Never write the password into `.mmd`, JSON, logs, changelog, or project notes.
6. If you edit the note and write the map back, re-encrypt with the same password and keep `extras.note.encrypted` `true` (and the hint if it had one):

```bash
printf '%s\n' "$PASSWORD" | python3 scripts/crypto.py encrypt --password-stdin --text 'PLAINTEXT'
```

If the encrypted note is not needed (for example only renaming a topic), leave the ciphertext and flags unchanged.

## JSON tree

```json
{
  "header": "[Scia Reto](https://sciareto.org) mind map",
  "attributes": { "__version__": "1.1", "showJumps": "true" },
  "root": {
    "text": "Root",
    "attributes": { "fillColor": "#7AA3E5", "mmd.emoticon": "acorn" },
    "extras": {
      "FILE": "./spec.pdf",
      "LINK": "https://example.com",
      "NOTE": "Read first",
      "TOPIC": "UID"
    },
    "snippets": { "Java": "System.exit(0);\n" },
    "children": []
  }
}
```

- `root` may be `null` for an empty map (delimiter only).
- Extra keys: `FILE`, `LINK`, `NOTE`, `TOPIC` (at most one of each).
- Scripts own Markdown / `<pre>` / backtick escaping. Put raw title text and raw extra payloads in JSON.
- Writer always emits format `__version__` `1.1` and the canonical Scia Reto header.
- Internal jumps: set `topicLinkUID` on the target and `extras.TOPIC` on the source to that id. Set map `showJumps` to `"true"` to draw them.
- Emoticon ids: `mmd.emoticon` values from [MMD_Format.MD](MMD_Format.MD) Appendix A. Chooser id `empty` means remove the attribute.

## New map

Minimal JSON:

```json
{
  "attributes": {},
  "root": {
    "text": "Root",
    "attributes": {},
    "extras": {},
    "snippets": {},
    "children": []
  }
}
```

## Details

Format rules, extras, [encrypted notes](MMD_Format.MD#encrypted-notes), and the emoticon catalog: [MMD_Format.MD](MMD_Format.MD). Script commands: [reference.md](reference.md).
