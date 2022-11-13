package com.igormaznitsa.mindmap.annotations.processor.builder;

import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.FoundMmdAnnotation;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class MmdFileBuilder {

  private final Builder builder;

  private MmdFileBuilder(final Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void write() {
    final Map<String, FileItem> fileMap = new LinkedHashMap<>();

    final AtomicBoolean errorDetected = new AtomicBoolean();

    this.builder.annotations.stream()
        .filter(x -> x.asAnnotation() instanceof MmdFile)
        .map(FileItem::new)
        .forEach(x -> {
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
            errorDetected.set(true);
          } else {
            fileMap.put(x.getFileUid(), x);
          }
        });

    if (errorDetected.get()) {
      return;
    }

    this.builder.annotations.stream()
        .filter(x -> x.asAnnotation() instanceof MmdTopic)
        .map(TopicItem::new)
        .forEach(
            x -> {
              try {
                this.processTopic(fileMap, x);
              } catch (MmdElementException ex) {
                errorDetected.set(true);
                this.builder
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getSource());
              } catch (MmdAnnotationProcessorException ex) {
                errorDetected.set(true);
                this.builder
                    .getMessager()
                    .printMessage(
                        Diagnostic.Kind.ERROR,
                        ex.getMessage(),
                        ex.getSource().getElement());
              }
            });

    if (errorDetected.get()) {
      return;
    }

    this.writeAll(fileMap);
  }

  private void writeAll(final Map<String, FileItem> fileMap) {
    fileMap.forEach(
        (k, e) -> {
          this.builder
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.NOTE,
                  String.format("Processing MMD file, uid=%s", e.getFileUid()));
          try {
            final Path filePath =
                e.write(
                    this.builder.getTypes(),
                    this.builder.getTargetFolder(),
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
          } catch (Exception ex) {
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
                          "Error during MMD file write with uid '%s': %s",
                          e.getFileUid(), ex.getMessage()),
                      e.getElement());
            }
          }
        });
  }

  private void processTopic(
      final Map<String, FileItem> fileMap,
      final TopicItem topic)
      throws MmdElementException, MmdAnnotationProcessorException {
    final FileItem targetFile =
        topic.findTargetFile(this.builder.getTypes(), fileMap).orElse(null);
    if (targetFile == null) {
      throw new MmdAnnotationProcessorException(topic, "Can't find target MMD file for topic");
    } else {
      targetFile.addChild(topic);
    }
  }

  public static final class Builder {
    private List<FoundMmdAnnotation> annotations;
    private Path targetFolder;
    private Path fileLinkBaseFolder;
    private boolean overwriteAllowed = true;
    private boolean dryStart;
    private Messager messager;
    private Elements elements;
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

    public Builder setAnnotations(final List<FoundMmdAnnotation> annotations) {
      this.assertNotCompleted();
      this.annotations = unmodifiableList(new ArrayList<>(annotations));
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

    public Builder setElements(final Elements elements) {
      this.assertNotCompleted();
      this.elements = Objects.requireNonNull(elements);
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
          || this.elements == null
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
      this.fileLinkBaseFolder = value;
      return this;
    }
  }
}
