/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.model;

import static com.igormaznitsa.mindmap.model.MiscUtils.ensureNotNull;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.IOUtils;

/**
 * Mind map container. <b>It is not thread safe!</b>
 */
public class MindMap implements Serializable, Constants, Iterable<Topic>, Cloneable {

  /**
   * Mind map format version
   */
  public static final String FORMAT_VERSION = "1.1";
  private static final long serialVersionUID = 5929181596778047354L;
  private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s(.+)$");
  private static final Pattern PATTERN_ATTRIBUTE =
      Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*(\\`+)(.*?)\\2");
  private static final String GENERATOR_VERSION_NAME = "__version__";
  private final Map<String, String> attributes =
      new TreeMap<>(Comparator.naturalOrder());
  private final transient List<MindMapModelListener> modelListeners =
      new CopyOnWriteArrayList<>();

  private Topic root;

  /**
   * Create new container.
   *
   * @param makeRoot if true then empty root topic will be auto-created, if false then there is no any topic in new map
   */
  public MindMap(final boolean makeRoot) {
    if (makeRoot) {
      this.root = new Topic(this, null, "");
    }
  }

  private MindMap(final MindMap map) {
    this.attributes.putAll(map.attributes);
    final Topic rootTopic = map.getRoot();
    this.root = rootTopic == null ? null : rootTopic.makeCopy(this, null);
  }

  /**
   * Make mind map based on reader content, internal errors will be ignored during reading.
   *
   * @param reader reader to source, must not be null
   * @throws IOException thrown if any read error
   */
  public MindMap(final Reader reader) throws IOException {
    this(reader, true);
  }

  /**
   * Make mind map from reader content.
   *
   * @param reader       source reader, must not be null
   * @param ignoreErrors flag shows that format errors should be ignored during read
   * @throws IOException thrown if any read error
   */
  public MindMap(final Reader reader, final boolean ignoreErrors) throws IOException {
    final String text = IOUtils.toString(requireNonNull(reader));

    final MindMapLexer lexer = new MindMapLexer();
    lexer.start(text, 0, text.length(), MindMapLexer.TokenType.HEAD_LINE);

    Topic rootTopic = null;

    boolean process = true;

    while (process) {
      final int oldLexerPosition = lexer.getCurrentPosition().getOffset();
      lexer.advance();
      final boolean lexerPositionWasNotChanged =
          oldLexerPosition == lexer.getCurrentPosition().getOffset();

      final MindMapLexer.TokenType token = lexer.getTokenType();
      if (token == null || lexerPositionWasNotChanged) {
        throw new IllegalArgumentException("Wrong format of mind map, end of header is not found");
      }
      switch (token) {
        case HEAD_LINE:
          continue;
        case ATTRIBUTE: {
          fillMapByAttributes(lexer.getTokenText(), this.attributes);
        }
        break;
        case HEAD_DELIMITER: {
          process = false;
          rootTopic = Topic.parse(this, lexer, ignoreErrors);
        }
        break;
        default:
          break;
      }
    }

    this.root = rootTopic;
    this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
  }

  static boolean fillMapByAttributes(final String line,
                                     final Map<String, String> map) {
    final Matcher attrmatcher = PATTERN_ATTRIBUTES.matcher(line);
    if (attrmatcher.find()) {
      final Matcher attrParser = PATTERN_ATTRIBUTE.matcher(attrmatcher.group(1));
      while (attrParser.find()) {
        map.put(attrParser.group(1), attrParser.group(3));
      }
      return true;
    }
    return false;
  }

  static String allAttributesAsString(final Map<String, String> map) throws IOException {
    final StringBuilder buffer = new StringBuilder();

    final List<String> attrNames = new ArrayList<>(map.keySet());
    Collections.sort(attrNames);

    boolean nonFirst = false;
    for (final String k : attrNames) {
      final String value = map.get(k);
      if (nonFirst) {
        buffer.append(',');
      } else {
        nonFirst = true;
      }
      buffer.append(k).append('=').append(ModelUtils.makeMDCodeBlock(value));
    }

    return buffer.toString();
  }

  /**
   * Make full copy of the mind map with copy of all its content.
   *
   * @return copy of the mind map, must not be null
   */
  public MindMap makeCopy() {
    return new MindMap(this);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return this.makeCopy();
  }

  /**
   * Remove all mind map content.
   */
  public void clear() {
    this.setRoot(null, true);
  }

  /**
   * Find next topic for content matches with pattern since provided start topic
   *
   * @param baseFolder      base folder for mind map, can be null
   * @param start           start topic, can be null
   * @param pattern         pattern to be used for topic content, must not be null
   * @param findInTopicText flag if true shows that topic title should be used for search
   * @param extrasToFind    set of extra types to be included into search, can be null
   * @return found next topic in mind map matches pattern for selected content, can be null if not found
   */
  public Topic findNext(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind
  ) {
    return this.findNext(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
  }

  /**
   * Find next topic for content matches with pattern since provided start topic
   *
   * @param baseFolder      base folder for mind map, can be null
   * @param start           start topic, can be null
   * @param pattern         pattern to be used for topic content, must not be null
   * @param findInTopicText flag if true shows that topic title should be used for search
   * @param extrasToFind    set of extra types to be included into search, can be null
   * @param topicFinders    custom finders to make extra search in topic if it doesn't match with pattern, can be null
   * @return found next topic in mind map matches pattern for selected content, can be null if not found
   */
  public Topic findNext(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind,
      final Set<TopicFinder> topicFinders
  ) {
    if (start != null && start.getMap() != this) {
      throw new IllegalArgumentException("Topic must belong to the mind map");
    }

    Topic result = null;

    boolean startFound = start == null;
    for (final Topic t : this) {
      if (startFound) {
        if (t.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
          result = t;
        } else if (topicFinders != null) {
          for (final TopicFinder f : topicFinders) {
            if (f.doesTopicContentMatches(t, baseFolder, pattern, extrasToFind)) {
              result = t;
              break;
            }
          }
        }
        if (result != null) {
          break;
        }
      } else if (t == start) {
        startFound = true;
      }
    }
    return result;
  }

  /**
   * Find previous topic for content matches with pattern since provided start topic
   *
   * @param baseFolder      base folder for mind map, can be null
   * @param start           start topic, can be null
   * @param pattern         pattern to be used for topic content, must not be null
   * @param findInTopicText flag if true shows that topic title should be used for search
   * @param extrasToFind    set of extra types to be included into search, can be null
   * @return found next topic in mind map matches pattern for selected content, can be null if not found
   */
  public Topic findPrev(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind
  ) {
    return this.findPrev(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
  }

  /**
   * Find previous topic for content matches with pattern since provided start topic
   *
   * @param baseFolder      base folder for mind map, can be null
   * @param start           start topic, can be null
   * @param pattern         pattern to be used for topic content, must not be null
   * @param findInTopicText flag if true shows that topic title should be used for search
   * @param extrasToFind    set of extra types to be included into search, can be null
   * @param topicFinders    custom finders to make extra search in topic if it doesn't match with pattern, can be null   * @return found next topic in mind map matches pattern for selected content, can be null if not found
   */
  public Topic findPrev(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind,
      final Set<TopicFinder> topicFinders
  ) {
    if (start != null && start.getMap() != this) {
      throw new IllegalArgumentException("Topic doesn't belong to the mind map");
    }

    Topic result = null;
    final List<Topic> plain = this.asList();
    int startIndex = start == null ? plain.size() : plain.indexOf(start);
    if (startIndex < 0) {
      throw new IllegalArgumentException(
          "It looks like that topic doesn't belong to the mind map");
    }
    if (startIndex > 0) {
      while (startIndex > 0 && result == null) {
        final Topic candidate = plain.get(--startIndex);
        if (candidate.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
          result = candidate;
        } else if (topicFinders != null) {
          for (TopicFinder f : topicFinders) {
            if (f.doesTopicContentMatches(candidate, baseFolder, pattern, extrasToFind)) {
              result = candidate;
              break;
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Set root topic for mind map.
   *
   * @param newRoot          topic to be new root for mind map, can be null
   * @param makeNotification if true then send notification to listeners
   * @throws IllegalStateException if topic is not belong to the mind map
   */
  public void setRoot(final Topic newRoot, final boolean makeNotification) {
    if (newRoot != null) {
      if (newRoot.getMap() != this) {
        throw new IllegalStateException("Base map must be the same");
      }
    }
    this.root = newRoot;
    if (makeNotification) {
      this.fireModelChanged();
    }
  }

  /**
   * Allows to iterate through all topics and their children in the mind map
   *
   * @return topic iterator, must not be null
   */
  @Override
  public Iterator<Topic> iterator() {
    final Topic theRoot = this.root;

    return new Iterator<Topic>() {
      Topic rootTopic = theRoot;
      Iterator<Topic> children;

      @Override
      public void remove() {
        this.children.remove();
      }

      @Override
      public boolean hasNext() {
        return this.rootTopic != null || (this.children != null && this.children.hasNext());
      }

      @Override
      public Topic next() {
        final Topic result;
        if (this.rootTopic != null) {
          result = this.rootTopic;
          this.rootTopic = null;
          this.children = result.iterator();
        } else if (this.children != null) {
          result = this.children.next();
        } else {
          throw new NoSuchElementException();
        }
        return result;
      }
    };
  }

  /**
   * Check that the mind map is empty.
   *
   * @return true is the mind map is empty, false otherwise
   */
  public boolean isEmpty() {
    return this.root == null;
  }

  private void fireModelChanged() {
    final Topic rootTopic = this.root;
    final MindMapModelEvent event =
        new MindMapModelEvent(this, rootTopic == null ? null : rootTopic.getPath());
    for (final MindMapModelListener l : this.modelListeners) {
      l.onMindMapStructureChanged(event);
    }
  }

  private void fireTopicChanged(final Topic topic) {
    final MindMapModelEvent event =
        new MindMapModelEvent(this, topic == null ? null : topic.getPath());
    for (final MindMapModelListener l : this.modelListeners) {
      l.onMindMapNodesChanged(event);
    }
  }

  /**
   * Get names of all attributes in the mind map.
   *
   * @return set of attribute names, must not be null
   */
  public Set<String> getAttributeNames() {
    return this.attributes.keySet();
  }

  /**
   * Find attribute in the mind map.
   *
   * @param name attribute name, must not be null
   * @return value of attribute if found, null otherwise
   */
  public String findAttribute(final String name) {
    return this.attributes.get(name);
  }

  /**
   * Put or remove named attribute.
   *
   * @param name  attribute name, must not be null
   * @param value attribute value, if null then the attribute will be removed
   * @return previous value of attribute or null if there is not such one
   */
  public String putAttribute(final String name, final String value) {
    final String previous;
    if (value == null) {
      previous = this.attributes.remove(requireNonNull(name));
    } else {
      previous = this.attributes.put(requireNonNull(name), value);
    }
    return previous;
  }

  /**
   * Remove all topic payloads in the mind map.
   */
  public void clearAllPayloads() {
    if (this.root != null) {
      clearAllPayloads(this.root);
    }
  }

  /**
   * Remove all payloads for topic and its successors.
   *
   * @param topic topic to be processed, must not be null
   */
  private void clearAllPayloads(final Topic topic) {
    topic.setPayload(null);
    for (final Topic m : topic.getChildren()) {
      this.clearAllPayloads(m);
    }
  }

  /**
   * Find topic at position.
   *
   * @param position positions to coordinate topic
   * @return null for zero coordinate, the root for only position, child topic for topic at the last addressed position otherwise, null if there is not any child with position
   */
  public Topic findAtPosition(final int... position) {
    if (position.length == 0) {
      return null;
    }

    Topic result = this.root;
    int index = 1;
    while (result != null && index < position.length) {
      final int elementPosition = position[index++];
      if (elementPosition < 0 || result.getChildren().size() <= elementPosition) {
        result = null;
        break;
      }
      result = result.getChildren().get(elementPosition);
    }
    return result;
  }

  /**
   * Get root topic.
   *
   * @return root topic or null if the mind map is empty
   */
  public Topic getRoot() {
    return this.root;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("MindMap[");
    String delimiter = "";
    for (final Topic t : this) {
      builder.append(delimiter);
      builder.append(t);
      delimiter = ",";
    }
    builder.append(']');
    return builder.toString();
  }

  /**
   * Write whole mind map content into string.
   *
   * @return mind map content as string, must not be null
   */
  public String asString() {
    final StringWriter writer = new StringWriter();
    try {
      this.write(writer);
    } catch (IOException ex) {
      throw new Error("Unexpected exception", ex);
    }
    return writer.toString();
  }

  /**
   * Write content of the mind map into a writer.
   *
   * @param writer target writer, must not be null
   * @param <T>    type of writer
   * @return the same target writer, must not be null
   * @throws IOException thrown if any error during write
   */
  public <T extends Writer> T write(final T writer) throws IOException {
    writer.append("[Scia Reto](https://sciareto.org) mind map").append(NEXT_PARAGRAPH);
    this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
    writer.append("> ").append(MindMap.allAttributesAsString(this.attributes))
        .append(NEXT_LINE);
    writer.append("---").append(NEXT_LINE);
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      rootTopic.write(writer);
    }
    return writer;
  }

  /**
   * Clone topic in the mind map. If it is root then root won't be cloned but same root will be returned
   *
   * @param topic             the target topic, must not be null
   * @param cloneWholeSubtree if true then whole subtree should be cloned, false otherwise
   * @return cloned topic, must not be null.
   */
  public Topic cloneTopicInMap(final Topic topic, final boolean cloneWholeSubtree) {
    if (requireNonNull(topic) == this.root) {
      return this.root;
    }

    final Topic clonedtopic = topic.makeCopy(this, topic.getParent());
    if (!cloneWholeSubtree) {
      clonedtopic.removeAllChildren();
    }

    clonedtopic.removeAttributes(true, ExtraTopic.TOPIC_UID_ATTR);
    fireModelChanged();
    return clonedtopic;
  }

  /**
   * Remove topic from the mind map.
   *
   * @param topic topic to be removed. must not be null
   * @return true if removed, false otherwise
   * @throws IllegalStateException if topic doesn't belong to the map
   */
  public boolean removeTopic(final Topic topic) {
    if (topic.getMap() != this) {
      throw new IllegalStateException("Topic is not belong to the map");
    }

    final boolean result;
    final Topic rootTopic = this.root;
    if (rootTopic == null) {
      result = false;
    } else if (this.root == topic) {
      rootTopic.setText("");
      rootTopic.removeExtras();
      rootTopic.setPayload(null);
      rootTopic.removeAllChildren();
      result = true;
    } else {
      rootTopic.removeTopic(topic);
      result = rootTopic.removeAllLinksTo(topic);
    }
    if (result) {
      this.fireModelChanged();
    }

    return result;
  }

  /**
   * Find topic which is the target for link.
   *
   * @param link link to topic, must not be null
   * @return found target topic or null if not found
   */
  public Topic findTopicForLink(final ExtraTopic link) {
    Topic result = null;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      result = rootTopic.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, link.getValue());
    }
    return result;
  }

  /**
   * List all topic in the mind map contain extra with specified type.
   *
   * @param type extra type, must not be null
   * @return listed found topics, must not be null
   */
  public List<Topic> findAllTopicsForExtraType(final Extra.ExtraType type) {
    final List<Topic> result = new ArrayList<>();
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      _findAllTopicsForExtraType(rootTopic, type, result);
    }
    return result;
  }

  private void _findAllTopicsForExtraType(final Topic topic,
                                          final Extra.ExtraType type,
                                          final List<Topic> result) {
    if (topic.getExtras().containsKey(type)) {
      result.add(topic);
    }
    for (final Topic c : topic.getChildren()) {
      _findAllTopicsForExtraType(c, type, result);
    }
  }

  /**
   * Change topic text and send notification to listeners.
   *
   * @param topic topic belongs to the map, must not be null
   * @param text  text to be set, can be null
   * @throws IllegalStateException if topic not belongs to the map
   */
  public void setTopicTextWithEvent(final Topic topic, final String text) {
    if (topic.getMap() == this) {
      topic.setText(ensureNotNull(text, ""));
      this.fireTopicChanged(topic);
    } else {
      throw new IllegalStateException("Topic must belong to the map");
    }
  }

  /**
   * Add mind map model listener.
   *
   * @param listener listener must not be null
   */
  public void addMindMapModelListener(final MindMapModelListener listener) {
    this.modelListeners.add(requireNonNull(listener));
  }

  /**
   * Remove mind map model listener
   *
   * @param listener listener must not be null
   */
  public void removeMindMapModelListener(final MindMapModelListener listener) {
    this.modelListeners.remove(requireNonNull(listener));
  }

  /**
   * Check that mind map contains a file link.
   *
   * @param baseFolder base folder for the mind map, can be null
   * @param file       file to be checked, can't be null
   * @return true if mind map contains link to the file, false otherwise
   */
  public boolean doesContainFile(final File baseFolder, final MMapURI file) {
    boolean result = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      return rootTopic.doesContainFileLink(baseFolder, file, true);
    }
    return result;
  }

  /**
   * Remove all links to a file.
   *
   * @param baseFolder base folder for mind map, can be null
   * @param file       file link to be deleted, must not be null
   * @return true if any topic was processed and its file link was removed, false otherwise
   */
  public boolean deleteAllLinksToFile(final File baseFolder, final MMapURI file) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      changed = rootTopic.deleteFileLinkIfPresented(baseFolder, file);
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

  /**
   * Find all links to specified file and replace them by new one.
   *
   * @param baseFolder base folder for mind map, can be null
   * @param oldFile    file to be replaced, must not be null
   * @param newFile    new file, must not be null
   * @return true if any topic with old file processed, false otherwise
   */
  public boolean replaceAllLinksToFile(final File baseFolder,
                                       final MMapURI oldFile,
                                       final MMapURI newFile) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      changed = rootTopic.replaceFileLinkIfPresented(baseFolder, oldFile, newFile);
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

  /**
   * Get the mind map as a stream.
   *
   * @return mind map topics through sequential stream, must not be null
   */
  public Stream<Topic> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  /**
   * Get the mind map as list
   *
   * @return list of topics in the mind map, must not be null
   */
  public List<Topic> asList() {
    return this.stream().collect(Collectors.toList());
  }

}
