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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFileRef;
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
import javax.lang.model.util.Types;
import org.apache.commons.io.FilenameUtils;

/**
 * Annotation processor to collect MMD annotations and build MMD mind map files for them.
 */
@SupportedOptions({
    MmdAnnotationProcessor.KEY_MMD_TARGET_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_FOLDER_CREATE,
    MmdAnnotationProcessor.KEY_MMD_DRY_START,
    MmdAnnotationProcessor.KEY_MMD_FILE_OVERWRITE,
    MmdAnnotationProcessor.KEY_MMD_FILE_ROOT_FOLDER,
    MmdAnnotationProcessor.KEY_MMD_FILE_LINK_BASE_FOLDER
})
public class MmdAnnotationProcessor extends AbstractProcessor {

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
   * Option to overwrite result MMD file if already exist
   */
  public static final String KEY_MMD_FILE_OVERWRITE = "mmd.file.overwrite";
  private static final Map<String, Class<? extends Annotation>> ANNOTATIONS =
      Map.of(
          MmdTopic.class.getName(), MmdTopic.class,
          MmdFiles.class.getName(), MmdFiles.class,
          MmdFile.class.getName(), MmdFile.class,
          MmdFileRef.class.getName(), MmdFileRef.class);
  private Trees trees;
  private SourcePositions sourcePositions;
  private Messager messager;
  private Types types;
  private Path optionTargetFolder;
  private Path optionFileLinkBaseFolder;
  private Path optionFileRootFolder;
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
            final Annotation[] annotationInstances = element.getAnnotationsByType(annotationClass);
            final long startPosition =
                AnnotationUtils.findStartPosition(this.sourcePositions, this.trees, element);
            final UriLine position =
                AnnotationUtils.findPosition(this.sourcePositions, this.trees, element);

            if (annotationClass == MmdFiles.class) {
              Arrays.stream(annotationInstances)
                  .map(x -> (MmdFiles) x)
                  .flatMap(x -> Stream.of(x.value()))
                  .forEach(
                      file -> foundAnnotationList.add(
                          new MmdAnnotationWrapper(
                              element, file, new File(position.getUri()).toPath(),
                              position.getLine(), startPosition)));
            } else {
              Arrays.stream(annotationInstances)
                  .forEach(
                      instance -> foundAnnotationList.add(
                          new MmdAnnotationWrapper(
                              element, instance, new File(position.getUri()).toPath(),
                              position.getLine(), startPosition)));
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
          (o1, o2) ->
              o1.getElement().getSimpleName().toString().compareTo(o2.getElement().toString()));

      final MmdFileBuilder fileBuilder = MmdFileBuilder.builder()
          .setMessager(this.messager)
          .setTypes(this.types)
          .setFileRootFolder(this.optionFileRootFolder)
          .setTargetFolder(this.optionTargetFolder)
          .setDryStart(this.optionDryStart)
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

}
