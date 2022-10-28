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

import static com.igormaznitsa.mindmap.model.MiscUtils.ensureDoesntHaveNull;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public final class Topic implements Serializable, Constants, Iterable<Topic> {

  private static final long serialVersionUID = -4642569244907433215L;
  private static final AtomicLong LOCALUID_GENERATOR = new AtomicLong();
  private final EnumMap<Extra.ExtraType, Extra<?>> extras =
      new EnumMap<>(Extra.ExtraType.class);
  private final Map<Extra.ExtraType, Extra<?>> unmodifableExtras =
      Collections.unmodifiableMap(this.extras);
  private final Map<String, String> attributes =
      new TreeMap<>(ModelUtils.STRING_COMPARATOR);
  private final Map<String, String> unmodifableAttributes =
      Collections.unmodifiableMap(this.attributes);
  private final Map<String, String> codeSnippets =
      new TreeMap<>(ModelUtils.STRING_COMPARATOR);
  private final Map<String, String> unmodifableCodeSnippets =
      Collections.unmodifiableMap(this.codeSnippets);
  private final List<Topic> children = new ArrayList<>();
  private final List<Topic> unmodifableChildren = Collections.unmodifiableList(this.children);
  private final transient long localUID = LOCALUID_GENERATOR.getAndIncrement();
  private final MindMap map;
  private Topic parent;
  private volatile String text;
  private transient Object payload;

  /**
   * Constructor to build topic on base of another topic for another mind map.
   *
   * @param mindMap      mind map to be owner for new topic
   * @param base         base souce topic
   * @param copyChildren flag to make copy of children, true if to make copy,
   *                     false otherwise
   * @since 1.2.2
   */
  public Topic(final MindMap mindMap, final Topic base,
               final boolean copyChildren) {
    this(mindMap, base.text);
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

  public Topic(final MindMap map, final Topic parent, final String text,
               final Extra<?>... extras) {
    this(map, text, extras);
    this.parent = parent;

    if (parent != null) {
      if (parent.getMap() != map) {
        throw new IllegalArgumentException("Parent must belong to the same mind map");
      }
      parent.children.add(this);
    }
  }

  private Topic(final MindMap map, final String text,
                final Extra<?>... extras) {
    this.map = requireNonNull(map);
    this.text = requireNonNull(text);

    for (final Extra<?> e : extras) {
      if (e != null) {
        this.extras.put(e.getType(), e);
      }
    }
  }

  public static Topic parse(final MindMap map, final MindMapLexer lexer,
                            final boolean ignoreErrors) {
    map.lock();
    try {
      Topic topic = null;
      int depth = 0;

      Extra.ExtraType extraType = null;

      String codeSnippetlanguage = null;
      StringBuilder codeSnippetBody = null;

      int detectedLevel = -1;

      while (true) {
        final int oldLexerPosition = lexer.getCurrentPosition().getOffset();
        lexer.advance();
        final boolean lexerPositionWasNotChanged =
            oldLexerPosition == lexer.getCurrentPosition().getOffset();

        final MindMapLexer.TokenType token = lexer.getTokenType();
        if (token == null || lexerPositionWasNotChanged) {
          break;
        }

        switch (token) {
          case TOPIC_LEVEL: {
            final String tokenText = lexer.getTokenText();
            detectedLevel = ModelUtils.calcCharsOnStart('#', tokenText);
          }
          break;
          case TOPIC_TITLE: {
            final String tokenText = ModelUtils.removeISOControls(lexer.getTokenText());
            final String newTopicText = ModelUtils.unescapeMarkdownStr(tokenText);

            if (detectedLevel == depth + 1) {
              depth = detectedLevel;
              topic = new Topic(map, topic, newTopicText);
            } else if (detectedLevel == depth) {
              topic = new Topic(map, topic == null ? null : topic.getParent(), newTopicText);
            } else if (detectedLevel < depth) {
              if (topic != null) {
                topic = topic.findParentForDepth(depth - detectedLevel);
                topic = new Topic(map, topic, newTopicText);
                depth = detectedLevel;
              }
            }

          }
          break;
          case EXTRA_TYPE: {
            final String extraName = lexer.getTokenText().substring(1).trim();
            try {
              extraType = Extra.ExtraType.valueOf(extraName);
            } catch (IllegalArgumentException ex) {
              extraType = null;
            }
          }
          break;
          case CODE_SNIPPET_START: {
            if (topic != null) {
              codeSnippetlanguage = lexer.getTokenText().substring(3);
              codeSnippetBody = new StringBuilder();
            }
          }
          break;
          case CODE_SNIPPET_BODY: {
            codeSnippetBody.append(lexer.getTokenText());
          }
          break;
          case CODE_SNIPPET_END: {
            if (topic != null && codeSnippetlanguage != null && codeSnippetBody != null) {
              topic.codeSnippets.put(codeSnippetlanguage.trim(), codeSnippetBody.toString());
            }
            codeSnippetlanguage = null;
            codeSnippetBody = null;
          }
          break;
          case ATTRIBUTE: {
            if (topic != null) {
              final String text = lexer.getTokenText().trim();
              MindMap.fillMapByAttributes(text, topic.attributes);
            }
            extraType = null;
          }
          break;
          case EXTRA_TEXT: {
            if (topic != null && extraType != null) {
              try {
                final String text = lexer.getTokenText();
                final String groupPre =
                    extraType.preprocessString(text.substring(5, text.length() - 6));
                if (groupPre != null) {
                  topic.setExtra(extraType.parseLoaded(groupPre, topic.attributes));
                } else {
                  if (!ignoreErrors) {
                    throw new IllegalStateException("Detected invalid extra data " + extraType);
                  }
                }
              } catch (Exception ex) {
                throw new Error("Unexpected exception #23241", ex);
              } finally {
                extraType = null;
              }
            }
          }
          break;
          case UNKNOWN_LINE: {
            if (topic != null && extraType != null) {
              extraType = null;
            }
          }
          break;
          default:
            break;
        }
      }
      return topic == null ? null : topic.getRoot();
    } finally {
      map.unlock();
    }
  }

  public Topic findRoot() {
    Topic result = this;
    while (!result.isRoot()) {
      result = result.getParent();
    }
    return result;
  }

  public boolean containTopic(final Topic topic) {
    boolean result = false;

    if (this == topic) {
      result = true;
    } else {
      for (final Topic t : this.children) {
        if (t.containTopic(topic)) {
          result = true;
          break;
        }
      }
    }
    return result;
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
    boolean result = false;

    if (findInTopicText && pattern.matcher(this.text).find()) {
      result = true;
    } else if (extrasForSearch != null && !extrasForSearch.isEmpty()) {
      for (final Extra<?> e : this.extras.values()) {
        if (extrasForSearch.contains(e.getType()) && e.containsPattern(baseFolder, pattern)) {
          result = true;
          break;
        }
      }
    }
    return result;
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

  public Topic findParentForDepth(int depth) {
    this.map.lock();
    try {
      Topic result = this.parent;
      while (depth > 0 && result != null) {
        result = result.parent;
        depth--;
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public Topic getRoot() {
    this.map.lock();
    try {
      Topic result = this;
      while (true) {
        final Topic prev = result.parent;
        if (prev == null) {
          break;
        }
        result = prev;
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public Topic getFirst() {
    return this.children.isEmpty() ? null : this.children.get(0);
  }

  public Topic getLast() {
    return this.children.isEmpty() ? null : this.children.get(this.children.size() - 1);
  }

  public List<Topic> getChildren() {
    return this.unmodifableChildren;
  }

  public int getNumberOfExtras() {
    return this.extras.size();
  }

  public Map<Extra.ExtraType, Extra<?>> getExtras() {
    return this.unmodifableExtras;
  }

  public Extra<?>[] extrasToArray() {
    final Collection<Extra<?>> collection = this.unmodifableExtras.values();
    return collection.toArray(new Extra<?>[0]);
  }

  public Map<String, String> getAttributes() {
    return this.unmodifableAttributes;
  }

  public Map<String, String> getCodeSnippets() {
    return this.unmodifableCodeSnippets;
  }

  public boolean setAttribute(final String name, final String value) {
    this.map.lock();
    try {
      if (value == null) {
        return this.attributes.remove(name) != null;
      } else {
        return !value.equals(this.attributes.put(name, value));
      }
    } finally {
      this.map.unlock();
    }
  }

  public boolean setCodeSnippet(final String language, final String text) {
    this.map.lock();
    try {
      if (text == null) {
        return this.codeSnippets.remove(language) != null;
      } else {
        return !text.equals(this.codeSnippets.put(language, text));
      }
    } finally {
      this.map.unlock();
    }
  }

  public String getCodeSnippet(final String language) {
    return this.codeSnippets.get(language);
  }

  public String getAttribute(final String name) {
    return this.attributes.get(name);
  }

  public void delete() {
    this.map.lock();
    try {
      final Topic theParent = this.parent;
      if (theParent != null) {
        theParent.children.remove(this);
      }
    } finally {
      this.map.unlock();
    }
  }

  public Topic getParent() {
    return this.parent;
  }

  public String getText() {
    return this.text;
  }

  public void setText(final String text) {
    this.map.lock();
    try {
      this.text = requireNonNull(text);
    } finally {
      this.map.unlock();
    }
  }

  public boolean isFirstChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(0) == t;
  }

  public boolean isLastChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(this.children.size() - 1) == t;
  }

  public boolean removeExtra(final Extra.ExtraType... types) {
    this.map.lock();
    try {
      boolean result = false;
      for (final Extra.ExtraType e : ensureDoesntHaveNull(types)) {
        final Extra<?> removed = this.extras.remove(e);
        if (removed != null) {
          removed.detachedToTopic(this);
        }
        result |= removed != null;
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public void setExtra(final Extra<?>... extras) {
    this.map.lock();
    try {
      for (final Extra<?> e : ensureDoesntHaveNull(extras)) {
        this.extras.put(e.getType(), e);
        e.attachedToTopic(this);
      }
    } finally {
      this.map.unlock();
    }
  }

  public boolean makeFirst() {
    this.map.lock();
    try {
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
    } finally {
      this.map.unlock();
    }
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
    this.map.lock();
    try {
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
    } finally {
      this.map.unlock();
    }
  }

  public void moveBefore(final Topic topic) {
    this.map.lock();
    try {
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
    } finally {
      this.map.unlock();
    }
  }

  public String findAttributeInAncestors(final String attrName) {
    this.map.lock();
    try {
      String result = null;
      Topic current = this.parent;
      while (result == null && current != null) {
        result = current.getAttribute(attrName);
        current = current.parent;
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public void moveAfter(final Topic topic) {
    this.map.lock();
    try {
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
    } finally {
      this.map.unlock();
    }
  }

  public void write(final Writer out) throws IOException {
    this.map.lock();
    try {
      write(1, out);
    } finally {
      this.map.unlock();
    }
  }

  private void write(final int level, final Writer out) throws IOException {
    out.append(NEXT_LINE);
    ModelUtils.writeChar(out, '#', level);
    out.append(' ').append(ModelUtils.escapeMarkdownStr(this.text)).append(NEXT_LINE);

    if (!this.attributes.isEmpty() || !this.extras.isEmpty()) {
      final Map<String, String> attributesToWrite = new HashMap<>(this.attributes);
      for (final Map.Entry<Extra.ExtraType, Extra<?>> e : this.extras.entrySet()) {
        e.getValue().addAttributesForWrite(attributesToWrite);
      }

      if (!attributesToWrite.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(attributesToWrite)).append(NEXT_LINE)
            .append(NEXT_LINE); //NOI18N
      }
    }

    if (!this.extras.entrySet().isEmpty()) {
      final List<Extra.ExtraType> types = new ArrayList<>(this.extras.keySet());
      types.sort(Comparator.comparing(Enum::name));

      for (final Extra.ExtraType e : types) {
        this.extras.get(e).write(out);
        out.append(NEXT_LINE);
      }
    }

    if (!this.codeSnippets.isEmpty()) {
      final List<String> sortedKeys = new ArrayList<>(this.codeSnippets.keySet());
      Collections.sort(sortedKeys);
      for (final String language : sortedKeys) {
        final String body = this.codeSnippets.get(language);
        out.append("```").append(language).append(NEXT_LINE);
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

  @Override
  public int hashCode() {
    return (int) ((this.localUID >>> 32) ^ (this.localUID & 0xFFFFFFFFL));
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
    return "MindMapTopic('" + this.text + ':' + this.getLocalUid() + "')"; //NOI18N
  }

  public long getLocalUid() {
    return this.localUID;
  }

  public boolean hasChildren() {
    this.map.lock();
    try {
      return !this.children.isEmpty();
    } finally {
      this.map.unlock();
    }
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
        return true;
      } else if (t.removeTopic(topic)) {
        return true;
      }
    }
    return false;
  }

  public void removeAllChildren() {
    this.children.clear();
  }

  public boolean moveToNewParent(final Topic newParent) {
    this.map.lock();
    try {
      if (newParent == null || this == newParent || this.getParent() == newParent ||
          this.children.contains(newParent)) {
        return false;
      }

      final Topic theParent = this.parent;
      if (theParent != null) {
        theParent.children.remove(this);
      }
      newParent.children.add(this);
      this.parent = newParent;

      return true;
    } finally {
      this.map.unlock();
    }
  }

  public Topic makeChild(final String text, final Topic afterTheTopic) {
    this.map.lock();
    try {
      final Topic result = new Topic(this.map, this, MiscUtils.ensureNotNull(text, "")); //NOI18N
      if (afterTheTopic != null && this.children.contains(afterTheTopic)) {
        result.moveAfter(afterTheTopic);
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public Topic findNext(final TopicChecker checker) {
    this.map.lock();
    try {
      Topic result = null;
      Topic current = this.getParent();
      if (current != null) {
        final int indexThis = current.children.indexOf(this);
        if (indexThis >= 0) {
          for (int i = indexThis + 1; i < current.children.size(); i++) {
            if (checker == null) {
              result = current.children.get(i);
              break;
            } else if (checker.check(current.children.get(i))) {
              result = current.children.get(i);
              break;
            }
          }
        }
      }

      return result;
    } finally {
      this.map.unlock();
    }
  }

  public Topic findPrev(final TopicChecker checker) {
    this.map.lock();
    try {
      Topic result = null;
      Topic current = this.getParent();
      if (current != null) {
        final int indexThis = current.children.indexOf(this);
        if (indexThis >= 0) {
          for (int i = indexThis - 1; i >= 0; i--) {
            if (checker.check(current.children.get(i))) {
              result = current.children.get(i);
              break;
            }
          }
        }
      }

      return result;
    } finally {
      this.map.unlock();
    }
  }

  public void removeExtras(final Extra<?>... extras) {
    this.map.lock();
    try {
      if (extras == null || extras.length == 0) {
        this.extras.clear();
      } else {
        for (final Extra<?> e : extras) {
          if (e != null) {
            this.extras.remove(e.getType());
          }
        }
      }
    } finally {
      this.map.unlock();
    }
  }

  /**
   * Find max length of children chain. It doesn't count the root topic.
   *
   * @return max length of child chain, 0 if no children.
   * @since 1.4.11
   */
  public int findMaxChildPathLength() {
    int len = 0;
    for (final Topic t : this.getChildren()) {
      final int childLen = t.findMaxChildPathLength();
      len = Math.max(len, childLen + 1);
    }
    return len;
  }

  public Topic findForAttribute(final String attrName, String value) {
    if (value.equals(this.getAttribute(attrName))) {
      return this;
    }
    Topic result = null;
    for (final Topic c : this.children) {
      result = c.findForAttribute(attrName, value);
      if (result != null) {
        break;
      }
    }
    return result;
  }

  public int[] getPositionPath() {
    final Topic[] path = getPath();
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

  Topic makeCopy(final MindMap newMindMap, final Topic parent) {
    this.map.lock();
    try {
      final Topic result = new Topic(newMindMap, parent, this.text,
          this.extras.values().toArray(new Extra<?>[0]));
      for (final Topic c : this.children) {
        c.makeCopy(newMindMap, result);
      }
      result.attributes.putAll(this.attributes);
      result.codeSnippets.putAll(this.codeSnippets);

      return result;
    } finally {
      this.map.unlock();
    }
  }

  public boolean removeExtraFromSubtree(
      final Extra.ExtraType... type) {
    boolean result = false;

    this.map.lock();
    try {
      for (final Extra.ExtraType t : type) {
        result |= this.extras.remove(t) != null;
      }
      for (final Topic c : this.children) {
        result |= c.removeExtraFromSubtree(type);
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public boolean removeAttributeFromSubtree(final String... names) {
    boolean result = false;

    this.map.lock();
    try {
      for (final String t : names) {
        result |= this.attributes.remove(t) != null;
      }
      for (final Topic c : this.children) {
        result |= c.removeAttributeFromSubtree(names);
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public boolean deleteLinkToFileIfPresented(final File baseFolder,
                                             final MMapURI file) {
    boolean result = false;
    if (this.extras.containsKey(Extra.ExtraType.FILE)) {
      final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
      if (fileLink.isSameOrHasParent(baseFolder, file)) {
        result = this.extras.remove(Extra.ExtraType.FILE) != null;
      }
    }
    for (final Topic c : this.children) {
      result |= c.deleteLinkToFileIfPresented(baseFolder, file);
    }
    return result;
  }

  public boolean replaceLinkToFileIfPresented(final File baseFolder,
                                              final MMapURI oldFile,
                                              final MMapURI newFile) {
    boolean result = false;
    if (this.extras.containsKey(Extra.ExtraType.FILE)) {
      final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
      final ExtraFile replacement;

      if (fileLink.isSame(baseFolder, oldFile)) {
        replacement = new ExtraFile(newFile);
      } else {
        replacement = fileLink.replaceParentPath(baseFolder, oldFile, newFile);
      }

      if (replacement != null) {
        result = true;
        this.extras.remove(Extra.ExtraType.FILE);
        this.extras.put(Extra.ExtraType.FILE, replacement);
      }
    }

    for (final Topic c : this.children) {
      result |= c.replaceLinkToFileIfPresented(baseFolder, oldFile, newFile);
    }
    return result;
  }

  public boolean doesContainFileLink(final File baseFolder, final MMapURI file) {
    if (this.extras.containsKey(Extra.ExtraType.FILE)) {
      final ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
      if (fileLink.isSame(baseFolder, file)) {
        return true;
      }
    }
    for (final Topic c : this.children) {
      if (c.doesContainFileLink(baseFolder, file)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<Topic> iterator() {
    final Iterator<Topic> iter = this.children.iterator();

    return new Iterator<Topic>() {
      Topic childTopic;
      Iterator<Topic> childIterator;

      @Override
      public void remove() {
        iter.remove();
      }

      Iterator<Topic> init() {
        if (iter.hasNext()) {
          this.childTopic = iter.next();
        }
        return this;
      }

      @Override
      public boolean hasNext() {
        return iter.hasNext() || this.childTopic != null ||
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
            result = iter.next();
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
   * Check that the topic contains any code snipet for language from array (case sensitive).
   *
   * @param languageNames names of language
   * @return true if code snippet is detected for any language, false otherwise
   * @since 1.3.1
   */
  public boolean doesContainCodeSnippetForAnyLanguage(
      final String... languageNames) {
    boolean result = false;
    if (!this.codeSnippets.isEmpty()) {
      for (final String s : languageNames) {
        if (this.codeSnippets.containsKey(s)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
}
