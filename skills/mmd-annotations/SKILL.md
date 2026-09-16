---
name: mmd-annotations
description: >-
  Mark Java sources with Scia Reto MMD annotations (@MmdFile, @MmdTopic,
  @MmdFileRef, @HasMmdMarkedElements) so the annotation processor generates
  mind maps. Always attach mind-map-annotations and the processor to the user's
  project and audit compiler options before marking. Use when the user asks to
  annotate a project for mind maps, generate .mmd from Java, document architecture
  as maps for agents, or runs /mmd-annotations:setup or /mmd-annotations:mark.
---

# MMD Java annotations

Mark Java so `mind-map-annotation-processor` **generates** `.mmd` maps. Do **not** freehand-write those maps. After compile, use the [`mmd`](../mmd/SKILL.md) skill only if you need to parse or check the generated file.

This folder is self-contained: `SKILL.md`, [reference.md](reference.md), [examples.md](examples.md), `commands/`. Copy the whole folder.

Package: `com.igormaznitsa.mindmap.annotations`. Retention is `SOURCE` — no runtime cost.

## When to mark

Build maps that a later agent can read as an **index of the system**, not a dump of every identifier.

Mark:

- Public types that own a boundary (entry point, service, model root, SPI)
- Methods that encode a policy, lifecycle step, or collaboration
- Fields that are the actual stored state of that type
- Cross-links (`uid` / `jumpTo`) where control or data jumps between types

Do not mark:

- Getters, setters, trivial constructors, equals/hashCode/toString
- Generated sources, tests (unless the map is about test strategy)
- Every local variable — only the rare step that is otherwise invisible

Titles are **roles**. Notes are **why / invariant / gotcha**. `anchor` stays `true` (default) so generated topics keep a file:line extra back to the source.

## Workflow

Do **not** edit Java sources until **Project setup** below has passed for every module you will mark. Marks without a wired processor never become maps.

1. Read this file. Open [reference.md](reference.md) for attributes, processor options, and the build-audit table. Open [examples.md](examples.md) for copy-paste shapes.
2. Scope: files or modules the user named. If none, ask. Do not annotate the whole tree “just in case”.
3. **Project setup (mandatory).** Attach artifacts and audit compiler options. Fix gaps. See below.
4. Choose **one `@MmdFile` per map** (one bounded context / module). Give it a stable `uid` and a `fileName`. Point `folder` or `-Ammd.target.folder` at a docs folder when generated maps must not sit next to `.java`.
5. Annotate types and members. Every topic must resolve to exactly one file (`@MmdFile` on the type, `@MmdFileRef`, or `fileUid`). Ambiguous files fail the compile.
6. Compile so the processor writes maps. Fix processor errors; do not invent `.mmd` by hand.
7. If the user wants a sanity check, parse the generated `.mmd` with the `mmd` skill. Commit generated maps only when the user wants them in git (they are regenerated on each compile; overwrite defaults to true).

```bash
mvn22 -pl <module> -am compile
```

## Project setup (mandatory)

**Minimum before any `@Mmd*` on user sources:** every module you will mark must compile against `mind-map-annotations`, and its Java compile must run `mind-map-annotation-processor`. Then check that compiler options actually let the processor write maps.

Tell the user what you found and what you changed. Do not skip a failing check.

### 1. Attach artifacts

For each module in scope (Maven `pom.xml`, Gradle `build.gradle` / `.kts`, or javac/`build.xml`):

| Artifact | How it must be attached |
|----------|-------------------------|
| `com.igormaznitsa:mind-map-annotations` | Compile classpath, **`provided` / `compileOnly`**. Needed to import `@MmdFile` / `@MmdTopic`. Never `runtime` / shipped in the app. |
| `com.igormaznitsa:mind-map-annotation-processor` | **Annotation-processor path only** (`annotationProcessorPaths` or Gradle `annotationProcessor`). Do not put it on the app runtime classpath. |

Version: reuse the version already in the project. This repository uses the parent `mind-map` version (`project.version`). Otherwise pick one published `com.igormaznitsa` pair and keep annotations + processor **the same version**.

If the module already depends on `mind-map-annotations` (this repo’s own modules may), keep that; still add or confirm the processor path. Do **not** enable the processor on `mind-map-annotations`, `mind-map-annotation-processor`, or any module that sets `-proc:none` on purpose.

If wiring is missing, add it. Do not only mention it in chat.

### 2. Audit compiler options

Read the **effective** compile config (module POM plus parent `pluginManagement`, Gradle `compileJava`, IDEA annotation-processing panel if that is how they build). Check every row:

| Check | Fail if | Fix |
|-------|---------|-----|
| Processing is on | `-proc:none`, `options.compilerArgs` contains `-proc:none`, Gradle `options.annotationProcessorPath` empty while processing is disabled, IDE “Enable annotation processing” off for the module they compile in | Remove `-proc:none` from **this** module (do not strip it from the processor/annotations modules themselves). Turn processing on. |
| Processor is selected | Maven `annotationProcessorPaths` is set but does not list `mind-map-annotation-processor` (Maven then **ignores** processors on the compile classpath) | Add the processor path entry, or drop the empty/other-only `annotationProcessorPaths` and use a dedicated path that includes this processor. |
| Processor is not on the app classpath | Processor listed as a normal `compile`/`implementation` dependency of a shipped module | Move it to processor path; keep annotations `provided`/`compileOnly`. |
| Versions match | Annotations and processor versions differ | Align both. |
| FILE extras are portable | Anchors stay default `true` but `-Ammd.file.link.base.folder` is unset | Set it to the module or project root (`${project.basedir}` / `project.projectDir`). |
| Output folder can be created | `-Ammd.target.folder` (or `@MmdFile.folder` outside the source tree) points at a missing dir and `-Ammd.folder.create` is not `true` | Add `-Ammd.folder.create=true` or create the directory. |
| Comment marks | Sources will use `//@MmdTopic` but `-Ammd.comment.scan` is not `true` | Add `-Ammd.comment.scan=true`. |
| Headless PNG/SVG | `-Ammd.target.format` includes `PNG` or `SVG` on CI without `-J-Djava.awt.headless=true` | Add the JVM flag, or keep format `MMD` (default). |
| Write root | Generated path would fall outside `-Ammd.file.root.folder` / the link-base folder | Point `folder` / `mmd.target.folder` inside that root, or widen the option. |
| `mmd.dry.start` | Set to `true` | Turn it off unless the user asked for a dry run. |

Default format is `MMD`. Do not add extra export formats unless the user asked.

Full option list and Maven/Gradle snippets: [reference.md](reference.md#processor-options).

### 3. Confirm, then mark

Compile the wired module once after setup (even before new marks). A missing artifact or `-proc:none` must fail here, not after a pile of annotations.

Then continue the workflow from choosing `@MmdFile`.

## Annotation map

| Annotation | Role |
|------------|------|
| `@MmdFile` | Declares an output map on a type. Repeatable as `@MmdFiles`. |
| `@MmdFileRef` | Points a type/member at an existing `@MmdFile` (`uid` wins over `target`). |
| `@MmdTopic` | One topic. Empty `title` → element simple name. Repeatable as `@MmdTopics`. |
| `@HasMmdMarkedElements` | On a **method or constructor**: also harvest inner `@MmdTopic` (locals) and, if `mmd.comment.scan=true`, `//@MmdTopic` comments. |

`@MmdFile` / `@MmdFiles` only on types. `@MmdTopic` may sit on types, fields, methods, constructors, parameters, locals, packages. `@MmdFileRef` is **not** valid on locals.

## Defaults that matter

- Output folder: the annotated class’s source folder (`${mmd.src.class.folder}`), unless `folder` or `-Ammd.target.folder` is set.
- Output name: `fileName`, else the `.java` file name without extension.
- `anchor=true`: FILE extra = source path + line (needs `-Ammd.file.link.base.folder` for portable relative links).
- `fileLink` overrides `anchor`.
- `path = {"a","b"}`: create/find those ancestors (UID match first, else title). Root topic `path` must be empty.
- `jumpTo`: UID of another topic, or its title if no UID matches.
- `order`: sibling sort; default `-1`.
- `substitute=true`: replace `${name}` from JVM system properties, then processor options. Unknown names fail the compile. `folder` also understands `${mmd.src.class.folder}` without `substitute`.

## File targeting

A topic belongs to a file if, in order:

1. `@MmdTopic(fileUid=…)` matches a `@MmdFile(uid=…)` (or that file’s `fileName` / source base name).
2. Else a `@MmdFileRef` / enclosing `@MmdTopic(fileUid)` on the type.
3. Else a single `@MmdFile` found on the type or its ancestors.

If several `@MmdFile` sit on the same type and the topic has no `fileUid`, processing **fails**. Always set `fileUid` when a type declares more than one map.

## In-method marks

```java
@HasMmdMarkedElements
void connect() {
  @MmdTopic(title = "open socket")
  final Socket socket = this.open();

  //@MmdTopic(colorFill=Yellow) handshake
  this.handshake(socket);
}
```

Comment marks are harvested only when **both** `@HasMmdMarkedElements` and `-Ammd.comment.scan=true` are set. Only `//` comments; not inside strings or block comments. Case-insensitive `@MmdTopic`. See [reference.md](reference.md#comment-marks).

A method **without** `@HasMmdMarkedElements` ignores inner locals and comments (the method’s own `@MmdTopic` still counts).

## Maven / Gradle snippets

See [reference.md](reference.md#maven-compiler-snippet) (Maven) and [Gradle](reference.md#gradle). Minimum compiler args:

```
-Ammd.file.link.base.folder=<module-root>
-Ammd.folder.create=true
```

Reference wiring in this repository: `mind-map/mind-map-annotation-processor-it/pom.xml`.

## Agent map style

- One map per story. Depth via `path`, not a flat list of class names.
- Stable `uid` on anything you `jumpTo` later. Do not jump to a title that will be renamed.
- Short `note` (contract, thread rule, “do not call from EDT”). Multi-line is allowed.
- Small color set if you color at all (`MmdColor`). Skip emoticons unless they carry meaning; ids are `MmdEmoticon` names.
- `direction = LEFT` only for first-level “other side of the root” groups.
- `collapse = true` on implementation subtrees that agents should open only when needed.

## Commands

After this folder is installed as a Cursor skill:

| Command | Does |
|---------|------|
| `/mmd-annotations:setup` | Attach artifacts and audit compiler options. No source marks. |
| `/mmd-annotations:mark` | Setup gate, then mark Java and compile so maps are generated. |

Other agents: name the command or paste `commands/<name>.md`. Extra words after the command are the module/path scope.

Generated `.mmd` files are then ordinary maps: use [`mmd`](../mmd/SKILL.md) (`/mmd:read`, `/mmd:validate`).
