# Agent skills

This folder holds **copyable** skills for AI coding agents that work with this project. They are not loaded automatically. Copy a skill directory into the skills location of the agent you use.

Each skill is a folder with `SKILL.md` (required) plus optional scripts and reference files.

## Install

Copy the whole skill folder (for example `mmd/`), not only `SKILL.md`.

**Cursor**

- One project: `<project>/.cursor/skills/mmd/`
- All projects: `~/.cursor/skills/mmd/`

```bash
cp -R skills/mmd <project>/.cursor/skills/mmd
# or
cp -R skills/mmd ~/.cursor/skills/mmd
```

Do not copy into `~/.cursor/skills-cursor/` (reserved for Cursor built-in skills).

**Other agents**

Use that product’s skill or plugin directory. The layout is the usual one: `SKILL.md` at the skill root, scripts next to it.

## Skills

| Folder | Use |
|--------|-----|
| [`mmd/`](mmd/SKILL.md) | Parse, edit, validate, and (after asking for the password) decrypt Scia Reto `.mmd` mind maps |

Scripts are stdlib Python 3. After copying, run them from the skill folder:

```bash
python3 scripts/parse.py path.mmd
python3 scripts/validate.py path.mmd
```

The full MMD format spec stays in the repo: [`mind-map/MMD_Format.MD`](../mind-map/MMD_Format.MD). A short copy travels with the skill in [`mmd/reference.md`](mmd/reference.md).
