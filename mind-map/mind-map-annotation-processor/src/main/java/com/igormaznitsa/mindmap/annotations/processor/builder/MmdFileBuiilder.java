package com.igormaznitsa.mindmap.annotations.processor.builder;

import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findEnclosingType;
import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findFirstWithAncestors;
import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.processor.FoundMmdAnnotation;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MultipleFileVariantsForTopicException;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MmdFileBuiilder {

  private final Builder builder;

  private MmdFileBuiilder(final Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  private static List<Pair<Element, MmdFile>> findTargetFile(
      final Types types,
      final Map<String, FileItem> fileMap,
      final Element element)
      throws MmdElementException {
    if (element == null) {
      return List.of();
    }

    final Element typeElement = findEnclosingType(element).orElse(null);
    if (typeElement == null) {
      throw new MmdElementException("Can't find enclosing type element: " + element, element);
    }

    final List<MmdFile> fileAnnotation =
        findFirstWithAncestors(typeElement, MmdFile.class, types, true).stream()
            .map(Pair::getLeft)
            .collect(Collectors.toList());

    final List<com.igormaznitsa.mindmap.model.annotations.MmdFiles> filesAnnotation =
        findFirstWithAncestors(
            typeElement, com.igormaznitsa.mindmap.model.annotations.MmdFiles.class, types, true)
            .stream()
            .map(Pair::getLeft)
            .collect(Collectors.toList());

    final List<MmdFileLink> fileLinkAnnotation =
        findFirstWithAncestors(typeElement, MmdFileLink.class, types, true).stream()
            .map(Pair::getLeft)
            .collect(Collectors.toList());

    if (filesAnnotation.isEmpty() && fileAnnotation.isEmpty() && fileLinkAnnotation.isEmpty()) {
      throw new MmdElementException("Can't find any MMD file mark", element);
    } else {
      final List<Pair<Element, MmdFile>> result = new ArrayList<>();
      for (final MmdFile f : fileAnnotation) {
        result.add(Pair.of(element, f));
      }

      for (final MmdFileLink link : fileLinkAnnotation) {
        final String uid = link.uid();
        if (StringUtils.isBlank(uid)) {
          throw new MmdElementException("Element has blank file link UID", element);
        }
        if (fileMap.containsKey(uid)) {
          final FileItem fileItem = fileMap.get(uid);
          result.add(Pair.of(element, fileItem.asMmdFileAnnotation()));
        } else {
          throw new MmdElementException(
              "Can't find any MMD file element with UID: " + uid, element);
        }
      }
      return result;
    }
  }

  public void write() {
    final Map<String, FileItem> fileMap = new LinkedHashMap<>();

    final AtomicBoolean error = new AtomicBoolean();

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
                        "Found duplicated MMD file definition (UID: %s) for %s:%d",
                        x.getFileUid(),
                        alreadyDefined.getPath(),
                        alreadyDefined.getLine()),
                    x.getElement());
            error.set(true);
          } else {
            fileMap.put(x.getFileUid(), x);
          }
        });

    if (error.get()) {
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
                error.set(true);
                this.builder
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getSource());
              } catch (MmdAnnotationProcessorException ex) {
                error.set(true);
                this.builder
                    .getMessager()
                    .printMessage(
                        Diagnostic.Kind.ERROR,
                        ex.getMessage(),
                        ex.getSource().getElement());
              }
            });

    if (error.get()) {
      return;
    }

    this.write(fileMap);
  }

  private void write(final Map<String, FileItem> fileMap) {
    fileMap.forEach(
        (k, e) -> {
          this.builder
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.NOTE,
                  String.format("Processing MMD file for ID: %s", e.getFileUid()));
          try {
            final Path filePath =
                e.write(
                    this.builder.getTypes(),
                    this.builder.getTargetFolder(),
                    this.builder.getFileLinkBaseFolder(),
                    this.builder.isOverwriteAllowed(),
                    this.builder.isDryStart());
            this.builder
                .getMessager()
                .printMessage(Diagnostic.Kind.NOTE, "Created MMD file: " + filePath);
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
        findTopicTargetFile(this.builder.getTypes(), fileMap, topic).orElse(null);
    if (targetFile == null) {
      throw new MmdAnnotationProcessorException(topic, "Can't find target MMD file for topic");
    } else {
      targetFile.addChild(topic);
    }
  }

  private Optional<FileItem> findTopicTargetFile(
      final Types types,
      final Map<String, FileItem> fileMap,
      final TopicItem topicItem)
      throws MmdElementException, MmdAnnotationProcessorException {
    if (StringUtils.isBlank(topicItem.asMmdTopicAnnotation().mmdFileUid())) {
      final Optional<String> fileUid = topicItem.findFileUidAttribute();
      if (fileUid.isPresent()) {
        return topicItem.findFileUidAttribute().stream()
            .flatMap(
                x ->
                    fileMap.entrySet().stream()
                        .filter(
                            y ->
                                y.getValue().asMmdFileAnnotation().uid().equals(x)
                                    || y.getValue().asMmdFileAnnotation().fileName().equals(x)))
            .map(Map.Entry::getValue)
            .findFirst();
      } else {
        final List<Pair<Element, MmdFile>> directFileMarks =
            findTargetFile(types, fileMap, topicItem.getElement());

        final List<FileItem> fileItems =
            directFileMarks.stream()
                .flatMap(
                    x ->
                        fileMap.entrySet().stream()
                            .filter(y -> y.getValue().asMmdFileAnnotation().equals(x.getValue()))
                            .findFirst()
                            .stream())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (fileItems.size() == 1) {
          return Optional.of(fileItems.get(0));
        } else if (fileItems.size() > 1) {
          throw new MultipleFileVariantsForTopicException(topicItem, fileItems);
        } else {
          throw new MmdAnnotationProcessorException(
              topicItem, "There is no any defined MMD file for element");
        }
      }
    } else {
      FileItem found = fileMap.get(topicItem.asMmdTopicAnnotation().mmdFileUid());
      if (found == null) {
        found =
            fileMap.values().stream()
                .filter(
                    x ->
                        FilenameUtils.getName(x.asMmdFileAnnotation().fileName())
                            .equals(topicItem.asMmdTopicAnnotation().mmdFileUid()))
                .findFirst()
                .orElse(null);
      }
      return Optional.ofNullable(found);
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

    private Builder() {
    }

    public Path getTargetFolder() {
      return this.targetFolder;
    }

    public Builder setTargetFolder(final Path targetFolder) {
      this.targetFolder = targetFolder;
      return this;
    }

    public List<FoundMmdAnnotation> getAnnotations() {
      return this.annotations;
    }

    public Builder setAnnotations(final List<FoundMmdAnnotation> annotations) {
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

    public boolean isDryStart() {
      return this.dryStart;
    }

    public Builder setDryStart(final boolean dryStart) {
      this.dryStart = dryStart;
      return this;
    }

    public Elements getElements() {
      return this.elements;
    }

    public Builder setElements(final Elements elements) {
      this.elements = Objects.requireNonNull(elements);
      return this;
    }

    public Types getTypes() {
      return types;
    }

    public Builder setTypes(final Types types) {
      this.types = Objects.requireNonNull(types);
      return this;
    }


    public Messager getMessager() {
      return this.messager;
    }

    public Builder setMessager(final Messager messager) {
      this.messager = Objects.requireNonNull(messager);
      return this;
    }

    public MmdFileBuiilder build() {
      if (this.annotations == null
          || this.messager == null
          || this.elements == null
          || this.types == null) {
        throw new IllegalStateException("Not all fields set");
      }
      return new MmdFileBuiilder(this);
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
