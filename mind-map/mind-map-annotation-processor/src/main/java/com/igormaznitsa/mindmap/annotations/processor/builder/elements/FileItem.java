package com.igormaznitsa.mindmap.annotations.processor.builder.elements;

import static com.igormaznitsa.mindmap.model.annotations.MmdFile.MACROS_SRC_CLASS_FOLDER;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import com.igormaznitsa.mindmap.annotations.processor.FoundMmdAnnotation;
import com.igormaznitsa.mindmap.annotations.processor.builder.exceptions.MmdAnnotationProcessorException;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.lang.model.util.Types;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;

public class FileItem extends AbstractItem {
  private final String fileUid;

  private final List<InternalLayoutBlock> layoutBlocks = new ArrayList<>();

  public FileItem(final FoundMmdAnnotation base) {
    super(base);
    if (!(base.asAnnotation() instanceof MmdFile)) {
      throw new IllegalArgumentException("Expected annotation " + MmdFile.class.getSimpleName());
    }
    final MmdFile mmdFile = base.asAnnotation();
    if (StringUtils.isBlank(mmdFile.uid())) {
      this.fileUid = "$auto:" + UUID.randomUUID();
    } else {
      this.fileUid = mmdFile.uid();
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
                item.getAnnotationItem(), "Detected loop at generated graph for MMD topic path");
          }
          current = current.getParent();
        }
      }
    }
  }

  public MmdFile asMmdFileAnnotation() {
    return this.asAnnotation();
  }

  private Path getFolder() {
    return this.getPath().getParent();
  }

  private Path makeTargetFilePath(final Path forceFolder) {
    final String rawFileName;
    if (StringUtils.isBlank(this.asMmdFileAnnotation().fileName())) {
      rawFileName =
          FilenameUtils.removeExtension(this.getPath().getFileName().toString());
    } else {
      rawFileName = this.asMmdFileAnnotation().fileName();
    }

    final Path targetFolder =
        Objects.requireNonNullElseGet(
            forceFolder,
            () ->
                Paths.get(
                    normalizeNoEndSeparator(
                        this.asMmdFileAnnotation()
                            .folder()
                            .replace(
                                MACROS_SRC_CLASS_FOLDER,
                                normalizeNoEndSeparator(
                                    this.getFolder().toAbsolutePath().toString())))));

    return targetFolder.resolve(rawFileName + ".mmd");
  }

  public void addChild(final TopicItem topic) {
    this.layoutBlocks.add(new InternalLayoutBlock(requireNonNull(topic)));
  }

  public Path write(
      final Types types,
      final Path targetFolder,
      final Path fileLinkBaseFolder,
      final boolean allowOverwrite,
      final boolean dryStart)
      throws IOException, MmdAnnotationProcessorException {

    final Path targetFile = this.makeTargetFilePath(targetFolder);

    final MindMap map;
    try {
      map =
          this.makeMindMap(
              types, fileLinkBaseFolder == null ? targetFile.getParent() : fileLinkBaseFolder);
    } catch (URISyntaxException ex) {
      throw new IOException("Can't write MMD file for URI syntax error", ex);
    }

    final String mapText = map.asString();

    if (dryStart) {
      // do nothing
    } else {
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
    map.putAttribute("showJumps", "true");
    map.putAttribute("generatorId", "com.igormaznitsa:mind-map-annotation-processor:1.6.0");
    if (fileLinkBaseFolder == null) {
      map.putAttribute("noBaseFolder", "true");
    }

    fillAttributesWithoutFileAndTopicLinks(
        map.getRoot(), this.getElement(), this.asMmdFileAnnotation().rootTopic());
    this.doTopicLayout(fileLinkBaseFolder, map, types);

    return map;
  }

  public String getFileUid() {
    return this.fileUid;
  }

  private Optional<InternalLayoutBlock> findForNameOrUid(
      final InternalLayoutBlock preferredParent, final String nameOrUid) {
    List<InternalLayoutBlock> found =
        this.layoutBlocks.stream()
            .filter(
                x ->
                    StringUtils.isNotBlank(x.getAnnotation().uid())
                        && nameOrUid.equals(x.getAnnotation().uid()))
            .collect(Collectors.toList());

    if (found.isEmpty()) {
      found =
          this.layoutBlocks.stream()
              .filter(x -> nameOrUid.equals(x.findAnyPossibleUid()))
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
    InternalLayoutBlock first = this.findForNameOrUid(null, firstPathItemId).orElse(null);
    if (first == null) {
      first = new InternalLayoutBlock(item.getAnnotationItem(), firstPathItemId, true);
      this.layoutBlocks.add(first);
    }
    InternalLayoutBlock current = first;
    for (int i = 1; i < wholePath.length; i++) {
      InternalLayoutBlock found = this.findForNameOrUid(current, wholePath[i]).orElse(null);
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
                found.getAnnotationItem(), "Can't layout topic, may be it belongs to many paths");
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
            topic -> {
              topic.findParentAmong(types, this.layoutBlocks).ifPresent(topic::setParent);
            });

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
      if (!item.isAutocreated()) {
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

    if (isNoneBlank(this.asMmdFileAnnotation().rootTopic().jumpTo())) {
      final String targetUid = this.asMmdFileAnnotation().rootTopic().jumpTo();
      if (!targetUid.equals(this.asMmdFileAnnotation().rootTopic().uid())) {
        final Topic targetTopic = uidMarkedTopics.get(targetUid);
        if (targetTopic == null) {
          throw new MmdAnnotationProcessorException(
              this, "Can't find target topic for UID '" + targetUid + '\'');
        } else {
          mindMap.getRoot().setExtra(new ExtraTopic(targetUid));
        }
      }
    }
    for (final InternalLayoutBlock item : items) {
      final String targetUid = item.getAnnotation().jumpTo();
      if (isNoneBlank(targetUid)) {
        if (!item.getAnnotation().uid().equals(targetUid)) {
          if (uidMarkedTopics.containsKey(targetUid)) {
            item.findOrCreateTopic(mindMap).setExtra(new ExtraTopic(targetUid));
          } else {
            throw new MmdAnnotationProcessorException(
                item.getAnnotationItem(), "Can't find target topic for UID '" + targetUid + '\'');
          }
        }
      }
    }
  }
}
