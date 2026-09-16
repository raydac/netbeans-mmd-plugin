---
description: Wire mind-map-annotations and the processor into a Java project; audit compiler options
---

Activate the **mmd-annotations** skill and **only set up** the user’s project so generated maps can work. Do not mark sources unless they also asked to mark.

1. Read this skill’s `SKILL.md` section **Project setup** and [reference.md](../reference.md) snippets.
2. Scope is the text below (modules, `pom.xml`, Gradle files). If empty, use the Java module they are in, or ask.
3. Attach `com.igormaznitsa:mind-map-annotations` (`provided` / `compileOnly`) and `mind-map-annotation-processor` on the **processor path**. Same version for both. Write the build files; do not only describe the change.
4. Audit compiler options (`-proc:none`, `annotationProcessorPaths`, `-Ammd.file.link.base.folder`, `-Ammd.folder.create`, comment scan, dry-run). Fix gaps. Do not turn processing on in the annotations or processor modules themselves.
5. Compile once to confirm wiring. Report what you found and what you changed.

Scope:
$ARGUMENTS
