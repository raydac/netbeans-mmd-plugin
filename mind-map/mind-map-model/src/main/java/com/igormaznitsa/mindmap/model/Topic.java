/*
 * Copyright 2015 Igor Maznitsa.
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

import java.io.File;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.parser.MindMapLexer;

public final class Topic implements Serializable, Constants, Iterable<Topic> {

  private static final long serialVersionUID = -4512887489914466713L;

  private static Logger logger = LoggerFactory.getLogger(Topic.class);

  private static final AtomicLong LOCALUID_GENERATOR = new AtomicLong();

  @Nullable
  private Topic parent;

  private final EnumMap<Extra.ExtraType, Extra<?>> extras = new EnumMap<Extra.ExtraType, Extra<?>>(Extra.ExtraType.class);
  private final Map<Extra.ExtraType, Extra<?>> unmodifableExtras = Collections.unmodifiableMap(this.extras);
  private final Map<String, String> attributes = new TreeMap<String, String>(MindMap.STRING_COMPARATOR);
  private final Map<String, String> unmodifableAttributes = Collections.unmodifiableMap(this.attributes);

  @Nonnull
  private volatile String text;

  @Nonnull
  private final List<Topic> children = new ArrayList<Topic>();

  @Nonnull
  private final List<Topic> unmodifableChildren = Collections.unmodifiableList(this.children);

  @Nullable
  private transient Object payload;

  private final transient long localUID = LOCALUID_GENERATOR.getAndIncrement();

  @Nonnull
  private final MindMap map;

  /**
   * Constructor to build topic on base of another topic for another mind map.
   * 
   * @param mindMap mind map to be owner for new topic
   * @param base base souce topic
   * @param copyChildren flag to make copy of children, true if to make copy, false otherwise
   * 
   * @since 1.2.2
   */
  public Topic(@Nonnull final MindMap mindMap, @Nonnull final Topic base, final boolean copyChildren) {
    this(mindMap, base.text);
    this.attributes.putAll(base.attributes);
    this.extras.putAll(base.extras);
    
    if (copyChildren){
      for(final Topic t : base.children){
        final Topic clonedChildren = new Topic(mindMap, t, true);
        clonedChildren.parent = this;
        this.children.add(clonedChildren);
      }
    }
  }

  public Topic(@Nonnull final MindMap map, @Nullable final Topic parent, @Nonnull final String text, @Nonnull @MayContainNull final Extra<?>... extras) {
    this(map, text, extras);
    this.parent = parent;
    
    if (parent != null) {
      if (parent.getMap() != map) {
        throw new IllegalArgumentException("Parent must belong to the same mind map");
      }
      parent.children.add(this);
    }
  }

  private Topic(@Nonnull final MindMap map, @Nonnull final String text, @Nonnull @MayContainNull final Extra<?>... extras){
    this.map = Assertions.assertNotNull(map);
    this.text = Assertions.assertNotNull(text);

    for (final Extra<?> e : extras) {
      if (e != null) {
        this.extras.put(e.getType(), e);
      }
    }
  }
  
  public boolean containTopic(@Nonnull final Topic topic) {
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

  @Nullable
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

  @Nullable
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

  public boolean containsPattern(final @Nullable File baseFolder, final @Nonnull Pattern pattern, final boolean findInTopicText, @Nullable final Set<Extra.ExtraType> extrasForSearch) {
    boolean result = false;
    
    if (findInTopicText && pattern.matcher(this.text).find()){
      result = true;
    } else if (extrasForSearch!=null && !extrasForSearch.isEmpty()) {
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

  @Nullable
  public Object getPayload() {
    return this.payload;
  }

  public void setPayload(@Nullable final Object value) {
    this.payload = value;
  }

  @Nonnull
  private Object readResolve() {
    return new Topic(this.map, this, true);
  }

  @Nonnull
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

  @Nullable
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

  @Nonnull
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

  private boolean canBeDeletedSilently() {
    final MindMapController controller = this.map.getController();
    if (controller != null) {
      return controller.canBeDeletedSilently(this.map, this);
    }
    return false;
  }

  public boolean canBeLost() {
    this.map.lock();
    try {
      boolean noImportantContent = this.text.trim().isEmpty() && this.extras.isEmpty() && canBeDeletedSilently();
      if (noImportantContent) {
        for (final Topic t : this.children) {
          noImportantContent &= t.canBeLost();
          if (!noImportantContent) {
            break;
          }
        }
      }
      return noImportantContent;
    } finally {
      this.map.unlock();
    }
  }

  @Nullable
  public static Topic parse(@Nonnull final MindMap map, @Nonnull final MindMapLexer lexer) throws IOException {
    map.lock();
    try {
      Topic topic = null;
      int depth = 0;

      Extra.ExtraType extraType = null;

      while (true) {
        lexer.advance();
        final MindMapLexer.TokenType token = lexer.getTokenType();
        if (token == null) {
          break;
        }
        switch (token) {
          case TOPIC: {
            final String tokenText = lexer.getTokenText();
            final int topicDepth = ModelUtils.calcCharsOnStart('#', tokenText);
            final String newTopicText = ModelUtils.unescapeMarkdownStr(tokenText.substring(topicDepth).trim());

            if (topicDepth == depth + 1) {
              depth = topicDepth;
              topic = new Topic(map, topic, newTopicText);
            } else if (topicDepth == depth) {
              topic = new Topic(map, topic == null ? null : topic.getParent(), newTopicText);
            } else if (topicDepth < depth) {
              if (topic != null) {
                topic = topic.findParentForDepth(depth - topicDepth);
                topic = new Topic(map, topic, newTopicText);
                depth = topicDepth;
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
                final String groupPre = extraType.preprocessString(text.substring(5, text.length() - 6));
                if (groupPre != null) {
                  topic.setExtra(extraType.parseLoaded(groupPre));
                } else {
                  logger.error("Detected invalid extra data " + extraType);
                }
              } catch (Exception ex) {
                logger.error("Unexpected exception #23241", ex); //NOI18N
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

  @Nullable
  public Topic getFirst() {
    return this.children.isEmpty() ? null : this.children.get(0);
  }

  @Nullable
  public Topic getLast() {
    return this.children.isEmpty() ? null : this.children.get(this.children.size() - 1);
  }

  @Nonnull
  @MustNotContainNull
  public List<Topic> getChildren() {
    return this.unmodifableChildren;
  }

  public int getNumberOfExtras() {
    return this.extras.size();
  }

  @Nonnull
  public Map<Extra.ExtraType, Extra<?>> getExtras() {
    return this.unmodifableExtras;
  }

  @Nonnull
  @MustNotContainNull
  public Extra<?>[] extrasToArray(){
    final Collection<Extra<?>> collection = this.unmodifableExtras.values();
    return collection.toArray(new Extra<?>[collection.size()]);
  }
  
  @Nonnull
  public Map<String, String> getAttributes() {
    return this.unmodifableAttributes;
  }

  public boolean setAttribute(@Nonnull final String name, @Nullable final String value) {
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

  @Nullable
  public String getAttribute(@Nonnull final String name) {
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

  @Nullable
  public Topic getParent() {
    return this.parent;
  }

  @Nonnull
  public String getText() {
    return this.text;
  }

  public boolean isFirstChild(@Nonnull final Topic t) {
    return !this.children.isEmpty() && this.children.get(0) == t;
  }

  public boolean isLastChild(@Nonnull final Topic t) {
    return !this.children.isEmpty() && this.children.get(this.children.size() - 1) == t;
  }

  public void setText(@Nonnull final String text) {
    this.map.lock();
    try {
      this.text = Assertions.assertNotNull(text);
    } finally {
      this.map.unlock();
    }
  }

  public boolean removeExtra(@Nonnull @MustNotContainNull final Extra.ExtraType... types) {
    this.map.lock();
    try {
      boolean result = false;
      for (final Extra.ExtraType e : Assertions.assertDoesntContainNull(types)) {
        result |= this.extras.remove(e) != null;
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  public void setExtra(@MustNotContainNull @Nonnull final Extra<?>... extras) {
    this.map.lock();
    try {
      for (final Extra<?> e : Assertions.assertDoesntContainNull(extras)) {
        this.extras.put(e.getType(), e);
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

  public boolean hasAncestor(@Nonnull final Topic topic) {
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

  public void moveBefore(@Nonnull final Topic topic) {
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

  @Nullable
  public String findAttributeInAncestors(@Nonnull final String attrName) {
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

  public void moveAfter(@Nonnull final Topic topic) {
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

  public void write(@Nonnull final Writer out) throws IOException {
    this.map.lock();
    try {
      write(1, out);
    } finally {
      this.map.unlock();
    }
  }

  private void write(final int level, @Nonnull final Writer out) throws IOException {
    out.append(NEXT_LINE);
    ModelUtils.writeChar(out, '#', level);
    out.append(' ').append(ModelUtils.escapeMarkdownStr(this.text)).append(NEXT_LINE);
    if (!this.attributes.isEmpty()) {
      out.append("> ").append(MindMap.allAttributesAsString(this.attributes)).append(NEXT_LINE).append(NEXT_LINE); //NOI18N
    }
    for (final Map.Entry<Extra.ExtraType, Extra<?>> e : this.extras.entrySet()) {
      e.getValue().write(out);
      out.append(NEXT_LINE);
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
  public boolean equals(@Nonnull final Object topic) {
    if (this == topic) {
      return true;
    }
    if (topic instanceof Topic) {
      return this.localUID == ((Topic) topic).localUID;
    }
    return false;
  }

  @Override
  @Nonnull
  public String toString() {
    return "MindMapTopic('" + this.text + "')"; //NOI18N
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

  boolean removeAllLinksTo(@Nullable final Topic topic) {
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

  boolean removeTopic(@Nullable final Topic topic) {
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

  public boolean moveToNewParent(@Nullable final Topic newParent) {
    this.map.lock();
    try {
      if (newParent == null || this == newParent || this.getParent() == newParent || this.children.contains(newParent)) {
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

  @Nonnull
  public Topic makeChild(@Nullable final String text, @Nullable final Topic afterTheTopic) {
    this.map.lock();
    try {
      final Topic result = new Topic(this.map, this, GetUtils.ensureNonNull(text, "")); //NOI18N
      if (afterTheTopic != null && this.children.indexOf(afterTheTopic) >= 0) {
        result.moveAfter(afterTheTopic);
      }
      return result;
    } finally {
      this.map.unlock();
    }
  }

  @Nullable
  public Topic findNext(@Nullable final TopicChecker checker) {
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

  @Nullable
  public Topic findPrev(@Nonnull final TopicChecker checker) {
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

  public void removeExtras(@Nullable @MayContainNull final Extra<?>... extras) {
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

  @Nullable
  public Topic findForAttribute(@Nonnull final String attrName, @Nonnull String value) {
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

  @Nonnull
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

  @Nonnull
  @MustNotContainNull
  public Topic[] getPath() {
    final List<Topic> list = new ArrayList<Topic>();
    Topic current = this;
    do {
      list.add(0, current);
      current = current.parent;
    } while (current != null);
    return list.toArray(new Topic[list.size()]);
  }

  @Nonnull
  Topic makeCopy(@Nonnull final MindMap newMindMap, @Nullable final Topic parent) {
    this.map.lock();
    try {
      final Topic result = new Topic(newMindMap, parent, this.text, this.extras.values().toArray(new Extra<?>[this.extras.values().size()]));
      for (final Topic c : this.children) {
        c.makeCopy(newMindMap, result);
      }
      result.attributes.putAll(this.attributes);

      return result;
    } finally {
      this.map.unlock();
    }
  }

  public boolean removeExtraFromSubtree(@Nonnull @MustNotContainNull final Extra.ExtraType... type) {
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

  public boolean removeAttributeFromSubtree(@Nonnull @MustNotContainNull final String... names) {
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

  public boolean deleteLinkToFileIfPresented(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
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

  public boolean replaceLinkToFileIfPresented(@Nonnull final File baseFolder, @Nonnull final MMapURI oldFile, @Nonnull final MMapURI newFile) {
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

  public boolean doesContainFileLink(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
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
  @Nonnull
  public Iterator<Topic> iterator() {
    final Iterator<Topic> iter = this.children.iterator();

    return new Iterator<Topic>() {
      Topic childTopic;
      Iterator<Topic> childIterator;

      @Override
      public void remove() {
        iter.remove();
      }

      @Nonnull
      Iterator<Topic> init() {
        if (iter.hasNext()) {
          this.childTopic = iter.next();
        }
        return this;
      }

      @Override
      public boolean hasNext() {
        return iter.hasNext() || this.childTopic != null || (this.childIterator != null && this.childIterator.hasNext());
      }

      @Nonnull
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

}
