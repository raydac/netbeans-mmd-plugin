package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static com.igormaznitsa.mindmap.annotations.MmdFile.MACROS_SRC_CLASS_FOLDER;
import static com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem.MmdAttribute.TOPIC_LINK_UID;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.processor.MmdAnnotationWrapper;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.lang.model.util.Types;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Class describes one MMD file defined through MMD file annotation.
 *
 * @see MmdFile
 */
public class FileItem extends AbstractItem {
  private final String fileUid;
  private final String baseFileName;
  private final Path targetFile;
  private final List<InternalLayoutBlock> layoutBlocks = new ArrayList<>();

  public FileItem(final MmdAnnotationWrapper base, final Path forceTargetFolder) {
    super(base);
    if (!(base.asAnnotation() instanceof MmdFile)) {
      throw new IllegalArgumentException("Expected annotation " + MmdFile.class.getSimpleName());
    }
    final MmdFile mmdFile = base.asAnnotation();
    if (StringUtils.isBlank(mmdFile.uid())) {
      this.fileUid = "$auto:::" + UUID.randomUUID();
    } else {
      this.fileUid = mmdFile.uid();
    }

    this.targetFile = makeTargetFilePath(base, forceTargetFolder);
    this.baseFileName = findBaseFileName(base);
  }

  private static String findBaseFileName(final MmdAnnotationWrapper annotationWrapper) {
    final MmdFile mmdFile = annotationWrapper.asAnnotation();
    if (StringUtils.isBlank(mmdFile.fileName())) {
      return
          FilenameUtils.removeExtension(annotationWrapper.getPath().getFileName().toString());
    } else {
      return mmdFile.fileName();
    }
  }

  private static void assertNoGraphLoops(final List<InternalLayoutBlock> items)
      throws MmdAnnotationProcessorException {
    for (final InternalLayoutBlock item : items) {
      if (item.getParent() != null) {
        int counter = items.size();
        InternalLayoutBlock current = item;
        while (current.getParent() != null) {
          counter--;
          if (counter < 0) {
            throw new MmdAnnotationProcessorException(
                item.getAnnotationItem(),
                "Detected loop at graph at MMD topic path: " + makeLoopInfoString(item));
          }
          current = current.getParent();
        }
      }
    }
  }

  private static String makeLoopInfoString(final InternalLayoutBlock item) {
    final StringBuilder buffer = new StringBuilder();
    final Set<InternalLayoutBlock> processed = new HashSet<>();
    InternalLayoutBlock current = item;
    while (current.getParent() != null) {
      if (buffer.length() > 0) {
        buffer.append("->");
      }
      buffer.append(current.asTextWithoutControl());
      if (processed.contains(current)) {
        buffer.append("->");
        buffer.append(item.asTextWithoutControl());
        break;
      } else {
        processed.add(current);
      }
      current = current.getParent();
    }
    return buffer.toString();
  }

  private static Path makeTargetFilePath(final MmdAnnotationWrapper annotationWrapper,
                                         final Path forceTargetFolder) {
    final MmdFile mmdFile = annotationWrapper.asAnnotation();
    final String baseFileName = findBaseFileName(annotationWrapper);

    final Path targetFolder =
        Objects.requireNonNullElseGet(
            forceTargetFolder,
            () ->
                Paths.get(
                    normalizeNoEndSeparator(
                        mmdFile
                            .folder()
                            .replace(
                                MACROS_SRC_CLASS_FOLDER,
                                normalizeNoEndSeparator(
                                    annotationWrapper.getPath().getParent().toAbsolutePath()
                                        .toString())))));

    return targetFolder.resolve(baseFileName + ".mmd");
  }

  public String getBaseName() {
    return this.baseFileName;
  }

  public Path getTargetFile() {
    return this.targetFile;
  }

  public MmdFile asMmdFileAnnotation() {
    return this.asAnnotation();
  }

  public void addChild(final TopicItem topic) {
    this.layoutBlocks.add(new InternalLayoutBlock(requireNonNull(topic)));
  }

  public Path write(
      final Path rootFolder,
      final Types types,
      final Path fileLinkBaseFolder,
      final boolean allowOverwrite,
      final boolean dryStart)
      throws IOException, MmdAnnotationProcessorException {

    final Path targetFile = this.getTargetFile().normalize();

    final MindMap map;
    try {
      map =
          this.makeMindMap(
              types, fileLinkBaseFolder == null ? targetFile.getParent() : fileLinkBaseFolder);
    } catch (final URISyntaxException ex) {
      throw new IOException("Can't write MMD file for URI syntax error", ex);
    }

    final String mapText = map.asString();

    if (rootFolder != null && !targetFile.startsWith(rootFolder)) {
      throw new IOException("Target file is not bounded by the root folder: " + targetFile);
    }

    if (!dryStart) {
      PathUtils.createParentDirectories(targetFile);
      if (Files.isRegularFile(targetFile) && !allowOverwrite) {
        throw new IOException("MMD file already exists: " + targetFile);
      }
      FileUtils.write(targetFile.toFile(), mapText, StandardCharsets.UTF_8);
    }
    return targetFile;
  }

  private MindMap makeMindMap(final Types types, final Path fileLinkBaseFolder)
      throws URISyntaxException, MmdAnnotationProcessorException {
    final MindMap map = new MindMap(true);
    map.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS, "true");
    map.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_GENERATOR_ID,
        "com.igormaznitsa:mind-map-annotation-processor:1.6.0");
    if (fileLinkBaseFolder == null) {
      map.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_NO_BASE_FOLDER, "true");
    }

    fillAttributesWithoutFileAndTopicLinks(
        map.getRoot(), this.getElement(), this.asMmdFileAnnotation().rootTopic());
    this.doTopicLayout(fileLinkBaseFolder, map, types);

    return map;
  }

  public String getFileUid() {
    return this.fileUid;
  }

  @Override
  public int hashCode() {
    return this.fileUid.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (that == this) {
      return true;
    }
    if (that instanceof FileItem) {
      final FileItem thatItem = (FileItem) that;
      return this.fileUid.equals(thatItem.fileUid);
    }
    return false;
  }

  private Optional<InternalLayoutBlock> findForUidOrTitle(
      final InternalLayoutBlock preferredParent, final String id) {
    List<InternalLayoutBlock> found =
        this.layoutBlocks.stream()
            .filter(
                x ->
                    StringUtils.isNotBlank(x.getAnnotation().uid())
                        && id.equals(x.getAnnotation().uid()))
            .collect(Collectors.toList());

    if (found.isEmpty()) {
      found =
          this.layoutBlocks.stream()
              .filter(x -> id.equals(x.findAnyPossibleUid()))
              .collect(Collectors.toList());
    }

    if (found.isEmpty()) {
      return Optional.empty();
    } else {
      Optional<InternalLayoutBlock> result =
          found.stream().filter(x -> x.equals(preferredParent)).findFirst();
      if (!result.isPresent()) {
        result = Optional.of(found.get(0));
      }
      return result;
    }
  }

  private InternalLayoutBlock getLastTopicAtPath(final InternalLayoutBlock item)
      throws MmdAnnotationProcessorException {

    final String[] wholePath = item.getAnnotation().path();
    final String firstPathItemId = wholePath[0];
    InternalLayoutBlock first = this.findForUidOrTitle(null, firstPathItemId).orElse(null);
    if (first == null) {
      first = new InternalLayoutBlock(item.getAnnotationItem(), firstPathItemId, true);
      this.layoutBlocks.add(first);
    }
    InternalLayoutBlock current = first;
    for (int i = 1; i < wholePath.length; i++) {
      InternalLayoutBlock found = this.findForUidOrTitle(current, wholePath[i]).orElse(null);
      if (found == null) {
        found = new InternalLayoutBlock(item.getAnnotationItem(), wholePath[i], true);
        found.setParent(current);
        this.layoutBlocks.add(found);
      } else {
        if (found.getParent() == null) {
          found.setParent(current);
        } else {
          if (!found.getParent().equals(current)) {
            throw new MmdAnnotationProcessorException(
                found.getAnnotationItem(),
                "MMD processor has found multiple target paths for a topic during linking: " +
                    current.asTextWithoutControl());
          }
        }
      }
      current = found;
    }
    return current;
  }

  private void doTopicLayout(
      final Path fileLinkBaseFolder,
      final MindMap mindMap,
      final Types types
  ) throws URISyntaxException, MmdAnnotationProcessorException {
    // auto-layout by close parent topic elements
    this.layoutBlocks.stream()
        .filter(topic -> !topic.isLinked())
        .forEach(
            topic -> topic.findParentAmong(types, this.layoutBlocks).ifPresent(topic::setParent));

    // re-layout for topics contain defined paths
    final List<InternalLayoutBlock> pathSorted =
        this.layoutBlocks.stream()
            .filter(x -> x.getAnnotation().path().length > 0)
            .sorted(comparingInt(o -> o.getAnnotation().path().length))
            .collect(Collectors.toList());

    for (final InternalLayoutBlock item : pathSorted) {
      item.setParent(getLastTopicAtPath(item));
    }

    assertNoGraphLoops(this.layoutBlocks);

    // generate topics in mind map
    for (final InternalLayoutBlock item : this.layoutBlocks) {
      requireNonNull(item.findOrCreateTopic(mindMap), "Unexpected null during topic create");
    }

    // set direction flags on first level topics
    for (final InternalLayoutBlock item : this.layoutBlocks) {
      item.processTopicAttributes();
    }

    this.fillInternalTopicLinks(mindMap, this.layoutBlocks);
    this.fillFileLinksAndAnchors(mindMap, fileLinkBaseFolder, this.layoutBlocks);
  }

  private void fillFileLinksAndAnchors(
      final MindMap mindMap, final Path fileLinkBaseFolder, final List<InternalLayoutBlock> items)
      throws MmdAnnotationProcessorException {
    final Path basePath = fileLinkBaseFolder.toAbsolutePath();
    fillAnchorOrFileLink(mindMap.getRoot(), this, this.asMmdFileAnnotation().rootTopic(), basePath);
    for (final InternalLayoutBlock item : items) {
      if (!item.isAutoGenerated()) {
        fillAnchorOrFileLink(
            item.findOrCreateTopic(mindMap),
            item.getAnnotationItem(),
            item.getAnnotation(),
            basePath);
      }
    }
  }

  private void fillInternalTopicLinks(final MindMap mindMap, final List<InternalLayoutBlock> items)
      throws MmdAnnotationProcessorException {
    final Map<String, Topic> uidMarkedTopics = new HashMap<>();
    for (final Topic topic : mindMap) {
      final String uid = topic.getAttribute(MmdAttribute.TOPIC_LINK_UID.getId());
      if (uid != null) {
        if (uidMarkedTopics.put(uid, topic) != null) {
          final InternalLayoutBlock uidMarkedItem =
              items.stream()
                  .filter(x -> x.getAnnotation().uid().equals(uid))
                  .findFirst()
                  .orElseThrow(
                      () ->
                          new Error(
                              "Unexpected situation, can't find child topic for provided UIO: "
                                  + uid));
          throw new MmdAnnotationProcessorException(
              uidMarkedItem.getAnnotationItem(),
              "Detected duplicated MMD topic UID '" + uid + '\'');
        }
      }
    }

    for (final Topic topic : mindMap) {
      final String title = topic.getText();
      if (StringUtils.isNotBlank(title)) {
        if (!uidMarkedTopics.containsKey(title)) {
          uidMarkedTopics.put(title, topic);
        }
      }
    }

    if (isNoneBlank(this.asMmdFileAnnotation().rootTopic().jumpTo())) {
      final String targetUidOrTitle = this.asMmdFileAnnotation().rootTopic().jumpTo();
      if (!targetUidOrTitle.equals(this.asMmdFileAnnotation().rootTopic().uid())) {
        final Topic targetTopic = uidMarkedTopics.get(targetUidOrTitle);
        if (targetTopic == null) {
          throw new MmdAnnotationProcessorException(
              this, "Can't find target topic for UID or title: '" + targetUidOrTitle + '\'');
        } else {
          mindMap.getRoot().setExtra(new ExtraTopic(targetUidOrTitle));
        }
      }
    }
    for (final InternalLayoutBlock item : items) {
      String targetUid = item.getAnnotation().jumpTo();
      if (isNoneBlank(targetUid)) {
        final Topic targetTopic = uidMarkedTopics.get(targetUid);
        final Topic itemTopic = item.findOrCreateTopic(mindMap);
        if (targetTopic == null) {
          throw new MmdAnnotationProcessorException(
              item.getAnnotationItem(),
              "Can't find target topic for UID or title: '" + targetUid + '\'');
        } else if (targetTopic == itemTopic) {
          throw new MmdAnnotationProcessorException(
              item.getAnnotationItem(),
              "Detected self-jump for UID or title: '" + targetUid + '\'');
        } else {
          if (targetTopic.getAttribute(TOPIC_LINK_UID.getId()) == null) {
            targetUid = "auto-" + UUID.randomUUID();
            targetTopic.putAttribute(TOPIC_LINK_UID.getId(), targetUid);
          } else {
            targetUid = targetTopic.getAttribute(TOPIC_LINK_UID.getId());
          }
          itemTopic.setExtra(new ExtraTopic(targetUid));
        }
      }
    }
  }
}
