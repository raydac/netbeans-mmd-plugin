package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findEnclosingType;
import static com.igormaznitsa.mindmap.annotations.processor.builder.AnnotationUtils.findFirstWithAncestors;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdFiles;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdElementException;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MultipleFileVariantsForTopicException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class TopicItem extends AbstractItem {
  public TopicItem(final MmdAnnotationWrapper base) {
    super(base);
    if (!(base.asAnnotation() instanceof MmdTopic)) {
      throw new IllegalArgumentException("Expected annotation " + MmdTopic.class.getName());
    }
  }

  private static Optional<String> findFileUidAmongParentTopics(final Element element) {
    if (element == null) {
      return Optional.empty();
    }
    final MmdTopic topicAnnotation = element.getAnnotation(MmdTopic.class);
    if (topicAnnotation != null && StringUtils.isNotBlank(topicAnnotation.mmdFileUid())) {
      return Optional.of(topicAnnotation.mmdFileUid());
    } else {
      return findFileUidAmongParentTopics(element.getEnclosingElement());
    }
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

    final List<MmdFiles> filesAnnotation =
        findFirstWithAncestors(
            typeElement, MmdFiles.class, types, true)
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

  public Optional<FileItem> findTargetFile(
      final Types types,
      final Map<String, FileItem> fileMap)
      throws MmdElementException, MmdAnnotationProcessorException {
    if (StringUtils.isBlank(this.asMmdTopicAnnotation().mmdFileUid())) {
      final Optional<String> fileUid = this.findFileUidAttribute();
      if (fileUid.isPresent()) {
        return this.findFileUidAttribute().stream()
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
            findTargetFile(types, fileMap, this.getElement());

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
          throw new MultipleFileVariantsForTopicException(this, fileItems);
        } else {
          throw new MmdAnnotationProcessorException(
              this, "There is no any defined MMD file for element");
        }
      }
    } else {
      FileItem found = fileMap.get(this.asMmdTopicAnnotation().mmdFileUid());
      if (found == null) {
        found =
            fileMap.values().stream()
                .filter(
                    x ->
                        FilenameUtils.getName(x.asMmdFileAnnotation().fileName())
                            .equals(this.asMmdTopicAnnotation().mmdFileUid()))
                .findFirst()
                .orElse(null);
      }
      return Optional.ofNullable(found);
    }
  }


  public MmdTopic asMmdTopicAnnotation() {
    return this.asAnnotation();
  }

  public Optional<String> findFileUidAttribute() {
    return findFileUidAmongParentTopics(this.getElement());
  }

}
