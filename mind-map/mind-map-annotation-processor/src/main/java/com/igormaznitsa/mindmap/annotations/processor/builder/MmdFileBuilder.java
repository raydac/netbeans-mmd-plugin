package com.igormaznitsa.mindmap.annotations.processor.builder;

import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Main class to process found annotations, sort elements. layout and write as MMD files.
 */
public class MmdFileBuilder {

  private final Builder builder;

  private MmdFileBuilder(final Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean write() {
    final Map<String, FileItem> fileMap = new LinkedHashMap<>();

    if (!this.fillMapByFiles(fileMap, this.builder.getTargetFolder())) {
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
        .map(TopicItem::new)
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
      if (processedPaths.containsKey(fileItem.getTargetFile())) {
        this.builder.getMessager()
            .printMessage(Diagnostic.Kind.ERROR,
                "Detected duplicated target file path for MMD file: " + fileItem.getTargetFile(),
                fileItem.getElement());
        duplicatedCounter.incrementAndGet();
      } else {
        processedPaths.put(fileItem.getTargetFile(), fileItem);
      }
    });
    return duplicatedCounter.get();
  }

  private boolean fillMapByFiles(final Map<String, FileItem> fileMap,
                                 final Path forceTargetFolder) {
    return this.builder.getAnnotations().stream()
        .filter(x -> x.asAnnotation() instanceof MmdFile)
        .map(wrapper -> new FileItem(wrapper, forceTargetFolder))
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
            final Path filePath =
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
    private Messager messager;
    private Types types;

    private volatile boolean completed;

    private Builder() {
    }

    private void assertNotCompleted() {
      if (this.completed) {
        throw new IllegalStateException("Already completed");
      }
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
      this.types = Objects.requireNonNull(types);
      return this;
    }

    public Messager getMessager() {
      return this.messager;
    }

    public Builder setMessager(final Messager messager) {
      this.assertNotCompleted();
      this.messager = Objects.requireNonNull(messager);
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
