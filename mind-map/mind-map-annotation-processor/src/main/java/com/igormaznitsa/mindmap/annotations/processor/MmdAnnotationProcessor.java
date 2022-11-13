package com.igormaznitsa.mindmap.annotations.processor;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdFiles;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils;
import com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.UriLine;
import com.igormaznitsa.mindmap.annotations.processor.builder.MmdFileBuilder;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
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
          MmdFile.class.getName(), MmdFile.class,
          MmdFileLink.class.getName(), MmdFileLink.class);
  private Trees trees;
  private SourcePositions sourcePositions;
  private Messager messager;
  private Elements elements;
  private Types types;
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
    this.elements = processingEnv.getElementUtils();
    this.types = processingEnv.getTypeUtils();

    this.optionDryStart =
        Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(KEY_MMD_DRY_START, "false"));

    if (this.optionDryStart) {
      this.messager.printMessage(WARNING, "MMD processor started in DRY mode");
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_TARGET_FOLDER)) {
      this.optionTargetFolder = Paths.get(processingEnv.getOptions().get(KEY_MMD_TARGET_FOLDER));
      if (!(Files.isDirectory(this.optionTargetFolder) || this.optionDryStart)) {
        this.messager.printMessage(
            WARNING, "Folder for MMD not-exists: " + this.optionTargetFolder);
        if (Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, "false"))) {
          try {
            this.optionTargetFolder = Files.createDirectories(this.optionTargetFolder);
            this.messager.printMessage(
                NOTE, "Folder for MMD files successfully created: " + this.optionTargetFolder);
          } catch (IOException ex) {
            this.messager.printMessage(
                ERROR, "Can't create folder to write MMD files: " + this.optionTargetFolder);
          }
        } else {
          this.messager.printMessage(
              ERROR,
              "Can't find folder for MMD files (use "
                  + KEY_MMD_FOLDER_CREATE
                  + " flag to make it): "
                  + this.optionTargetFolder);
        }
      }
      if (this.optionTargetFolder != null) {
        this.messager.printMessage(
            WARNING,
            String.format(
                "Directly provided target folder to write MMD files: %s", this.optionTargetFolder));
      }
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_FILE_LINK_BASE_FOLDER)) {
      this.optionFileLinkBaseFolder =
          Paths.get(
              FilenameUtils.normalizeNoEndSeparator(
                  processingEnv.getOptions().get(KEY_MMD_FILE_LINK_BASE_FOLDER)));
      this.messager.printMessage(
          NOTE,
          String.format("File link base folder for MMD files: %s", this.optionFileLinkBaseFolder));
    }

    this.optionFileOverwrite =
        Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FILE_OVERWRITE, "true"));
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    final List<FoundMmdAnnotation> foundAnnotationList = new ArrayList<>();

    for (final TypeElement annotation : annotations) {
      final Set<? extends Element> annotatedElements =
          roundEnv.getElementsAnnotatedWith(annotation);

      final Class<? extends Annotation> annotationClass =
          ANNOTATIONS.get(annotation.getQualifiedName().toString());
      requireNonNull(
          annotationClass,
          () -> "Unexpectedly annotation class not found for " + annotation.getQualifiedName());

      annotatedElements.forEach(
          element -> {
            final Annotation[] annotationInstances = element.getAnnotationsByType(annotationClass);
            final UriLine position =
                AnnotationUtils.findPosition(this.sourcePositions, this.trees, element);

            if (annotationClass == MmdFiles.class) {
              Arrays.stream(annotationInstances)
                  .map(x -> (MmdFiles) x)
                  .flatMap(x -> Stream.of(x.value()))
                  .forEach(
                      file -> foundAnnotationList.add(
                          new FoundMmdAnnotation(
                              element, file, new File(position.getUri()).toPath(),
                              position.getLine())));
            } else {
              Arrays.stream(annotationInstances)
                  .forEach(
                      instance -> foundAnnotationList.add(
                          new FoundMmdAnnotation(
                              element, instance, new File(position.getUri()).toPath(),
                              position.getLine())));
            }
          });
    }


    if (foundAnnotationList.isEmpty()) {
      this.messager.printMessage(
          NOTE,
          "There is no any found MMD annotation");
    } else {
      this.messager.printMessage(
          NOTE,
          format(
              "MMD annotation processor has found %d annotations to process",
              foundAnnotationList.size()));

      foundAnnotationList.sort(
          (o1, o2) ->
              o1.getElement().getSimpleName().toString().compareTo(o2.getElement().toString()));

      final MmdFileBuilder fileBuilder = MmdFileBuilder.builder()
          .setMessager(this.messager)
          .setElements(this.elements)
          .setTypes(this.types)
          .setTargetFolder(this.optionTargetFolder)
          .setDryStart(this.optionDryStart)
          .setOverwriteAllowed(this.optionFileOverwrite)
          .setFileLinkBaseFolder(this.optionFileLinkBaseFolder)
          .setAnnotations(foundAnnotationList)
          .build();


      fileBuilder.write();
    }

    return true;
  }

}
