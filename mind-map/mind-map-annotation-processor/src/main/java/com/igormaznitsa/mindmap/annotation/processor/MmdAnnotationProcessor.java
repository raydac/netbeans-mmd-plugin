package com.igormaznitsa.mindmap.annotation.processor;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class MmdAnnotationProcessor extends AbstractProcessor {

  public static final String KEY_MMD_PACKAGE = "mmd.doc.package";
  public static final String KEY_MMD_FOLDER_TARGET = "mmd.doc.folder.target";
  public static final String KEY_MMD_FOLDER_CREATE = "mmd.doc.folder.create";
  public static final String KEY_MMD_RELATIVE_PATHS = "mmd.doc.path.relative";
  public static final String KEY_MMD_FILE_OVERWRITE = "mmd.doc.file.overwrite";
  private static final String MSG_PREFIX = "MMD: ";
  private static final Set<String> SUPPORTED_OPTIONS =
      Collections.unmodifiableSet(new HashSet<String>() {{
        add(KEY_MMD_FOLDER_TARGET);
        add(KEY_MMD_FOLDER_CREATE);
        add(KEY_MMD_RELATIVE_PATHS);
        add(KEY_MMD_FILE_OVERWRITE);
        add(KEY_MMD_PACKAGE);
      }});
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Collections.unmodifiableMap(new HashMap<String, Class<? extends Annotation>>() {{
        put(MmdTopic.class.getName(), MmdTopic.class);
        put(MmdFile.class.getName(), MmdFile.class);
      }});
  private Trees trees;
  private SourcePositions sourcePositions;
  private Filer filer;
  private Messager messager;
  private File optionTargetFolder;
  private boolean optionPreferRelativePaths;
  private boolean optionFileOverwrite;

  private String optionTargetPackage;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ANNOTATIONS.keySet();
  }

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.trees = Trees.instance(processingEnv);
    this.sourcePositions = this.trees.getSourcePositions();
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();

    this.optionTargetPackage =
        processingEnv.getOptions().getOrDefault(KEY_MMD_PACKAGE, "mmd").trim();

    if (processingEnv.getOptions().containsKey(KEY_MMD_FOLDER_TARGET)) {
      this.optionTargetFolder = new File(processingEnv.getOptions().get(KEY_MMD_FOLDER_TARGET));
      if (!this.optionTargetFolder.isDirectory()) {
        this.messager.printMessage(NOTE,
            MSG_PREFIX + "Can't find existing folder: " + this.optionTargetFolder);
        if (Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, "false"))) {
          if (this.optionTargetFolder.mkdirs()) {
            this.messager.printMessage(NOTE,
                MSG_PREFIX + "Successfully created target folder : " + this.optionTargetFolder);
          } else {
            this.messager.printMessage(ERROR,
                MSG_PREFIX + "Can't create requested target folder " + this.optionTargetFolder);
          }
        } else {
          this.messager.printMessage(ERROR,
              MSG_PREFIX + "Can't find folder (use " + KEY_MMD_FOLDER_CREATE +
                  " flag to make it): " +
                  this.optionTargetFolder);
        }
      }
      this.messager.printMessage(NOTE,
          String.format(MSG_PREFIX + "Target folder: %s", this.optionTargetFolder));
    }

    this.optionPreferRelativePaths = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_RELATIVE_PATHS, "true"));

    this.optionFileOverwrite = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_FILE_OVERWRITE, "true"));

    this.messager.printMessage(NOTE,
        String.format(MSG_PREFIX + "Target package: %s", this.optionTargetPackage));
    this.messager.printMessage(NOTE,
        String.format(MSG_PREFIX + "Prefer generate relative paths: %s",
            this.optionPreferRelativePaths));
  }

  private boolean write(final String name, final MindMap mindMap) {
    if (this.optionTargetFolder != null) {
      final String packagePath = optionTargetPackage.replace(".", File.pathSeparator);
      final File folder = new File(this.optionTargetFolder, packagePath);
      final File targetFile = new File(folder, name);

      if (!this.optionFileOverwrite && targetFile.isFile()) {
        this.messager.printMessage(WARNING,
            MSG_PREFIX + "File " + targetFile + " already exists, overwrite is disabled");
        return false;
      }

      if (!this.optionTargetFolder.isDirectory()) {
        this.messager.printMessage(ERROR,
            MSG_PREFIX + "Can't find target folder: " + this.optionTargetFolder);
        return false;
      }

      if (!folder.isDirectory() && folder.mkdirs()) {
        this.messager.printMessage(ERROR, MSG_PREFIX + "Can't create folder: " + folder);
        return false;
      }

      try (final Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile, false),
          StandardCharsets.UTF_8)) {
        writer.write(mindMap.packToString());
        this.messager.printMessage(NOTE,
            MSG_PREFIX + "Mind map file has been written: " + targetFile);
      } catch (IOException ex) {
        this.messager.printMessage(ERROR,
            MSG_PREFIX + "Can't write mind map file: " + targetFile + ", error: " +
                ex.getMessage());
        return false;
      }
    } else {
      try {
        final FileObject targetFile = this.filer.createResource(StandardLocation.SOURCE_OUTPUT,
            optionTargetPackage,
            name);

        if (!this.optionFileOverwrite && targetFile.getLastModified() != 0L) {
          this.messager.printMessage(WARNING,
              MSG_PREFIX + "File " + targetFile.getName() +
                  " already exists, overwrite is disabled");
          return false;
        }

        try (final Writer writer = new OutputStreamWriter(targetFile.openOutputStream(),
            StandardCharsets.UTF_8)) {
          writer.write(mindMap.packToString());
        }
        this.messager.printMessage(NOTE,
            MSG_PREFIX + "Mind map file has been written: " + targetFile.getName());
      } catch (IOException ex) {
        final String packageAsPath = this.optionTargetPackage.replace(".", "/") + name;
        this.messager.printMessage(ERROR,
            MSG_PREFIX + "Can't write mind map as resource, file: " + packageAsPath + ", error: " +
                ex.getMessage());
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<String> getSupportedOptions() {
    return SUPPORTED_OPTIONS;
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {

    final List<FoundMmdAnnotation> foundMmdAnnotationList = new ArrayList<>();

    for (final TypeElement annotation : annotations) {
      final Set<? extends Element> annotatedElements =
          roundEnv.getElementsAnnotatedWith(annotation);

      final Class<? extends Annotation> annotationClass =
          ANNOTATIONS.get(annotation.getQualifiedName().toString());
      requireNonNull(annotationClass,
          () -> "Unexpectedly annotation class not found for " + annotation.getQualifiedName());

      annotatedElements.forEach(x -> {
        final Annotation annotationInstance = x.getAnnotation(annotationClass);
        final UriLine position = findPosition(x);
        foundMmdAnnotationList.add(
            new FoundMmdAnnotation(annotationInstance, new File(position.uri),
                position.line));
      });
    }

    this.messager.printMessage(
        NOTE, format(MSG_PREFIX + "Detected %d annotated items", foundMmdAnnotationList.size()));

    if (!foundMmdAnnotationList.isEmpty()) {
      for (final Map.Entry<String, MindMap> e : new MmdSorter().sort(foundMmdAnnotationList)
          .entrySet()) {
        this.write(e.getKey(), e.getValue());
      }
    }

    return true;
  }

  private UriLine findPosition(final Element element) {
    final TreePath treePath = trees.getPath(element);
    final CompilationUnitTree compilationUnit = treePath.getCompilationUnit();
    final long startPosition =
        this.sourcePositions.getStartPosition(compilationUnit, treePath.getLeaf());
    final long lineNumber = compilationUnit.getLineMap().getLineNumber(startPosition);
    return new UriLine(compilationUnit.getSourceFile().toUri(), lineNumber);
  }

  private static final class UriLine {
    private final URI uri;
    private final long line;

    UriLine(final URI uri, final long line) {
      this.uri = requireNonNull(uri);
      this.line = line;
    }

  }
}
