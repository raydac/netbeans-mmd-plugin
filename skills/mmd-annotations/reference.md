# MMD annotation reference

Sources of truth: `mind-map-annotations` and `MmdAnnotationProcessor`. This file is a working subset for agents.

## Artifacts

| Artifact | Role |
|----------|------|
| `com.igormaznitsa:mind-map-annotations` | Annotations. Java 8 bytecode. `provided` at compile. |
| `com.igormaznitsa:mind-map-annotation-processor` | Processor (`MmdAnnotationProcessor`). Java 9+. Discovers via `META-INF/services/javax.annotation.processing.Processor`. |

Version: use the project’s existing version. In this repository that is the parent `mind-map` version.

## `@MmdFile`

Target: `TYPE`, `ANNOTATION_TYPE`. Repeatable: `@MmdFiles` (`@Inherited` on the container).

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `uid` | `""` | Stable id for `@MmdFileRef` / `fileUid`. Empty → internal auto id (not useful to reference). |
| `fileName` | `""` | Output base name, may include path segments. Empty → source file name without `.java`. |
| `folder` | `${mmd.src.class.folder}` | Output directory. Macro expands to the annotated type’s source folder. Overridden by `-Ammd.target.folder`. |
| `rootTopic` | `@MmdTopic` | Root node. Its `path` **must** be empty. Empty root `title` → the type’s simple name. |
| `substitute` | `false` | `${…}` replacement in this annotation’s text fields. |

Constant: `MmdFile.MACROS_SRC_CLASS_FOLDER` = `"${mmd.src.class.folder}"`.

## `@MmdFileRef`

Target: type, field, method, constructor, annotation type, package, type parameter, parameter, type use. **Not** local variables.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `uid` | `""` | `@MmdFile.uid()` (or, with a warning, that file’s `fileName` / source base name). Non-empty **wins** over `target`. |
| `target` | `MmdFileRef.class` | Class that carries `@MmdFile` / `@MmdFiles`. First match in hierarchy. Default means “unset”. |
| `substitute` | `false` | `${…}` in `uid`. |

At least one of `uid` or a real `target` is required.

## `@MmdTopic`

Repeatable: `@MmdTopics`. Empty `title` → `element.getSimpleName()`.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `title` | `""` | Topic text. |
| `uid` | `""` | Jump target id (`jumpTo` / `path` items). |
| `fileUid` | `""` | Owning `@MmdFile.uid()` (or file name / base name). |
| `path` | `{}` | Ancestors from root: each item is a UID, else a title (missing titles are created). |
| `emoticon` | `EMPTY` | `MmdEmoticon` constant. |
| `fileLink` | `""` | FILE extra. `path` or `path:line`. Overrides `anchor`. |
| `anchor` | `true` | If `fileLink` empty, FILE extra = this source file + line. |
| `jumpTo` | `""` | TOPIC extra: target UID, else title. |
| `note` | `""` | NOTE extra. `\n` for new lines. |
| `uri` | `""` | LINK extra. |
| `colorText` / `colorFill` / `colorBorder` | `Default` | `MmdColor`. Unset colors inherit from ancestors when painting. |
| `collapse` | `false` | Start collapsed. |
| `direction` | `AUTO` | `LEFT` / `RIGHT` / `AUTO`. `LEFT` is applied from the root first-level side. |
| `order` | `-1` | Sibling order (lower first). |
| `substitute` | `false` | `${…}` in text fields. |

## `@HasMmdMarkedElements`

Target: `TYPE`, `METHOD`, `CONSTRUCTOR`. The processor only walks **executable** elements. Marking a type alone logs a warning and does nothing extra.

Enables:

1. `@MmdTopic` on locals (and nested locals) inside that method/constructor.
2. `//@MmdTopic` line comments, only if `-Ammd.comment.scan=true`.

## Comment marks

Pattern (case-insensitive), on a `//` line after stripping `//`:

```
@MmdTopic <title>
@MmdTopic(<args>) <optional title>
```

`<args>` is a comma-separated `key=value` list. Keys are case-insensitive and match annotation attributes (`title`, `uid`, `fileUid`, `path`, `emoticon`, `fileLink`, `anchor`, `jumpTo`, `note`, `uri`, `colorText`, `colorFill`, `colorBorder`, `collapse`, `substitute`, `direction`, `order`). Values: unquoted token, `"escaped string"`, `{a,b}` for `path`. Enums accept `Yellow`, `MmdColor.Yellow`, `LEFT`, `Direction.LEFT`.

Ignored: `//` inside strings, and any `//@MmdTopic` buried in `/* */`.

## Substitution

When `substitute=true`, `${name}` is resolved from:

1. JVM system properties (`os.name`, `user.home`, …)
2. Processor option keys (`mmd.file.link.base.folder`, `mmd.target.folder`, `mmd.file.root.folder`, …)

Unknown names abort processing. Escape `$` as `$$` if needed. Nested `${…}` is enabled.

## Processor options

Passed as `-Akey=value` (Maven: `<compilerArg>-Akey=value</compilerArg>`).

| Key | Default | Meaning |
|-----|---------|---------|
| `mmd.target.folder` | unset | Force all output into this directory (overrides `@MmdFile.folder`). |
| `mmd.folder.create` | `false` | Create `mmd.target.folder` if missing. |
| `mmd.file.overwrite` | `true` | Overwrite existing output. |
| `mmd.dry.start` | `false` | Run without writing files. |
| `mmd.file.link.base.folder` | unset | Base for relative FILE extras. Also used as write-root limit when `mmd.file.root.folder` is unset. |
| `mmd.file.root.folder` | (link base if set) | Refuse to write outside this directory. |
| `mmd.target.format` | `MMD` | Comma/`;`/`:` list: `MMD`, `ASCIIDOC`, `FREEMIND`, `MD`, `MINDMUP`, `ORGMODE`, `PNG`, `PLANTUML`, `SVG`, `TXT`. |
| `mmd.comment.scan` | `false` | Harvest `//@MmdTopic` inside `@HasMmdMarkedElements` methods. |

PNG/SVG exporters use Swing. Pass `-J-Djava.awt.headless=true` for CI.

## Build audit (agent)

Before marking sources, walk the module’s POM/Gradle (and parents). Attach artifacts if missing, then apply the checklist in `SKILL.md` **Project setup**.

Do not enable the processor on modules that compile the annotations or the processor themselves (`-proc:none` there is required).

Maven: if `annotationProcessorPaths` is present, **only** those artifacts are processors. A path list for Lombok (or anything else) without `mind-map-annotation-processor` means MMD marks compile as unknown annotations or are ignored.

Inherited `-proc:none` or an empty processor path is a fail. Fix the **leaf module** that you will annotate; do not weaken a sibling that must stay `proc:none`.

## Maven compiler snippet

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <annotationProcessorPath>
        <groupId>com.igormaznitsa</groupId>
        <artifactId>mind-map-annotation-processor</artifactId>
        <version>${mmd.annotations.version}</version>
      </annotationProcessorPath>
    </annotationProcessorPaths>
    <compilerArgs>
      <compilerArg>-Ammd.file.link.base.folder=${project.basedir}</compilerArg>
      <compilerArg>-Ammd.folder.create=true</compilerArg>
    </compilerArgs>
  </configuration>
</plugin>
```

```xml
<dependency>
  <groupId>com.igormaznitsa</groupId>
  <artifactId>mind-map-annotations</artifactId>
  <version>${mmd.annotations.version}</version>
  <scope>provided</scope>
</dependency>
```

## Gradle

```kotlin
val mmdVersion = "…" // same version on both artifacts

dependencies {
    compileOnly("com.igormaznitsa:mind-map-annotations:$mmdVersion")
    annotationProcessor("com.igormaznitsa:mind-map-annotation-processor:$mmdVersion")
}

tasks.compileJava {
    options.compilerArgs.add("-Ammd.file.link.base.folder=${project.projectDir}")
    options.compilerArgs.add("-Ammd.folder.create=true")
}
```

Groovy is the same coordinates on `compileOnly` / `annotationProcessor` and `compileJava.options.compilerArgs`.

## javac

```
javac -processorpath mind-map-annotation-processor.jar \
  -Ammd.file.link.base.folder=/path/to/module \
  -Ammd.folder.create=true \
  … sources …
```

`mind-map-annotations` must still be on `-classpath` (or `--class-path`) so the annotations resolve.

## Live fixtures

`mind-map/mind-map-annotation-processor-it/src/main/java/com/igormaznitsa/mindmap/annoit/` — files, paths, attributes, collectors, linking, in-method comments.
