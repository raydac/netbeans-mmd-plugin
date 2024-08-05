/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.annotations.processor;

import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findAllInternalMmdTopicAnnotations;
import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findMmdComments;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.igormaznitsa.mindmap.annotations.HasMmdMarkedElements;
import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdFiles;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.MmdTopics;
import com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils;
import com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.UriLine;
import com.igormaznitsa.mindmap.annotations.processor.builder.MmdFileBuilder;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import com.igormaznitsa.mindmap.annotations.processor.exporters.MmdExporter;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Annotation processor to collect MMD annotations and build MMD mind map files for them.
 */
@SupportedOptions({
    MmdAnnotationProcessor.KEY_MMD_TARGET_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_FOLDER_CREATE,
    MmdAnnotationProcessor.KEY_MMD_DRY_START,
    MmdAnnotationProcessor.KEY_MMD_FILE_OVERWRITE,
    MmdAnnotationProcessor.KEY_MMD_FILE_ROOT_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_FILE_LINK_BASE_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_TARGET_FORMAT,
    MmdAnnotationProcessor.KEY_MMD_COMMENT_SCAN
})
public class MmdAnnotationProcessor extends AbstractProcessor {

  /**
   * Option to define target format for generated files, the default value is MMD
   *
   * @since 1.6.8
   * @see MmdExporter
   */
  public static final String KEY_MMD_TARGET_FORMAT = "mmd.target.format";
  /**
   * Option to force target folder to place all generated MMD files.
   */
  public static final String KEY_MMD_TARGET_FOLDER = "mmd.target.folder";
  /**
   * Option to provide base folder to build relative file links.
   */
  public static final String KEY_MMD_FILE_LINK_BASE_FOLDER = "mmd.file.link.base.folder";
  /**
   * Option to provide a folder to play role as limit to create new files, if attempt to create a file outbound of the folder then error.
   */
  public static final String KEY_MMD_FILE_ROOT_FOLDER = "mmd.file.root.folder";
  /**
   * Option to define dry start flag which disable write of result MMD files but all other operations and notifications will be processed.
   */
  public static final String KEY_MMD_DRY_START = "mmd.dry.start";
  /**
   * Option to allow to create required folders during write operations.
   */
  public static final String KEY_MMD_FOLDER_CREATE = "mmd.folder.create";
  /**
   * Option to turn on comment scanning during marked method processing.
   *
   * @since HasMmdMarkedElements
   * @since 1.6.6
   */
  public static final String KEY_MMD_COMMENT_SCAN = "mmd.comment.scan";
  /**
   * Option to overwrite result MMD file if already exist
   */
  public static final String KEY_MMD_FILE_OVERWRITE = "mmd.file.overwrite";
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Map.of(
          MmdTopic.class.getName(), MmdTopic.class,
          MmdTopics.class.getName(), MmdTopics.class,
          MmdFiles.class.getName(), MmdFiles.class,
          MmdFile.class.getName(), MmdFile.class,
          MmdFileRef.class.getName(), MmdFileRef.class,
          HasMmdMarkedElements.class.getName(), HasMmdMarkedElements.class);
  private Trees trees;
  private SourcePositions sourcePositions;
  private Messager messager;
  private Types types;
  private MmdExporter exporter;
  private Path optionTargetFolder;
  private Path optionFileLinkBaseFolder;
  private Path optionFileRootFolder;
  private boolean optionFileOverwrite;
  private boolean optionDryStart;
  private boolean optionCommentScan;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
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
    this.types = processingEnv.getTypeUtils();

    this.optionDryStart =
        Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_DRY_START, FALSE.toString()));
    this.optionCommentScan =
        Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_COMMENT_SCAN, FALSE.toString()));
    if (this.optionDryStart) {
      this.messager.printMessage(WARNING, "MMD processor started in DRY mode");
    }

    this.exporter = MmdExporter.MMD;
    if (processingEnv.getOptions().containsKey(KEY_MMD_TARGET_FORMAT)) {
      final String targetFormat = processingEnv.getOptions()
          .getOrDefault(KEY_MMD_TARGET_FORMAT, MmdExporter.MMD.name());
      try {
        this.exporter = MmdExporter.find(targetFormat);

      } catch (IllegalArgumentException ex) {
        this.messager.printMessage(
            ERROR,
            "Unknown target format " + targetFormat + ", list of allowed target format names " +
                MmdExporter.LIST_VALUES.stream().map(Enum::name)
                    .collect(Collectors.joining(",", "[", "]")));
      }
    }
    this.messager.printMessage(
        NOTE, "Selected target file format: " + this.exporter.name());

    if (processingEnv.getOptions().containsKey(KEY_MMD_TARGET_FOLDER)) {
      this.optionTargetFolder = Paths.get(processingEnv.getOptions().get(KEY_MMD_TARGET_FOLDER));
      if (!(Files.isDirectory(this.optionTargetFolder) || this.optionDryStart)) {
        this.messager.printMessage(
            WARNING, "Folder for MMD not-exists: " + this.optionTargetFolder);
        if (Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FOLDER_CREATE, FALSE.toString()))) {
          try {
            Files.createDirectories(this.optionTargetFolder);
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
            format(
                "Directly provided target folder to write MMD files: %s", this.optionTargetFolder));
      }
    }

    final String baseFolderPathAsString =
        processingEnv.getOptions().getOrDefault(KEY_MMD_FILE_LINK_BASE_FOLDER, null);
    if (baseFolderPathAsString != null) {
      this.optionFileLinkBaseFolder =
          Paths.get(
              FilenameUtils.normalizeNoEndSeparator(baseFolderPathAsString));
      this.messager.printMessage(
          NOTE,
          format("Found provided file link base folder for MMD files: %s",
              this.optionFileLinkBaseFolder));
    }

    if (processingEnv.getOptions().containsKey(KEY_MMD_FILE_ROOT_FOLDER)) {
      final String path = processingEnv.getOptions().get(KEY_MMD_FILE_ROOT_FOLDER);
      this.optionFileRootFolder =
          Paths.get(path).normalize().toAbsolutePath();
      this.messager.printMessage(NOTE,
          "Provided restricting root folder for new MMD files: " + this.optionFileRootFolder);
      if (!Files.isDirectory(this.optionFileRootFolder)) {
        this.messager.printMessage(ERROR,
            "Can't find root folder for MMD processor: " + this.optionTargetFolder);
        return;
      }
    } else if (this.optionFileLinkBaseFolder != null) {
      this.optionFileRootFolder = this.optionFileLinkBaseFolder;
      this.messager.printMessage(WARNING,
          "MMD processor uses the link base folder to restrict generated file paths  (can be changed with " +
              KEY_MMD_TARGET_FOLDER + " property): " + this.optionFileRootFolder);
    }

    this.optionFileOverwrite =
        Boolean.parseBoolean(
            processingEnv.getOptions().getOrDefault(KEY_MMD_FILE_OVERWRITE, "true"));
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    final List<MmdAnnotationWrapper> foundAnnotationList = new ArrayList<>();

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
            final List<Pair<? extends Annotation, UriLine>> annotationInstances =
                AnnotationUtils.findAnnotationsWithPositions(this.sourcePositions, this.trees,
                    element, annotationClass);
            final long startPosition =
                AnnotationUtils.findStartPosition(this.sourcePositions, this.trees, element);

            if (annotationClass == MmdFileRef.class) {
              annotationInstances
                  .forEach(pair -> {
                    try {
                      assertValidMmdFileRef(element, (MmdFileRef) pair.getLeft());
                    } catch (final MmdElementException ex) {
                      this.messager.printMessage(
                          ERROR, ex.getMessage(), ex.getSource());
                    }
                  });
            } else if (annotationClass == MmdTopics.class) {
              annotationInstances.stream()
                  .flatMap(
                      pair -> AnnotationUtils.findAnnotationsWithPositions(this.sourcePositions,
                          trees, element, MmdTopic.class).stream())
                  .forEach(
                      pair -> foundAnnotationList.add(
                          new MmdAnnotationWrapper(
                              element, pair.getKey(), new File(pair.getRight().getUri()).toPath(),
                              pair.getRight().getLine(), startPosition, false)));
            } else if (annotationClass == MmdFiles.class) {
              annotationInstances.stream()
                  .flatMap(pair -> {
                    final MmdFiles mmdFiles = (MmdFiles) pair.getLeft();
                    return Arrays.stream(mmdFiles.value())
                        .map(x -> Pair.of(x, pair.getValue()));
                  })
                  .forEach(
                      pair -> foundAnnotationList.add(
                          new MmdAnnotationWrapper(
                              element, pair.getLeft(), new File(pair.getRight().getUri()).toPath(),
                              pair.getRight().getLine(), startPosition, false)));
            } else if (annotationClass == HasMmdMarkedElements.class) {
              annotationInstances
                  .forEach(pair -> {
                    if (element instanceof ExecutableElement) {
                      if (this.optionCommentScan) {
                        try {
                          final Optional<String> elementSources =
                              AnnotationUtils.findElementSources(this.sourcePositions,
                                  this.trees, element);

                          if (elementSources.isPresent()) {
                            final Path elementFile =
                                new File(
                                    AnnotationUtils.findElementSrcPosition(this.sourcePositions,
                                        this.trees,
                                        element).getUri()).toPath();
                            final AtomicInteger counter = new AtomicInteger();
                            findMmdComments(pair.getRight().getLine(), 0,
                                elementSources.get()).forEach(comment -> {
                              counter.incrementAndGet();
                              foundAnnotationList.add(
                                  new MmdAnnotationWrapper(element, comment, elementFile,
                                      comment.line(), comment.position(), true));
                            });
                            if (counter.get() > 0) {
                              this.messager.printMessage(NOTE,
                                  "Found " + counter.get() + " internal comment-markers", element);
                            }
                          }
                        } catch (Exception ex) {
                          this.messager.printMessage(ERROR,
                              "Can't read sources for element: " + ex.getMessage(), element);
                        }
                      }

                      findAllInternalMmdTopicAnnotations(
                          this.trees,
                          (ExecutableElement) element)
                          .forEach(pairInternalAnnotations -> {
                            final long localStartPosition =
                                AnnotationUtils.findStartPosition(this.sourcePositions,
                                    this.trees,
                                    pairInternalAnnotations.getValue());
                            final UriLine localPosition =
                                AnnotationUtils.findElementSrcPosition(this.sourcePositions,
                                    this.trees,
                                    pairInternalAnnotations.getValue());
                            foundAnnotationList.add(new MmdAnnotationWrapper(
                                pairInternalAnnotations.getValue(),
                                pairInternalAnnotations.getKey(),
                                new File(localPosition.getUri()).toPath(),
                                localPosition.getLine(), localStartPosition, false));
                          });
                    } else {
                      this.messager.printMessage(WARNING,
                          "Detected unexpected element marked by @" +
                              HasMmdMarkedElements.class.getSimpleName() + ": " +
                              element.getClass().getSimpleName(), element);
                    }
                  });
            } else {
              annotationInstances
                  .forEach(
                      pair -> foundAnnotationList.add(
                          new MmdAnnotationWrapper(
                              element, pair.getLeft(), new File(pair.getRight().getUri()).toPath(),
                              pair.getRight().getLine(), startPosition, false)
                      )
                  );
            }
          });
    }


    if (!foundAnnotationList.isEmpty()) {
      this.messager.printMessage(
          NOTE,
          format(
              "MMD annotation processor has found %d annotations to process",
              foundAnnotationList.size()));

      foundAnnotationList.sort(
          Comparator.comparing(o -> o.getElement().getSimpleName().toString()));

      final MmdFileBuilder fileBuilder = MmdFileBuilder.builder()
          .setMessager(this.messager)
          .setExporter(this.exporter)
          .setTypes(this.types)
          .setFileRootFolder(this.optionFileRootFolder)
          .setTargetFolder(this.optionTargetFolder)
          .setDryStart(this.optionDryStart)
          .setCommentScan(this.optionCommentScan)
          .setOverwriteAllowed(this.optionFileOverwrite)
          .setFileLinkBaseFolder(this.optionFileLinkBaseFolder)
          .setAnnotations(foundAnnotationList)
          .build();


      if (fileBuilder.write()) {
        this.messager.printMessage(
            NOTE,
            "MMD annotation processor work completed");
      } else {
        this.messager.printMessage(
            ERROR,
            "MMD annotation processor work failed");
      }
    }

    return true;
  }

  private void assertValidMmdFileRef(final Element element, final MmdFileRef fileRef)
      throws MmdElementException {
    if (isBlank(fileRef.uid())) {
      try {
        requireNonNull(fileRef.target());
        throw new MmdElementException(
            "Internal error! Unexpectedly can't get exception for access to type mirror! Contact developer!",
            element);
      } catch (final MirroredTypeException ex) {
        final TypeElement typeElement = (TypeElement) types.asElement(ex.getTypeMirror());
        if (MmdFileRef.class.getCanonicalName().equals(typeElement.getQualifiedName().toString())) {
          throw new MmdElementException(
              String.format(
                  "Found element marked by %s annotation contains only default values for attributes",
                  MmdFileRef.class.getSimpleName()), element);
        }
      }
    }
  }

}
