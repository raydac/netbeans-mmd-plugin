package com.igormaznitsa.mindmap.annotation.processor.creator;

import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationFileItem;
import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationTopicItem;
import com.igormaznitsa.mindmap.annotation.processor.creator.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotation.processor.creator.exceptions.MultipleFileVariantsForTopicException;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MmdFileCreator {

  private final Builder builder;

  private MmdFileCreator(final Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void process() {
    final Map<String, MmdAnnotationFileItem> fileMap = new LinkedHashMap<>();

    final AtomicBoolean error = new AtomicBoolean();

    this.builder.annotations.stream()
        .filter(x -> x.getAnnotation() instanceof MmdFile)
        .map(MmdAnnotationFileItem::new)
        .forEach(x -> {
          if (x.getAnnotation().getElement().getAnnotationMirrors().size() > 0) {
            if (fileMap.containsKey(x.getUid())) {
              this.builder.getMessager()
                  .printMessage(Diagnostic.Kind.ERROR,
                      String.format("Found duplicated MMD file definition for UID: %s",
                          x.getUid()), x.getAnnotation().getElement());
              error.set(true);
            } else {
              fileMap.put(x.getUid(), x);
            }
          }
        });

    if (error.get()) {
      return;
    }

    this.builder.annotations.stream()
        .filter(x -> x.getAnnotation() instanceof MmdTopic)
        .map(MmdAnnotationTopicItem::new)
        .forEach(x -> {
          try {
            this.processTopic(fileMap, x);
          } catch (MmdAnnotationProcessorException ex) {
            error.set(true);
            this.builder.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                ex.getMessage(),
                ex.getSource().getAnnotation().getElement()
            );
          }
        });

    if (error.get()) {
      return;
    }

    this.writeFiles(fileMap);
  }

  private void writeFiles(final Map<String, MmdAnnotationFileItem> fileMap) {
    fileMap.forEach((k, e) -> {
      this.builder.getMessager().printMessage(Diagnostic.Kind.NOTE,
          String.format("Processing MMD file for ID: %s", e.getUid()));
      try {
        final Path filePath = e.write(
            this.builder.getForceFolder(),
            this.builder.isOverwriteAllowed(),
            this.builder.isDryStart()
        );
        this.builder.getMessager()
            .printMessage(Diagnostic.Kind.NOTE, "Created MMD file: " + filePath);
      } catch (Exception ex) {
        this.builder.getMessager().printMessage(Diagnostic.Kind.ERROR,
            String.format("Error during MMD file write with uid '%s': %s", e.getUid(),
                ex.getMessage()), e.getAnnotation().getElement());
      }
    });
  }

  private void processTopic(
      final Map<String, MmdAnnotationFileItem> fileMap,
      final MmdAnnotationTopicItem topic
  ) throws MmdAnnotationProcessorException {
    final MmdAnnotationFileItem targetFile = findTopicTargetFile(fileMap, topic).orElse(null);
    if (targetFile == null) {
      throw new MmdAnnotationProcessorException(topic, "Can't find target MMD file for topic");
    } else {
      targetFile.addTopic(topic);
    }
  }

  private Optional<MmdAnnotationFileItem> findTopicTargetFile(
      final Map<String, MmdAnnotationFileItem> fileMap,
      final MmdAnnotationTopicItem topicItem) throws MultipleFileVariantsForTopicException {
    if (StringUtils.isBlank(topicItem.getTopicAnnotation().mmdFileUid())) {
      final Optional<String> fileUid = topicItem.findFileUidAttribute();
      if (fileUid.isPresent()) {
        return topicItem.findFileUidAttribute()
            .stream()
            .flatMap(x -> fileMap.entrySet()
                .stream()
                .filter(y -> y.getValue().getFileAnnotation().uid().equals(x)
                    || y.getValue().getFileAnnotation().fileName().equals(x))
            )
            .map(Map.Entry::getValue)
            .findFirst();
      } else {
        final List<Pair<Element, MmdFile>> directFileMarks = topicItem.findDirectMmdFileMarks();

        final List<MmdAnnotationFileItem> fileItems = directFileMarks.stream()
            .flatMap(x -> fileMap.entrySet()
                .stream()
                .filter(
                    y -> y.getValue().getFileAnnotation().equals(x.getValue()))
                .findFirst().stream()
            )
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        if (fileItems.size() == 1) {
          return Optional.of(fileItems.get(0));
        } else {
          throw new MultipleFileVariantsForTopicException(topicItem, fileItems);
        }
      }
    } else {
      MmdAnnotationFileItem found = fileMap.get(topicItem.getTopicAnnotation().mmdFileUid());
      if (found == null) {
        found = fileMap.values().stream()
            .filter(x -> FilenameUtils.getName(x.getFileAnnotation().fileName())
                .equals(topicItem.getTopicAnnotation().mmdFileUid()))
            .findFirst().orElse(null);
      }
      return Optional.ofNullable(found);
    }
  }

  public static final class Builder {
    private List<MmdAnnotation> annotations;
    private Path forceFolder;
    private boolean overwriteAllowed = true;
    private boolean dryStart;
    private boolean preferRelativePaths = true;
    private Messager messager;

    private Builder() {
    }

    public Path getForceFolder() {
      return this.forceFolder;
    }

    public Builder setForceFolder(final Path forceFolder) {
      this.forceFolder = forceFolder;
      return this;
    }

    public List<MmdAnnotation> getAnnotations() {
      return this.annotations;
    }

    public Builder setAnnotations(
        final List<MmdAnnotation> annotations) {
      this.annotations = unmodifiableList(new ArrayList<>(annotations));
      return this;
    }

    public boolean isOverwriteAllowed() {
      return this.overwriteAllowed;
    }

    public Builder setOverwriteAllowed(final boolean overwriteAllowed) {
      this.overwriteAllowed = overwriteAllowed;
      return this;
    }

    public boolean isPreferRelativePaths() {
      return this.preferRelativePaths;
    }

    public Builder setPreferRelativePaths(final boolean preferRelativePaths) {
      this.preferRelativePaths = preferRelativePaths;
      return this;
    }

    public boolean isDryStart() {
      return this.dryStart;
    }

    public Builder setDryStart(final boolean dryStart) {
      this.dryStart = dryStart;
      return this;
    }

    public Messager getMessager() {
      return this.messager;
    }

    public Builder setMessager(final Messager messager) {
      this.messager = Objects.requireNonNull(messager);
      return this;
    }

    public MmdFileCreator build() {
      if (this.annotations == null || this.messager == null) {
        throw new IllegalStateException("Not all fields set");
      }
      return new MmdFileCreator(this);
    }
  }
}
