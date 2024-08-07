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

package com.igormaznitsa.mindmap.annotations.processor.builder;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationProcessor;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import com.igormaznitsa.mindmap.annotations.processor.exporters.MindMapBinExporter;
import com.igormaznitsa.mindmap.annotations.processor.exporters.MmdExporter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

/**
 * Main class to process found annotations, sort elements. layout and write as MMD files.
 */
public class MmdFileBuilder {

  private final Builder builder;

  private final Map<String, String> propertySubstisutorMap;

  private final StringSubstitutor stringSubstitutor;

  private MmdFileBuilder(final Builder builder) {
    this.builder = builder;

    final String UNDEFINED = "<UNDEFINED>";

    this.propertySubstisutorMap = new HashMap<>();
    if (builder.getFileLinkBaseFolder() == null) {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_FILE_LINK_BASE_FOLDER,
          UNDEFINED);
    } else {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_FILE_LINK_BASE_FOLDER,
          builder.fileLinkBaseFolder.toString());
    }
    if (builder.getTargetFolder() == null) {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_TARGET_FOLDER,
          UNDEFINED);
    } else {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_TARGET_FOLDER,
          builder.getTargetFolder().toString());
    }
    if (builder.getFileRootFolder() == null) {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_FILE_ROOT_FOLDER,
          UNDEFINED);
    } else {
      this.propertySubstisutorMap.put(MmdAnnotationProcessor.KEY_MMD_FILE_ROOT_FOLDER,
          builder.getFileRootFolder().toString());
    }

    this.stringSubstitutor = new StringSubstitutor(key -> {
      String result = StringLookupFactory.INSTANCE.systemPropertyStringLookup().lookup(key);
      if (result == null) {
        result = this.propertySubstisutorMap.get(key);
      }
      return result;
    });
    this.stringSubstitutor.setVariablePrefix("${");
    this.stringSubstitutor.setVariableSuffix("}");
    this.stringSubstitutor.setEnableSubstitutionInVariables(true);
    this.stringSubstitutor.setEscapeChar('$');
    this.stringSubstitutor.setEnableUndefinedVariableException(true);
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean write() {
    final Map<String, FileItem> fileMap = new LinkedHashMap<>();

    if (!this.fillMapByFiles(
        this.builder.getExporters().stream().map(MmdExporter::getBinExporter).collect(
            Collectors.toSet()), fileMap,
        this.builder.getTargetFolder())) {
      return false;
    }

    if (this.countDuplicatedPaths(fileMap) > 0) {
      return false;
    }

    if (!this.processTopics(fileMap)) {
      return false;
    }

    return this.writeAll(fileMap);
  }

  private boolean processTopics(final Map<String, FileItem> fileMap) {
    return this.builder.getAnnotations().stream()
        .filter(x -> x.asAnnotation() instanceof MmdTopic)
        .map(x -> new TopicItem(x, this.getSubstitutor()))
        .map(
            x -> {
              boolean error = false;
              try {
                this.processTopic(fileMap, x);
              } catch (MmdElementException ex) {
                error = true;
                this.builder
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getSource());
              } catch (MmdAnnotationProcessorException ex) {
                error = true;
                this.builder
                    .getMessager()
                    .printMessage(
                        Diagnostic.Kind.ERROR,
                        ex.getMessage(),
                        ex.getSource().getElement());
              }
              return error;
            })
        .takeWhile(error -> !error)
        .noneMatch(error -> error);
  }

  private int countDuplicatedPaths(final Map<String, FileItem> fileItemMap) {
    final AtomicInteger duplicatedCounter = new AtomicInteger(0);
    final Map<Path, FileItem> processedPaths = new HashMap<>();
    fileItemMap.forEach((uid, fileItem) -> {
      for (final Pair<Path, MindMapBinExporter> targetFile : fileItem.getTargetFiles()) {
        if (processedPaths.containsKey(targetFile.getKey())) {
          this.builder.getMessager()
              .printMessage(Diagnostic.Kind.ERROR,
                  "Detected duplicated target file path for MMD file: " + targetFile.getKey(),
                  fileItem.getElement());
          duplicatedCounter.incrementAndGet();
        } else {
          processedPaths.put(targetFile.getKey(), fileItem);
        }
      }
    });
    return duplicatedCounter.get();
  }

  private BiFunction<String, Map<String, String>, String> getSubstitutor() {
    return this::makeSubstitution;
  }

  private boolean fillMapByFiles(
      final Set<MindMapBinExporter> exporters,
      final Map<String, FileItem> fileMap,
      final Path forceTargetFolder) {
    return this.builder.getAnnotations().stream()
        .filter(x -> x.asAnnotation() instanceof MmdFile)
        .map(wrapper -> new FileItem(exporters, wrapper, forceTargetFolder, this.getSubstitutor()))
        .map(x -> {
          boolean error = false;
          if (fileMap.containsKey(x.getFileUid())) {
            final FileItem alreadyDefined = fileMap.get(x.getFileUid());
            this.builder
                .getMessager()
                .printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                        "Duplicated MMD file definition (UID: %s), duplication provided at %s:%d",
                        x.getFileUid(),
                        alreadyDefined.getPath(),
                        alreadyDefined.getLine()),
                    x.getElement());
            error = true;
          } else {
            fileMap.put(x.getFileUid(), x);
          }
          return error;
        }).takeWhile(error -> !error)
        .noneMatch(error -> error);
  }

  private String makeSubstitution(final String text,
                                  final Map<String, String> additionalMap) {
    if (isBlank(text)) {
      return text;
    }
    String result = this.stringSubstitutor.replace(text);
    if (!additionalMap.isEmpty()) {
      final StringSubstitutor localSubstitute = new StringSubstitutor();
      localSubstitute.setVariablePrefix("${");
      localSubstitute.setVariableSuffix("}");
      localSubstitute.setEnableSubstitutionInVariables(true);
      localSubstitute.setEscapeChar('$');
      localSubstitute.setEnableUndefinedVariableException(true);
      result = localSubstitute.replace(result, additionalMap);
    }
    return result;
  }

  private boolean writeAll(final Map<String, FileItem> fileMap) {
    return fileMap.values().stream()
        .map(fileItem -> {
          boolean error = false;
          this.builder
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.NOTE,
                  String.format("Processing MMD file, uid=%s", fileItem.getFileUid()));

          try {
            final List<Path> filePath =
                fileItem.write(
                    this.builder.getFileRootFolder(),
                    this.builder.getTypes(),
                    this.builder.getFileLinkBaseFolder(),
                    this.builder.isOverwriteAllowed(),
                    this.builder.isDryStart());
            if (this.builder.isDryStart()) {
              this.builder
                  .getMessager()
                  .printMessage(Diagnostic.Kind.NOTE, "Formed MMD file but not saved: " + filePath);
            } else {
              this.builder
                  .getMessager()
                  .printMessage(Diagnostic.Kind.NOTE, "Saved MMD file: " + filePath);
            }
          } catch (final Exception ex) {
            error = true;
            if (ex instanceof MmdAnnotationProcessorException) {
              final MmdAnnotationProcessorException mmdEx = (MmdAnnotationProcessorException) ex;
              this.builder
                  .getMessager()
                  .printMessage(
                      Diagnostic.Kind.ERROR,
                      mmdEx.getMessage(),
                      mmdEx.getSource().getElement());
            } else {
              this.builder
                  .getMessager()
                  .printMessage(
                      Diagnostic.Kind.ERROR,
                      String.format(
                          "Error during MMD file write (uid='%s'): %s",
                          fileItem.getFileUid(), ex.getMessage()),
                      fileItem.getElement());
            }
          }
          return error;
        })
        .takeWhile(error -> !error)
        .noneMatch(error -> error);
  }

  private void processTopic(
      final Map<String, FileItem> fileMap,
      final TopicItem topic)
      throws MmdElementException, MmdAnnotationProcessorException {
    final FileItem targetFile =
        topic.findTargetFileItem(
            this.builder.getTypes(),
            fileMap,
            (message, element) -> this.builder.getMessager()
                .printMessage(Diagnostic.Kind.WARNING, message, element)
        ).orElse(null);
    if (targetFile == null) {
      throw new MmdAnnotationProcessorException(topic, "Can't find target MMD file for topic");
    } else {
      targetFile.addChild(topic);
    }
  }

  public static final class Builder {
    private List<MmdAnnotationWrapper> annotations;
    private Path targetFolder;
    private Path fileLinkBaseFolder;
    private Path fileRootFolder;
    private boolean overwriteAllowed = true;
    private boolean dryStart;
    private boolean commentScan;
    private Messager messager;
    private Set<MmdExporter> exporters = Set.of(MmdExporter.MMD);
    private Types types;

    private volatile boolean completed;

    private Builder() {
    }

    private void assertNotCompleted() {
      if (this.completed) {
        throw new IllegalStateException("Already completed");
      }
    }

    public Set<MmdExporter> getExporters() {
      return this.exporters;
    }

    public Builder setExporters(final Set<MmdExporter> exporters) {
      this.assertNotCompleted();
      this.exporters = requireNonNull(exporters);
      return this;
    }

    public Path getTargetFolder() {
      return this.targetFolder;
    }

    public Builder setTargetFolder(final Path targetFolder) {
      this.assertNotCompleted();
      this.targetFolder = targetFolder;
      return this;
    }

    public boolean isOverwriteAllowed() {
      return this.overwriteAllowed;
    }

    public Builder setOverwriteAllowed(final boolean overwriteAllowed) {
      this.assertNotCompleted();
      this.overwriteAllowed = overwriteAllowed;
      return this;
    }

    public boolean isCommentScan() {
      return this.commentScan;
    }

    public Builder setCommentScan(final boolean commentScan) {
      this.assertNotCompleted();
      this.commentScan = commentScan;
      return this;
    }

    public boolean isDryStart() {
      return this.dryStart;
    }

    public Builder setDryStart(final boolean dryStart) {
      this.assertNotCompleted();
      this.dryStart = dryStart;
      return this;
    }

    public List<MmdAnnotationWrapper> getAnnotations() {
      return this.annotations;
    }

    public Builder setAnnotations(final List<MmdAnnotationWrapper> annotations) {
      this.assertNotCompleted();
      this.annotations = unmodifiableList(new ArrayList<>(annotations));
      return this;
    }

    public Types getTypes() {
      return types;
    }

    public Builder setTypes(final Types types) {
      this.assertNotCompleted();
      this.types = requireNonNull(types);
      return this;
    }

    public Messager getMessager() {
      return this.messager;
    }

    public Builder setMessager(final Messager messager) {
      this.assertNotCompleted();
      this.messager = requireNonNull(messager);
      return this;
    }

    public MmdFileBuilder build() {
      this.assertNotCompleted();
      if (this.annotations == null
          || this.messager == null
          || this.types == null) {
        throw new IllegalStateException("Not all fields set");
      }
      this.completed = true;
      return new MmdFileBuilder(this);
    }

    public Path getFileLinkBaseFolder() {
      return this.fileLinkBaseFolder;
    }

    public Builder setFileLinkBaseFolder(final Path value) {
      this.assertNotCompleted();
      this.fileLinkBaseFolder = value;
      return this;
    }

    public Path getFileRootFolder() {
      return this.fileRootFolder;
    }

    public Builder setFileRootFolder(final Path fileRootFolder) {
      this.assertNotCompleted();
      this.fileRootFolder = fileRootFolder;
      return this;
    }
  }
}
