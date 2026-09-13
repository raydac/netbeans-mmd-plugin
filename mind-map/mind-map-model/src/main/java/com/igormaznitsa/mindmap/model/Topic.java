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

package com.igormaznitsa.mindmap.model;

import static com.igormaznitsa.mindmap.model.MiscUtils.ensureNoNullElement;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class describes the main work unit for Mind Map.
 */
public final class Topic implements Serializable, Constants, Iterable<Topic> {

  private static final long serialVersionUID = -4642569244907433215L;
  private static final AtomicLong LOCAL_UID_GENERATOR = new AtomicLong();
  private final EnumMap<Extra.ExtraType, Extra<?>> extras =
      new EnumMap<>(Extra.ExtraType.class);
  private final Map<String, String> attributes =
      new TreeMap<>(Comparator.naturalOrder());
  private final Map<String, String> codeSnippets =
      new TreeMap<>(Comparator.naturalOrder());
  private final List<Topic> children = new ArrayList<>();
  private final transient long localUID = LOCAL_UID_GENERATOR.getAndIncrement();
  private final MindMap map;
  private Topic parent;
  private volatile String text;
  private transient Object payload;

  /**
   * Constructor to build topic on base of another topic for another mind map.
   *
   * @param mindMap      mind map to be owner for new topic
   * @param base         base source topic
   * @param copyChildren flag to make copy of children, true if to make copy,
   *                     false otherwise
   */
  public Topic(final MindMap mindMap, final Topic base,
               final boolean copyChildren) {
    this(mindMap, null, base.text);
    this.attributes.putAll(base.attributes);
    this.extras.putAll(base.extras);
    this.codeSnippets.putAll(base.codeSnippets);

    if (copyChildren) {
      for (final Topic t : base.children) {
        final Topic clonedChildren = new Topic(mindMap, t, true);
        clonedChildren.parent = this;
        this.children.add(clonedChildren);
      }
    }
  }

  /**
   * Constructor
   *
   * @param map    parent map, must not be null
   * @param parent parent topic can be null
   * @param text   topic text, must not be null
   * @param extras extras for the topic, must not be null
   */
  public Topic(final MindMap map, final Topic parent, final String text,
               final Extra<?>... extras) {
    this.map = requireNonNull(map);
    this.text = requireNonNull(text);

    for (final Extra<?> extra : extras) {
      if (extra != null) {
        this.extras.put(extra.getType(), extra);
        extra.attachedToTopic(this);
      }
    }
    this.parent = parent;

    if (parent != null) {
      if (parent.getMap() != map) {
        throw new IllegalArgumentException("Parent must belong to the same mind map");
      }
      parent.children.add(this);
    }
  }

  public static Topic parse(final MindMap map, final MindMapLexer lexer,
                            final boolean ignoreErrors) {
    return new TopicParser(map, lexer, ignoreErrors).parse();
  }

  public Topic findRoot() {
    return this.getRoot();
  }

  public boolean containTopic(final Topic topic) {
    return this == topic || this.children.stream().anyMatch(child -> child.containTopic(topic));
  }

  public Topic nextSibling() {
    final int position = this.parent == null ? -1 : this.parent.getChildren().indexOf(this);

    final Topic result;
    if (position < 0) {
      result = null;
    } else {
      final List<Topic> all = this.parent.getChildren();
      final int nextPosition = position + 1;
      result = all.size() > nextPosition ? all.get(nextPosition) : null;
    }
    return result;
  }

  public Topic prevSibling() {
    final int position = this.parent == null ? -1 : this.parent.getChildren().indexOf(this);

    final Topic result;
    if (position <= 0) {
      result = null;
    } else {
      final List<Topic> all = this.parent.getChildren();
      result = all.get(position - 1);
    }
    return result;
  }

  public boolean containsPattern(final File baseFolder, final Pattern pattern,
                                 final boolean findInTopicText,
                                 final Set<Extra.ExtraType> extrasForSearch) {
    if (findInTopicText && pattern.matcher(this.text).find()) {
      return true;
    }
    if (extrasForSearch == null || extrasForSearch.isEmpty()) {
      return false;
    }
    return this.extras.values().stream()
        .anyMatch(extra -> extrasForSearch.contains(extra.getType())
            && extra.containsPattern(baseFolder, pattern));
  }

  public boolean isRoot() {
    return this.parent == null;
  }

  public Object getPayload() {
    return this.payload;
  }

  public void setPayload(final Object value) {
    this.payload = value;
  }

  private Object readResolve() {
    return new Topic(this.map, this, true);
  }

  public MindMap getMap() {
    return this.map;
  }

  public int getTopicLevel() {
    Topic topic = this.parent;
    int result = 0;
    while (topic != null) {
      topic = topic.parent;
      result++;
    }
    return result;
  }

  public Topic findParentForDepth(final int depth) {
    Topic result = this.parent;
    int remaining = depth;
    while (remaining > 0 && result != null) {
      result = result.parent;
      remaining--;
    }
    return result;
  }

  public Topic getRoot() {
    Topic result = this;
    while (true) {
      final Topic prev = result.parent;
      if (prev == null) {
        break;
      }
      result = prev;
    }
    return result;
  }

  public Topic getFirst() {
    return this.children.isEmpty() ? null : this.children.get(0);
  }

  public Topic getLast() {
    return this.children.isEmpty() ? null : this.children.get(this.children.size() - 1);
  }

  public List<Topic> getChildren() {
    return this.children;
  }

  public boolean isExtrasEmpty() {
    return this.extras.isEmpty();
  }

  public Map<Extra.ExtraType, Extra<?>> getExtras() {
    return this.extras;
  }

  public Map<String, String> getAttributes() {
    return this.attributes;
  }

  public Map<String, String> getCodeSnippets() {
    return this.codeSnippets;
  }

  public boolean putAttribute(final String name, final String value) {
    if (value == null) {
      return this.attributes.remove(name) != null;
    } else {
      return !value.equals(this.attributes.put(name, value));
    }
  }

  public boolean putCodeSnippet(final String language, final String text) {
    if (text == null) {
      return this.codeSnippets.remove(language) != null;
    } else {
      return !text.equals(this.codeSnippets.put(language, text));
    }
  }

  public String getCodeSnippet(final String language) {
    return this.codeSnippets.get(language);
  }

  public String getAttribute(final String name) {
    return this.attributes.get(name);
  }

  public void delete() {
    final Topic theParent = this.parent;
    if (theParent != null) {
      theParent.children.remove(this);
      this.parent = null;
    }
  }

  public Topic getParent() {
    return this.parent;
  }

  public String getText() {
    return this.text;
  }

  public void setText(final String text) {
    this.text = requireNonNull(text);
  }

  public boolean isFirstChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(0) == t;
  }

  public boolean isLastChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(this.children.size() - 1) == t;
  }

  public boolean removeExtra(final Extra.ExtraType... types) {
    boolean result = false;
    for (final Extra.ExtraType e : ensureNoNullElement(types)) {
      final Extra<?> removed = this.extras.remove(e);
      if (removed != null) {
        removed.detachedToTopic(this);
      }
      result |= removed != null;
    }
    return result;
  }

  public void setExtra(final Extra<?>... extras) {
    for (final Extra<?> e : ensureNoNullElement(extras)) {
      final Extra<?> previous = this.extras.put(e.getType(), e);
      if (previous != null && previous != e) {
        previous.detachedToTopic(this);
      }
      e.attachedToTopic(this);
    }
  }

  public boolean makeFirst() {
    final Topic theParent = this.parent;
    if (theParent != null) {
      int thatIndex = theParent.children.indexOf(this);
      if (thatIndex > 0) {
        theParent.children.remove(thatIndex);
        theParent.children.add(0, this);
        return true;
      }
    }
    return false;
  }

  public boolean hasAncestor(final Topic topic) {
    Topic parent = this.parent;
    while (parent != null) {
      if (parent == topic) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  public boolean makeLast() {
    final Topic theParent = this.parent;
    if (theParent != null) {
      int thatIndex = theParent.children.indexOf(this);
      if (thatIndex >= 0 && thatIndex != theParent.children.size() - 1) {
        theParent.children.remove(thatIndex);
        theParent.children.add(this);
        return true;
      }
    }
    return false;
  }

  public void moveBefore(final Topic topic) {
    final Topic theParent = this.parent;
    if (theParent != null) {
      int thatIndex = theParent.children.indexOf(topic);
      final int thisIndex = theParent.children.indexOf(this);

      if (thatIndex > thisIndex) {
        thatIndex--;
      }

      if (thatIndex >= 0 && thisIndex >= 0) {
        theParent.children.remove(this);
        theParent.children.add(thatIndex, this);
      }
    }
  }

  public String findAttributeInAncestors(final String attrName) {
    String result = null;
    Topic current = this.parent;
    while (result == null && current != null) {
      result = current.getAttribute(attrName);
      current = current.parent;
    }
    return result;
  }

  public void moveAfter(final Topic topic) {
    final Topic theParent = this.parent;
    if (theParent != null) {
      int thatIndex = theParent.children.indexOf(topic);
      int thisIndex = theParent.children.indexOf(this);

      if (thatIndex > thisIndex) {
        thatIndex--;
      }

      if (thatIndex >= 0 && thisIndex >= 0) {
        theParent.children.remove(this);
        theParent.children.add(thatIndex + 1, this);
      }
    }
  }

  public void write(final Writer out) throws IOException {
    write(1, out);
  }

  private void write(final int level, final Writer out) throws IOException {
    out.append(NEXT_LINE);
    ModelUtils.repeatChar(out, '#', level);
    out.append(' ').append(ModelUtils.escapeMarkdown(this.text)).append(NEXT_LINE);

    if (!this.attributes.isEmpty() || !this.extras.isEmpty()) {
      final Map<String, String> attributesToWrite = new HashMap<>(this.attributes);
      for (final Map.Entry<Extra.ExtraType, Extra<?>> e : this.extras.entrySet()) {
        e.getValue().addAttributesForWrite(attributesToWrite);
      }

      if (!attributesToWrite.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(attributesToWrite)).append(NEXT_LINE)
            .append(NEXT_LINE);
      }
    }

    if (!this.extras.isEmpty()) {
      final List<Extra.ExtraType> types = new ArrayList<>(this.extras.keySet());
      types.sort(Comparator.comparing(Enum::name));

      for (final Extra.ExtraType e : types) {
        this.extras.get(e).write(out);
        out.append(NEXT_LINE);
      }
    }

    if (!this.codeSnippets.isEmpty()) {
      for (final Map.Entry<String, String> snippet : this.codeSnippets.entrySet()) {
        final String body = snippet.getValue();
        out.append("```").append(snippet.getKey()).append(NEXT_LINE);
        out.append(body);
        if (!body.endsWith("\n")) {
          out.append(NEXT_LINE);
        }
        out.append("```").append(NEXT_LINE);
      }
    }

    for (final Topic t : this.children) {
      t.write(level + 1, out);
    }
  }

  /**
   * Sort child topics by provided comparator.
   *
   * @param topicComparator comparator to be used for sort, must not be null.
   * @param sortChildren    flag if true then child elements also must be sorted
   * @since 1.6.0
   */
  public void sortChildren(final Comparator<Topic> topicComparator, final boolean sortChildren) {
    this.children.sort(topicComparator);
    if (sortChildren) {
      this.children.forEach(x -> x.sortChildren(topicComparator, true));
    }
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.localUID);
  }

  @Override
  public boolean equals(final Object topic) {
    if (this == topic) {
      return true;
    }
    if (topic instanceof Topic) {
      return this.localUID == ((Topic) topic).localUID;
    }
    return false;
  }

  @Override
  public String toString() {
    return "MindMapTopic('" + this.text + ':' + this.getLocalUid() + "')";
  }

  public long getLocalUid() {
    return this.localUID;
  }

  public boolean isEmpty() {
    return this.children.isEmpty();
  }

  boolean removeAllLinksTo(final Topic topic) {
    boolean result = false;
    if (topic != null) {
      final String uid = topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
      if (uid != null) {
        final ExtraTopic link = (ExtraTopic) this.getExtras().get(Extra.ExtraType.TOPIC);
        if (link != null && uid.equals(link.getValue())) {
          this.removeExtra(Extra.ExtraType.TOPIC);
          result = true;
        }
      }

      for (final Topic ch : this.children) {
        result |= ch.removeAllLinksTo(topic);
      }
    }

    return result;
  }

  boolean removeTopic(final Topic topic) {
    if (topic == null) {
      return false;
    }
    final Iterator<Topic> iterator = this.children.iterator();
    while (iterator.hasNext()) {
      final Topic t = iterator.next();
      if (t == topic) {
        iterator.remove();
        topic.parent = null;
        return true;
      } else if (t.removeTopic(topic)) {
        return true;
      }
    }
    return false;
  }

  public void removeAllChildren() {
    for (final Topic child : this.children) {
      child.parent = null;
    }
    this.children.clear();
  }

  public boolean moveToNewParent(final Topic newParent) {
    if (newParent == null || this == newParent || this.getParent() == newParent
        || newParent.hasAncestor(this) || this.containTopic(newParent)) {
      return false;
    }

    final Topic theParent = this.parent;
    if (theParent != null) {
      theParent.children.remove(this);
    }
    newParent.children.add(this);
    this.parent = newParent;

    return true;
  }

  public Topic makeChild(final String text, final Topic afterTheTopic) {
    final Topic result = new Topic(this.map, this, MiscUtils.ensureNotNull(text, ""));
    if (afterTheTopic != null && this.children.contains(afterTheTopic)) {
      result.moveAfter(afterTheTopic);
    }
    return result;
  }

  public Topic findNext(final Predicate<Topic> checker) {
    final Topic current = this.getParent();
    if (current == null) {
      return null;
    }
    final int indexThis = current.children.indexOf(this);
    if (indexThis < 0) {
      return null;
    }
    return current.children.subList(indexThis + 1, current.children.size()).stream()
        .filter(candidate -> checker == null || checker.test(candidate))
        .findFirst()
        .orElse(null);
  }

  public Topic findPrev(final Predicate<Topic> checker) {
    final Topic current = this.getParent();
    if (current == null) {
      return null;
    }
    final int indexThis = current.children.indexOf(this);
    if (indexThis < 0) {
      return null;
    }
    for (int i = indexThis - 1; i >= 0; i--) {
      final Topic candidate = current.children.get(i);
      if (checker == null || checker.test(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  public void removeExtras(final Extra<?>... extras) {
    if (extras == null || extras.length == 0) {
      for (final Extra<?> e : this.extras.values()) {
        e.detachedToTopic(this);
      }
      this.extras.clear();
    } else {
      for (final Extra<?> e : extras) {
        if (e != null) {
          final Extra<?> removed = this.extras.remove(e.getType());
          if (removed != null) {
            removed.detachedToTopic(this);
          }
        }
      }
    }
  }

  /**
   * Find max length of children chain. It doesn't count the root topic.
   *
   * @return max length of child chain, 0 if no children.
   */
  public int findMaxChildPathLength() {
    return this.getChildren().stream()
        .mapToInt(child -> child.findMaxChildPathLength() + 1)
        .max()
        .orElse(0);
  }

  /**
   * Find among subtree first topic contains attibute with value
   *
   * @param attributeName  name of attribute, must not be null
   * @param attributeValue value to be checked, must not be null
   * @return first topic in subtree with such attribute value, null if not found
   */
  public Topic findForAttribute(final String attributeName, final String attributeValue) {
    if (attributeValue.equals(this.getAttribute(attributeName))) {
      return this;
    }
    return this.children.stream()
        .map(child -> child.findForAttribute(attributeName, attributeValue))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Make position path to the topic
   *
   * @return array of indexes in parents, must not be null
   */
  public int[] getPositionPath() {
    final Topic[] path = this.getPath();
    final int[] result = new int[path.length];

    Topic current = path[0];
    int index = 1;
    while (index < path.length) {
      final Topic next = path[index];
      final int theindex = current.children.indexOf(next);
      result[index++] = theindex;
      if (theindex < 0) {
        break;
      }
      current = next;
    }

    return result;
  }

  /**
   * Collect all ancestors of the topic and build path
   *
   * @return path to the topic with all ancestors, must not be null
   */
  public Topic[] getPath() {
    final List<Topic> list = new ArrayList<>();
    Topic current = this;
    do {
      list.add(0, current);
      current = current.parent;
    }
    while (current != null);
    return list.toArray(new Topic[0]);
  }

  /**
   * Make copy of the topic in the target mind map
   *
   * @param targetMindMap target mind map, must not be null
   * @param parent        parent topic, can be null
   * @param withChildren  flag shows that subtree must be also copied if true
   * @return result topic, must not be null
   */
  public Topic makeCopy(final MindMap targetMindMap, final Topic parent,
                        final boolean withChildren) {
    final Topic newTopic = new Topic(
        targetMindMap,
        parent,
        this.text,
        this.extras.values().toArray(new Extra<?>[0])
    );
    if (withChildren) {
      for (final Topic c : this.children) {
        c.makeCopy(targetMindMap, newTopic, withChildren);
      }
    }
    newTopic.attributes.putAll(this.attributes);
    newTopic.codeSnippets.putAll(this.codeSnippets);

    return newTopic;
  }

  /**
   * Make copy of the topic and its subtree in the target mind map
   *
   * @param targetMindMap target mind map, must not be null
   * @param parent        parent topic, can be null
   * @return result topic, must not be null
   */
  public Topic makeCopy(final MindMap targetMindMap, final Topic parent) {
    return this.makeCopy(targetMindMap, parent, true);
  }

  /**
   * Remove all extras from the topic
   *
   * @param includeSubtree if true then remove all extras from all subtree also
   * @param types          extra types to be removed
   * @return true if any extra was found and removed, false otherwise
   */
  public boolean removeAllExtras(
      final boolean includeSubtree,
      final Extra.ExtraType... types) {
    boolean result = false;

    for (final Extra.ExtraType t : types) {
      final Extra<?> removed = this.extras.remove(t);
      if (removed != null) {
        removed.detachedToTopic(this);
        result = true;
      }
    }
    if (includeSubtree) {
      for (final Topic c : this.children) {
        result |= c.removeAllExtras(includeSubtree, types);
      }
    }
    return result;
  }

  /**
   * Clear all attributes of the topic.
   */
  public void clearAttributes() {
    this.attributes.clear();
  }

  /**
   * Remove all attributes from topic
   *
   * @param includeSubtree if true then remove all attributes from subtree
   * @param attributeNames names of attributes to be removed
   * @return true if any attribute was found and removed
   */
  public boolean removeAttributes(
      final boolean includeSubtree,
      final String... attributeNames
  ) {
    boolean result = false;

    for (final String name : attributeNames) {
      result |= this.attributes.remove(name) != null;
    }
    if (includeSubtree) {
      for (final Topic c : this.children) {
        result |= c.removeAttributes(includeSubtree, attributeNames);
      }
    }
    return result;
  }

  /**
   * Remove file link from the topic if presented
   *
   * @param baseFolder base mind map folder, can be null
   * @param fileUri    file URI to be removed, can't be null
   * @return true if file was detected and removed, false otherwise
   */
  public boolean deleteFileLinkIfPresented(final File baseFolder,
                                           final MMapURI fileUri) {
    boolean result = false;
    if (this.extras.containsKey(Extra.ExtraType.FILE)) {
      final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
      if (fileLink.isSameOrHasParent(baseFolder, fileUri)) {
        result = this.removeExtra(Extra.ExtraType.FILE);
      }
    }
    for (final Topic c : this.children) {
      result |= c.deleteFileLinkIfPresented(baseFolder, fileUri);
    }
    return result;
  }

  /**
   * Replace file link in the topic if found
   *
   * @param baseFolder base mind map folder, can be null
   * @param oldFileUri file uri to be replaced, must not be null
   * @param newFileUri new file uri, must not be null
   * @return true if file link found and replaced, false otherwise
   */
  public boolean replaceFileLinkIfPresented(final File baseFolder,
                                            final MMapURI oldFileUri,
                                            final MMapURI newFileUri) {
    boolean result = false;
    if (this.extras.containsKey(Extra.ExtraType.FILE)) {
      final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
      final ExtraFile replacement;

      if (fileLink.isSame(baseFolder, oldFileUri)) {
        replacement = new ExtraFile(newFileUri);
      } else {
        replacement = fileLink.replaceParentPath(baseFolder, oldFileUri, newFileUri);
      }

      if (replacement != null) {
        result = true;
        this.setExtra(replacement);
      }
    }

    for (final Topic c : this.children) {
      result |= c.replaceFileLinkIfPresented(baseFolder, oldFileUri, newFileUri);
    }
    return result;
  }

  /**
   * Check that the topic contains file uri
   *
   * @param baseFolder     mind map base folder, can be null
   * @param fileUri        file uri to check, must not be null
   * @param includeSubtree check subtree also if true
   * @return true if file uri detected, false otherwise
   */
  public boolean doesContainFileLink(final File baseFolder, final MMapURI fileUri,
                                     final boolean includeSubtree) {
    final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
    if (fileLink != null && fileLink.isSame(baseFolder, fileUri)) {
      return true;
    }
    return includeSubtree && this.children.stream()
        .anyMatch(child -> child.doesContainFileLink(baseFolder, fileUri, includeSubtree));
  }

  @Override
  public Iterator<Topic> iterator() {
    final Iterator<Topic> childrenIterator = this.children.iterator();

    return new Iterator<Topic>() {
      Topic childTopic;
      Iterator<Topic> childIterator;

      @Override
      public void remove() {
        childrenIterator.remove();
      }

      Iterator<Topic> init() {
        if (childrenIterator.hasNext()) {
          this.childTopic = childrenIterator.next();
        }
        return this;
      }

      @Override
      public boolean hasNext() {
        return childrenIterator.hasNext() || this.childTopic != null ||
            (this.childIterator != null && this.childIterator.hasNext());
      }

      @Override
      public Topic next() {
        final Topic result;
        if (this.childTopic != null) {
          result = this.childTopic;
          this.childTopic = null;
          this.childIterator = result.iterator();
        } else if (this.childIterator != null) {
          if (this.childIterator.hasNext()) {
            result = this.childIterator.next();
          } else {
            result = childrenIterator.next();
            this.childIterator = result.iterator();
          }
        } else {
          throw new NoSuchElementException();
        }
        return result;
      }
    }.init();
  }

  /**
   * Check that the topic contains any code snippet for language from array (case sensitive).
   *
   * @param languageNames names of language
   * @return true if code snippet is detected for any language, false otherwise
   */
  public boolean doesContainCodeSnippetForAnyLanguage(
      final String... languageNames) {
    return Arrays.stream(languageNames).anyMatch(this.codeSnippets::containsKey);
  }

  /**
   * Get all subtree of the topic as stream
   *
   * @return stream of subtree for the topic, must not be null
   */
  public Stream<Topic> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  /**
   * Get number of children in the topic
   *
   * @return number of children
   */
  public int size() {
    return this.children.size();
  }

  private static final class TopicParser {

    private final MindMap map;
    private final MindMapLexer lexer;
    private final boolean ignoreErrors;
    private Topic topic;
    private int depth;
    private Extra.ExtraType extraType;
    private String codeSnippet;
    private StringBuilder codeSnippetBody;
    private int detectedLevel = -1;

    private TopicParser(final MindMap map, final MindMapLexer lexer, final boolean ignoreErrors) {
      this.map = map;
      this.lexer = lexer;
      this.ignoreErrors = ignoreErrors;
    }

    private Topic parse() {
      while (this.advance()) {
        this.processToken();
      }
      return this.topic == null ? null : this.topic.getRoot();
    }

    private boolean advance() {
      final int oldOffset = this.lexer.getCurrentPosition().getOffset();
      this.lexer.advance();
      return this.lexer.getTokenType() != null
          && oldOffset != this.lexer.getCurrentPosition().getOffset();
    }

    private void processToken() {
      switch (this.lexer.getTokenType()) {
        case TOPIC_LEVEL:
          this.detectedLevel = ModelUtils.countPrefixChars('#', this.lexer.getTokenText());
          break;
        case TOPIC_TITLE:
          this.processTopicTitle();
          break;
        case EXTRA_TYPE:
          this.processExtraType();
          break;
        case CODE_SNIPPET_START:
          this.processCodeSnippetStart();
          break;
        case CODE_SNIPPET_BODY:
          if (this.codeSnippetBody != null) {
            this.codeSnippetBody.append(this.lexer.getTokenText());
          }
          break;
        case CODE_SNIPPET_END:
          this.processCodeSnippetEnd();
          break;
        case ATTRIBUTE:
          this.processAttribute();
          break;
        case EXTRA_TEXT:
          this.processExtraText();
          break;
        case UNKNOWN_LINE:
          if (this.topic != null && this.extraType != null) {
            this.extraType = null;
          }
          break;
        default:
          break;
      }
    }

    private void processTopicTitle() {
      final String newTopicText =
          ModelUtils.unescapeMarkdown(ModelUtils.removeISOControls(this.lexer.getTokenText()));

      if (this.detectedLevel == this.depth + 1) {
        this.depth = this.detectedLevel;
        this.topic = new Topic(this.map, this.topic, newTopicText);
      } else if (this.detectedLevel == this.depth) {
        this.topic = new Topic(this.map,
            this.topic == null ? null : this.topic.getParent(),
            newTopicText);
      } else if (this.detectedLevel < this.depth) {
        if (this.topic != null) {
          this.topic = this.topic.findParentForDepth(this.depth - this.detectedLevel);
          this.topic = new Topic(this.map, this.topic, newTopicText);
          this.depth = this.detectedLevel;
        }
      } else if (this.detectedLevel > this.depth + 1 && this.topic != null) {
        this.depth = this.detectedLevel;
        this.topic = new Topic(this.map, this.topic, newTopicText);
      }
    }

    private void processExtraType() {
      try {
        this.extraType = Extra.ExtraType.valueOf(this.lexer.getTokenText().substring(1).trim());
      } catch (final IllegalArgumentException ex) {
        this.extraType = null;
      }
    }

    private void processCodeSnippetStart() {
      if (this.topic != null) {
        this.codeSnippet = this.lexer.getTokenText().substring(3);
        this.codeSnippetBody = new StringBuilder();
      }
    }

    private void processCodeSnippetEnd() {
      if (this.topic != null && this.codeSnippet != null && this.codeSnippetBody != null) {
        this.topic.codeSnippets.put(this.codeSnippet.trim(), this.codeSnippetBody.toString());
      }
      this.codeSnippet = null;
      this.codeSnippetBody = null;
    }

    private void processAttribute() {
      if (this.topic != null) {
        MindMap.fillMapByAttributes(this.lexer.getTokenText().trim(), this.topic.attributes);
      }
      this.extraType = null;
    }

    private void processExtraText() {
      if (this.topic == null || this.extraType == null) {
        return;
      }
      try {
        final String text = this.lexer.getTokenText();
        final String groupPre =
            this.extraType.preprocessString(text.substring(5, text.length() - 6));
        if (groupPre != null) {
          this.topic.setExtra(this.extraType.parseLoaded(groupPre, this.topic.attributes));
        } else if (!this.ignoreErrors) {
          throw new IllegalStateException("Detected invalid extra data " + this.extraType);
        }
      } catch (final Exception ex) {
        if (!this.ignoreErrors) {
          if (ex instanceof IllegalStateException) {
            throw (IllegalStateException) ex;
          }
          throw new Error("Unexpected exception #23241", ex);
        }
      } finally {
        this.extraType = null;
      }
    }
  }
}
