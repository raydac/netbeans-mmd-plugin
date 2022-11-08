package com.igormaznitsa.mindmap.annotation.processor;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.annotation.processor.creator.MmdFileCreator;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdFiles;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.apache.commons.io.FilenameUtils;

@SupportedOptions({
    MmdAnnotationProcessor.KEY_MMD_TARGET_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_FOLDER_CREATE,
    MmdAnnotationProcessor.KEY_MMD_DRY_START,
    MmdAnnotationProcessor.KEY_MMD_FILE_OVERWRITE,
    MmdAnnotationProcessor.KEY_MMD_FILE_LINK_BASE_FOLDER
})
public class MmdAnnotationProcessor extends AbstractProcessor {

  public static final String KEY_MMD_TARGET_FOLDER = "mmd.target.folder";
  public static final String KEY_MMD_FILE_LINK_BASE_FOLDER = "mmd.file.link.base.folder";
  public static final String KEY_MMD_DRY_START = "mmd.dry.start";
  public static final String KEY_MMD_FOLDER_CREATE = "mmd.folder.create";
  public static final String KEY_MMD_FILE_OVERWRITE = "mmd.file.overwrite";
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Map.of(
          MmdTopic.class.getName(), MmdTopic.class,
          MmdFiles.class.getName(), MmdFiles.class,
          MmdFile.class.getName(), MmdFile.class
      );
  private Trees trees;
  private SourcePositions sourcePositions;
  private Messager messager;
  private Path optionTargetFolder;
  private Path optionFileLinkBaseFolder;
  private boolean optionFileOverwrite;
  private boolean optionDryStart;

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

    this.optionDryStart = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_DRY_START, "false"));

    if (this.optionDryStart) {
      this.messager.printMessage(WARNING,
          "MMD processor started in DRY mode");
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_TARGET_FOLDER)) {
      this.optionTargetFolder = Paths.get(processingEnv.getOptions().get(KEY_MMD_TARGET_FOLDER));
      if (!(Files.isDirectory(this.optionTargetFolder) || this.optionDryStart)) {
        this.messager.printMessage(WARNING,
            "Folder for MMD not-exists: " + this.optionTargetFolder);
        if (Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, "false"))) {
          try {
            this.optionTargetFolder = Files.createDirectories(this.optionTargetFolder);
            this.messager.printMessage(NOTE,
                "Folder for MMD files successfully created: " + this.optionTargetFolder);
          } catch (IOException ex) {
            this.messager.printMessage(ERROR,
                "Can't create folder to write MMD files: " + this.optionTargetFolder);
          }
        } else {
          this.messager.printMessage(ERROR,
              "Can't find folder for MMD files (use " + KEY_MMD_FOLDER_CREATE +
                  " flag to make it): " +
                  this.optionTargetFolder);
        }
      }
      if (this.optionTargetFolder != null) {
        this.messager.printMessage(WARNING,
            String.format("Directly provided target folder to write MMD files: %s",
                this.optionTargetFolder));
      }
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_FILE_LINK_BASE_FOLDER)) {
      this.optionFileLinkBaseFolder = Paths.get(FilenameUtils.normalizeNoEndSeparator(
          processingEnv.getOptions().get(KEY_MMD_FILE_LINK_BASE_FOLDER)));
      this.messager.printMessage(NOTE,
          String.format("File link base folder for MMD files: %s", this.optionFileLinkBaseFolder));
    }

    this.optionFileOverwrite = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(
        KEY_MMD_FILE_OVERWRITE, "true"));
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {

    final List<MmdAnnotation> mmdAnnotationList = new ArrayList<>();

    for (final TypeElement annotation : annotations) {
      final Set<? extends Element> annotatedElements =
          roundEnv.getElementsAnnotatedWith(annotation);

      final Class<? extends Annotation> annotationClass =
          ANNOTATIONS.get(annotation.getQualifiedName().toString());
      requireNonNull(annotationClass,
          () -> "Unexpectedly annotation class not found for " + annotation.getQualifiedName());

      annotatedElements.forEach(element -> {
        final Annotation[] annotationInstances = element.getAnnotationsByType(annotationClass);
        final UriLine position = findPosition(element);

        if (annotationClass == MmdFiles.class) {
          Arrays.stream(annotationInstances)
              .flatMap(x -> Arrays.stream(((MmdFiles) x).value()))
              .forEach(mmdFile -> {
                mmdAnnotationList.add(
                    new MmdAnnotation(element, mmdFile, new File(position.uri).toPath(),
                        position.line));
              });
        } else {
          Arrays.stream(annotationInstances)
              .forEach(instance -> {
                mmdAnnotationList.add(
                    new MmdAnnotation(element, instance, new File(position.uri).toPath(),
                        position.line));
              });
        }
      });
    }

    this.messager.printMessage(
        NOTE, format("Detected %d annotated items to be used for MMD", mmdAnnotationList.size()));

    if (!mmdAnnotationList.isEmpty()) {
      mmdAnnotationList.sort(
          (o1, o2) -> o1.getElement()
              .getSimpleName()
              .toString()
              .compareTo(o2.getElement().toString())
      );

      MmdFileCreator.builder()
          .setMessager(this.messager)
          .setTargetFolder(this.optionTargetFolder)
          .setDryStart(this.optionDryStart)
          .setOverwriteAllowed(this.optionFileOverwrite)
          .setFileLinkBaseFolder(this.optionFileLinkBaseFolder)
          .setAnnotations(mmdAnnotationList)
          .build().process();
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
