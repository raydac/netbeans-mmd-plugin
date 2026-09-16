# Agent skills

Copyable kits so an LLM agent can work with Scia Reto **MMD mind maps** in a user project: read and edit `.mmd` files, keep encrypted notes safe, and (for Java) generate maps from source annotations.

This folder is the product. Copy a whole skill directory into the agent’s skills location. Skills here are **not** loaded from this path automatically.

Each skill is a folder with `SKILL.md` plus optional `commands/`, `scripts/`, and reference files. After install, Cursor exposes `/<skill>:<command>` from `commands/*.md`.

## Install

Copy the **whole** skill folder, not only `SKILL.md`.

**Cursor**

- One project: `<project>/.cursor/skills/<skill>/`
- All projects: `~/.cursor/skills/<skill>/`

```bash
cp -R skills/mmd <project>/.cursor/skills/mmd
cp -R skills/mmd-annotations <project>/.cursor/skills/mmd-annotations
# or
cp -R skills/mmd ~/.cursor/skills/mmd
cp -R skills/mmd-annotations ~/.cursor/skills/mmd-annotations
```

Do not copy into `~/.cursor/skills-cursor/` (reserved for Cursor built-in skills).

**Other agents**

Use that product’s skill or plugin directory. Same layout: `SKILL.md` at the skill root.

## Commands (after install)

### Maps (skill [`mmd/`](mmd/SKILL.md))

| Command | Use when |
|---------|----------|
| `/mmd:read` | Use existing `.mmd` files as knowledge (parse only) |
| `/mmd:edit` | Change a map: parse → JSON → write → validate |
| `/mmd:new` | Create a new `.mmd` |
| `/mmd:validate` | Check that a map is well-formed |
| `/mmd:decrypt` | Read encrypted topic notes (asks for the password) |

Scripts are stdlib Python 3. From the skill folder:

```bash
python3 scripts/parse.py path.mmd
python3 scripts/write.py --in tree.json --out path.mmd
python3 scripts/validate.py path.mmd
python3 scripts/crypto.py list path.mmd
```

Do not freehand-write `.mmd` except a trivial empty file, then still validate. Never put a note password on the command line.

### Java → maps (skill [`mmd-annotations/`](mmd-annotations/SKILL.md))

| Command | Use when |
|---------|----------|
| `/mmd-annotations:setup` | Add `mind-map-annotations` + processor and fix compiler options |
| `/mmd-annotations:mark` | Setup, then mark types/methods so compile generates `.mmd` |

The agent must attach the artifacts and audit build options **before** any `@Mmd*` on user sources.

## Typical work

1. **Keep knowledge in maps** — `/mmd:new` or `/mmd:edit` on project `.mmd` files; `/mmd:read` when a later agent should learn the domain from those files.
2. **Generate maps from Java** — `/mmd-annotations:setup` once per module, then `/mmd-annotations:mark` for the architecture that should stay visible; compile writes the files; `/mmd:read` or `/mmd:validate` on the output.
3. **Encrypted notes** — `/mmd:decrypt` only when the note text is required; ask the user for the password.

## Skills

| Folder | What it ships |
|--------|----------------|
| [`mmd/`](mmd/SKILL.md) | Format spec, Python parse/write/validate/crypto, slash commands for maps |
| [`mmd-annotations/`](mmd-annotations/SKILL.md) | How to wire the processor and mark Java; slash commands for setup and mark |
