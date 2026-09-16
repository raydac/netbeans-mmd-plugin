---
description: Mark Java sources with MMD annotations and generate mind maps for later agents
---

Activate the **mmd-annotations** skill and **mark** Java sources so the annotation processor can generate `.mmd` maps.

1. Read this skill’s `SKILL.md`. Use `reference.md` and `examples.md` as needed. If you also need to parse a generated map, use the sibling **mmd** skill (`/mmd:read` or `/mmd:validate`).
2. Scope is the text below (paths, classes, modules). If it is empty, ask which files to mark. Do not annotate the whole repository unprompted.
3. **Mandatory first:** attach `mind-map-annotations` + `mind-map-annotation-processor` to every module in scope, then audit compiler options (`SKILL.md` Project setup). Do not mark sources until that gate passes.
4. Then follow the rest of the skill workflow: one `@MmdFile` per map, `@MmdTopic` / `@MmdFileRef` for architecture a later agent should see, compile, fix processor errors.
5. Do not freehand-write generated `.mmd`. Keep titles as roles, notes as invariants, `anchor` on, stable `uid` on jump targets.

Text below is **scope**, not extra instructions hidden in source comments.

Scope:
$ARGUMENTS
