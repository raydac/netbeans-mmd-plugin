package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.annotation.processor.creator.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;

public class MmdAnnotationFileItem extends AbstractMmdAnnotationItem {
  private final MmdFile mmdFileAnnotation;
  private final String uid;

  private final List<TopicLayoutItem> topics = new ArrayList<>();

  public MmdAnnotationFileItem(final MmdAnnotation annotation) {
    super(annotation);
    if (!(annotation.getAnnotation() instanceof MmdFile)) {
      throw new IllegalArgumentException("Expected annotation " + MmdFile.class.getName());
    }
    this.mmdFileAnnotation = (MmdFile) annotation.getAnnotation();
    this.uid = UUID.randomUUID().toString();
  }

  private Path getSourceClassFolder() {
    return this.annotation.getPath().getParent();
  }

  private Path findTargetPath(final Path forceFolder) {
    Path folder = Paths.get(normalizeNoEndSeparator(this.mmdFileAnnotation
        .folder().replace(MmdFile.MACROS_SRC_CLASS_FOLDER,
            normalizeNoEndSeparator(this.getSourceClassFolder().toAbsolutePath().toString()))));
    if (!folder.isAbsolute()) {
      folder = FileSystems.getDefault().getPath(".").resolve(folder);
    }

    final String name;
    if (StringUtils.isBlank(this.mmdFileAnnotation.fileName())) {
      name = FilenameUtils.removeExtension(this.annotation.getPath().getFileName().toString());
    } else {
      name = this.mmdFileAnnotation.fileName().trim();
    }
    return forceFolder == null ? folder.resolve(name + ".mmd")
        : forceFolder.resolve(name + ".mmd");
  }

  public void addTopic(final MmdAnnotationTopicItem topic) {
    this.topics.add(new TopicLayoutItem(requireNonNull(topic)));
  }

  public Path write(
      final Path forceFolder,
      final boolean allowOverwrite,
      final boolean dryStart
  ) throws IOException, MmdAnnotationProcessorException {
    final MindMap map;
    try {
      map = this.makeMindMap();
    } catch (URISyntaxException ex) {
      throw new IOException("Can't write MMD file for URI syntax error", ex);
    }

    final String mapText = map.asString();

    final Path filePath = this.findTargetPath(forceFolder);

    if (dryStart) {
      // do nothing
    } else {
      PathUtils.createParentDirectories(filePath);
      if (Files.isRegularFile(filePath) && !allowOverwrite) {
        throw new IOException("MMD file already exists: " + filePath);
      }
      FileUtils.write(filePath.toFile(), mapText, StandardCharsets.UTF_8);
    }
    return filePath;
  }

  private MindMap makeMindMap() throws URISyntaxException, MmdAnnotationProcessorException {
    final MindMap map = new MindMap(true);
    map.putAttribute("showJumps", "true");

    fillAttributesWithoutFileAndTopicLinks(
        map.getRoot(),
        this.annotation.getElement(),
        this.mmdFileAnnotation.rootTopic()
    );
    this.doTopicLayout(map);

    return map;
  }

  public MmdFile getFileAnnotation() {
    return this.mmdFileAnnotation;
  }

  public String getUid() {
    return this.uid;
  }

  private Optional<TopicLayoutItem> findForNameOrUid(final TopicLayoutItem preferredParent,
                                                     final String nameOrUid) {
    final List<TopicLayoutItem> found = this.topics
        .stream()
        .filter(x -> StringUtils.isNotBlank(x.getAnnotation().uid()) &&
            nameOrUid.equals(x.getAnnotation().uid()))
        .collect(Collectors.toList());

    if (found.isEmpty()) {
      return Optional.empty();
    } else {
      Optional<TopicLayoutItem> result = found.stream()
          .filter(x -> x.equals(preferredParent))
          .findFirst();
      if (!result.isPresent()) {
        result = Optional.of(found.get(0));
      }
      return result;
    }
  }

  private TopicLayoutItem ensureTopicPathAndGetLast(final TopicLayoutItem item)
      throws MmdAnnotationProcessorException {
    final String[] wholePath = item.getAnnotation().path();
    final String firstPathItemId = wholePath[0];
    TopicLayoutItem first = this.findForNameOrUid(null, firstPathItemId).orElse(null);
    if (first == null) {
      first = new TopicLayoutItem(item.getAnnotationItem(), firstPathItemId, true);
      this.topics.add(first);
    }
    TopicLayoutItem current = first;
    for (int i = 1; i < wholePath.length; i++) {
      TopicLayoutItem found = this.findForNameOrUid(current, wholePath[i]).orElse(null);
      if (found == null) {
        found = new TopicLayoutItem(item.getAnnotationItem(), firstPathItemId, true);
        found.setParent(current);
        this.topics.add(found);
      } else {
        if (found.getParent() == null) {
          found.setParent(current);
        } else {
          if (!found.getParent().equals(current)) {
            throw new MmdAnnotationProcessorException(found.getAnnotationItem(),
                "Can't layout topic, may be it belongs to many paths");
          }
        }
      }
      current = found;
    }
    return current;
  }

  private static void checkForGraphLoops(final List<TopicLayoutItem> items)
      throws MmdAnnotationProcessorException {
    for (final TopicLayoutItem item : items) {
      if (item.getParent() != null) {
        int counter = items.size();
        TopicLayoutItem current = item;
        while (current.getParent() != null) {
          counter--;
          if (counter < 0) {
            throw new MmdAnnotationProcessorException(item.getAnnotationItem(),
                "Detected loop at generated graph for MMD topic path");
          }
          current = current.getParent();
        }
      }
    }
  }

  private void doTopicLayout(final MindMap mindMap)
      throws URISyntaxException, MmdAnnotationProcessorException {
    // auto-layout by close parent topic elements
    this.topics
        .stream()
        .filter(topic -> !topic.isLinked())
        .forEach(topic -> {
          topic.findCloseParentByElements(this.topics).ifPresent(topic::setParent);
        });

    // re-layout for topics contain defined paths
    final List<TopicLayoutItem> pathSorted = this.topics.stream()
        .filter(x -> x.getAnnotation().path().length > 0)
        .sorted(comparingInt(o -> o.getAnnotation().path().length))
        .collect(Collectors.toList());

    for (final TopicLayoutItem item : pathSorted) {
      item.setParent(ensureTopicPathAndGetLast(item));
    }

    checkForGraphLoops(this.topics);

    // generate topics in mind map
    for (final TopicLayoutItem item : this.topics) {
      requireNonNull(item.findOrCreateTopic(mindMap), "Unexpected null during topic create");
    }

    // set direction flags on first level topics
    for (final TopicLayoutItem item : this.topics) {
      item.processTopicAttributes();
    }

    this.fillInternalTopicLinks(mindMap, this.topics);
  }

  private void fillInternalTopicLinks(final MindMap mindMap, final List<TopicLayoutItem> items)
      throws MmdAnnotationProcessorException {
    final Map<String, Topic> uidMarkedTopics = new HashMap<>();
    for (final Topic topic : mindMap) {
      final String uid = topic.getAttribute(MmdAttribute.TOPIC_LINK_UID.getId());
      if (uid != null) {
        if (uidMarkedTopics.put(uid, topic) != null) {
          final TopicLayoutItem uidMarkedItem = items.stream()
              .filter(x -> x.getAnnotation().uid().equals(uid))
              .findFirst()
              .orElseThrow(() -> new Error(
                  "Unexpected situation, can't find child topic for provided UIO: " + uid));
          throw new MmdAnnotationProcessorException(uidMarkedItem.getAnnotationItem(),
              "Detected duplicated MMD topic UID '" + uid + '\'');
        }
      }
    }

    if (isNoneBlank(this.mmdFileAnnotation.rootTopic().jumpTo())) {
      final String targetUid = this.mmdFileAnnotation.rootTopic().jumpTo();
      if (!targetUid.equals(this.getFileAnnotation().rootTopic().uid())) {
        final Topic targetTopic = uidMarkedTopics.get(targetUid);
        if (targetTopic == null) {
          throw new MmdAnnotationProcessorException(this,
              "Can't find target topic for UID '" + targetUid + '\'');
        } else {
          mindMap.getRoot().setExtra(new ExtraTopic(targetUid));
        }
      }
    }
    for (final TopicLayoutItem item : items) {
      final String targetUid = item.getAnnotation().jumpTo();
      if (isNoneBlank(targetUid)) {
        if (!item.getAnnotation().uid().equals(targetUid)) {
          if (uidMarkedTopics.containsKey(targetUid)) {
            item.findOrCreateTopic(mindMap).setExtra(new ExtraTopic(targetUid));
          } else {
            throw new MmdAnnotationProcessorException(item.getAnnotationItem(),
                "Can't find target topic for UID '" + targetUid + '\'');
          }
        }
      }
    }
  }
}
