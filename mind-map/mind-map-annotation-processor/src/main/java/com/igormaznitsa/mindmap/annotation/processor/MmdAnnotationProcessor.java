package com.igormaznitsa.mindmap.annotation.processor;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class MmdAnnotationProcessor extends AbstractProcessor {

  public static final String KEY_MMD_FOLDER_TARGET = "mmd.doc.folder.target";
  public static final String KEY_MMD_FOLDER_CREATE = "mmd.doc.folder.create";
  public static final String KEY_MMD_RELATIVE_PATHS = "mmd.doc.path.relative";

  private Trees trees;
  private SourcePositions sourcePositions;
  private static final Set<String> SUPPORTED_OPTIONS =
      Collections.unmodifiableSet(new HashSet<String>() {{
        add(KEY_MMD_FOLDER_TARGET);
        add(KEY_MMD_FOLDER_CREATE);
        add(KEY_MMD_RELATIVE_PATHS);
      }});
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Collections.unmodifiableMap(new HashMap<String, Class<? extends Annotation>>() {{
        put(MmdTopic.class.getName(), MmdTopic.class);
        put(MmdFile.class.getName(), MmdFile.class);
      }});
  private Messager messager;
  private File optionTargetFolder;
  private boolean optionPreferRelativePaths;

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

    this.optionTargetFolder = new File(processingEnv.getOptions().getOrDefault(
        KEY_MMD_FOLDER_TARGET, "." + File.separatorChar));
    if (!this.optionTargetFolder.isDirectory()) {
      this.messager.printMessage(WARNING, "Can't find existing folder: " + this.optionTargetFolder);
      if (Boolean.parseBoolean(
          processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, "false"))) {
        if (this.optionTargetFolder.mkdirs()) {
          this.messager.printMessage(NOTE,
              "Successfully created target folder : " + this.optionTargetFolder);
        } else {
          throw new IllegalStateException("Can't create target folder " + this.optionTargetFolder);
        }
      } else {
        throw new IllegalStateException(
            "Can't find folder (use " + KEY_MMD_FOLDER_CREATE + " flag to make it): " +
                this.optionTargetFolder);
      }
    }
    this.optionPreferRelativePaths = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_RELATIVE_PATHS, "true"));

    this.messager.printMessage(NOTE,
        String.format("MMD: Target folder: %s", this.optionTargetFolder));
    this.messager.printMessage(NOTE,
        String.format("MMD: Prefer generate relative paths: %s", this.optionPreferRelativePaths));
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
        NOTE, format("MMD: Detected %d annotated items", foundMmdAnnotationList.size()));

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
