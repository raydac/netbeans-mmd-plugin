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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Topic implements Serializable, Constants {

  private static final long serialVersionUID = -4512887489914466613L;

  private static Logger logger = LoggerFactory.getLogger(Topic.class);

  
  private static final AtomicLong LOCALUID_GENERATOR = new AtomicLong();

  private Topic parent;
  private final EnumMap<Extra.ExtraType, Extra<?>> extras = new EnumMap<>(Extra.ExtraType.class);
  private final Map<Extra.ExtraType, Extra<?>> unmodifableExtras = Collections.unmodifiableMap(this.extras);
  private final Map<String, String> attributes = new HashMap<>();
  private final Map<String, String> unmodifableAttributes = Collections.unmodifiableMap(this.attributes);

  private volatile String text;
  private final List<Topic> children = new ArrayList<>();
  private final List<Topic> unmodifableChildren = Collections.unmodifiableList(this.children);

  private transient Object payload;

  private final transient long localUID = LOCALUID_GENERATOR.getAndIncrement();

  private static final Pattern PATTERN_TOPIC_HEADER = Pattern.compile("^\\s*(\\#+)\\s*(.*)$"); //NOI18N
  private static final Pattern PATTERN_EXTRA = Pattern.compile("^\\s*\\-\\s*([^\\s]+)\\s*$"); //NOI18N
  private static final Pattern PATTERN_MARKDOWN_FORMAT = Pattern.compile("(?ms)^\\s*(#+\\s.*?)$|^\\s*(-\\s.*?)$|^\\s*(\\>.*?)$|\\<\\s*?pre\\s*?\\>(.*?)\\<\\s*?\\/\\s*?pre\\s*\\>|^(.*?)$"); //NOI18N
  private static final int MD_GROUP_HEAD = 1;
  private static final int MD_GROUP_ITEM = 2;
  private static final int MD_GROUP_BLOCKQUOTE = 3;
  private static final int MD_GROUP_PRE = 4;
  private static final int MD_GROUP_OTHER_LINE = 5;

  private final MindMap map;

  private Topic(final Topic base) {
    this(base.map, base.parent, base.text);
    this.attributes.putAll(base.attributes);
    this.extras.putAll(base.extras);
  }

  public Topic(final MindMap map, final Topic parent, final String text, final Extra<?>... extras) {
    this.map = map;
    this.parent = parent;
    this.text = text;

    for (final Extra<?> e : extras) {
      if (e != null) {
        this.extras.put(e.getType(), e);
      }
    }

    if (parent != null) {
      parent.children.add(this);
    }
  }

  public boolean containTopic(final Topic topic) {
    boolean result = false;

    if (this == topic) {
      result = true;
    }
    else {
      for (final Topic t : this.children) {
        if (t.containTopic(topic)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  public Object getPayload() {
    return this.payload;
  }

  public void setPayload(final Object value) {
    this.payload = value;
  }

  private Object readResolve() {
    return new Topic(this);
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
    }
    finally {
      this.map.unlock();
    }
  }

  public Topic getRoot() {
    this.map.lock();
    try {
      Topic result = this;
      while (result.getParent() != null) {
        result = result.parent;
      }
      return result;
    }
    finally {
      this.map.unlock();
    }
  }

  public boolean canBeLost() {
    this.map.lock();
    try {
      final boolean hasNotImportantAttributes = this.attributes.isEmpty() || (this.attributes.size() == 1 && this.attributes.containsKey("leftSide")); //NOI18N
      
      boolean noImportantContent = this.text.trim().isEmpty() && hasNotImportantAttributes && this.extras.isEmpty();
      if (noImportantContent) {
        for (final Topic t : this.children) {
          noImportantContent &= t.canBeLost();
          if (!noImportantContent) {
            break;
          }
        }
      }
      return noImportantContent;
    }
    finally {
      this.map.unlock();
    }
  }

  public static Topic parse(final MindMap map, final String text) throws IOException {
    map.lock();
    try {
      final Matcher matcher = PATTERN_MARKDOWN_FORMAT.matcher(text);

      Topic topic = null;
      int depth = 0;

      Extra.ExtraType extraType = null;

      while (matcher.find()) {
        if (matcher.group(MD_GROUP_HEAD) != null) {
          extraType = null;

          final Matcher topicMatcher = PATTERN_TOPIC_HEADER.matcher(matcher.group(MD_GROUP_HEAD));
          if (topicMatcher.find()) {
            final int newDepth = topicMatcher.group(1).length();
            final String newTopicText = ModelUtils.unescapeMarkdownStr(topicMatcher.group(2));

            if (newDepth == depth + 1) {
              depth = newDepth;
              topic = new Topic(map, topic, newTopicText);
            }
            else if (newDepth == depth) {
              topic = new Topic(map, topic == null ? null : topic.getParent(), newTopicText);
            }
            else if (newDepth < depth) {
              if (topic != null) {
                topic = topic.findParentForDepth(depth - newDepth);
                topic = new Topic(map, topic, newTopicText);
                depth = newDepth;
              }
            }
          }
        }
        else if (matcher.group(MD_GROUP_ITEM) != null) {
          final Matcher extraName = PATTERN_EXTRA.matcher(matcher.group(MD_GROUP_ITEM));
          if (extraName.find()) {
            try {
              extraType = Extra.ExtraType.valueOf(extraName.group(1));
            }
            catch (IllegalArgumentException ex) {
              extraType = null;
            }
          }
        }
        else if (matcher.group(MD_GROUP_BLOCKQUOTE) != null) {
          if (topic != null) {
            MindMap.fillMapByAttributes(matcher.group(MD_GROUP_BLOCKQUOTE), topic.attributes);
          }
          extraType = null;
        }
        else if (matcher.group(MD_GROUP_PRE) != null) {
          if (topic != null && extraType != null) {
            try {
              final String groupPre = matcher.group(MD_GROUP_PRE);
              if (extraType.isStringValid(groupPre)){
                topic.setExtra(extraType.parseLoaded(groupPre));
              }else{
                logger.error("Detected invalid extra data "+extraType+" : "+groupPre);
              }
            }
            catch (Exception ex) {
              logger.error("Unexpected exception #23241", ex); //NOI18N
            }
            finally {
              extraType = null;
            }
          }
        }
        else if (matcher.group(MD_GROUP_OTHER_LINE) != null) {
          if (topic != null && extraType != null) {
            extraType = null;
          }
        }
      }

      return topic == null ? null : topic.getRoot();
    }
    finally {
      map.unlock();
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

  public Map<String, String> getAttributes() {
    return this.unmodifableAttributes;
  }

  public void setAttribute(final String name, final String value) {
    this.map.lock();
    try {
      if (value == null) {
        this.attributes.remove(name);
      }
      else {
        this.attributes.put(name, value);
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public String getAttribute(final String name) {
    return this.attributes.get(name);
  }

  public void delete() {
    this.map.lock();
    try {
      if (this.parent != null) {
        this.parent.children.remove(this);
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public Topic getParent() {
    return this.parent;
  }

  public String getText() {
    return this.text;
  }

  public boolean isFirstChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(0) == t;
  }

  public boolean isLastChild(final Topic t) {
    return !this.children.isEmpty() && this.children.get(this.children.size() - 1) == t;
  }

  public void setText(final String text) {
    this.map.lock();
    try {
      this.text = text;
    }
    finally {
      this.map.unlock();
    }
  }

  public boolean removeExtra(final Extra.ExtraType... types) {
    this.map.lock();
    try {
      boolean result = false;
      for (final Extra.ExtraType e : types) {
        if (e != null) {
          result |= this.extras.remove(e) != null;
        }
      }
      return result;
    }
    finally {
      this.map.unlock();
    }
  }

  public void setExtra(final Extra<?>... extras) {
    this.map.lock();
    try {
      for (final Extra<?> e : extras) {
        if (e != null) {
          this.extras.put(e.getType(), e);
        }
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public boolean makeFirst() {
    this.map.lock();
    try {
      if (this.parent != null) {
        int thatIndex = this.parent.children.indexOf(this);
        if (thatIndex > 0) {
          this.parent.children.remove(thatIndex);
          this.parent.children.add(0, this);
          return true;
        }
      }
      return false;
    }
    finally {
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
      if (this.parent != null) {
        int thatIndex = this.parent.children.indexOf(this);
        if (thatIndex >= 0 && thatIndex != this.parent.children.size() - 1) {
          this.parent.children.remove(thatIndex);
          this.parent.children.add(this);
          return true;
        }
      }
      return false;
    }
    finally {
      this.map.unlock();
    }
  }

  public void moveBefore(final Topic topic) {
    this.map.lock();
    try {
      if (this.parent != null) {
        int thatIndex = this.parent.children.indexOf(topic);
        final int thisIndex = this.parent.children.indexOf(this);

        if (thatIndex > thisIndex) {
          thatIndex--;
        }

        if (thatIndex >= 0 && thisIndex >= 0) {
          this.parent.children.remove(this);
          this.parent.children.add(thatIndex, this);
        }
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public String findAttributeInAncestors(final String attrName){
    this.map.lock();
    try{
      String result = null;
      Topic current = this.parent;
      while(result == null && current!=null){
        result = current.getAttribute(attrName);
        current = current.parent;
      }
      return result;
    }finally{
      this.map.unlock();
    }
  }
  
  public void moveAfter(final Topic topic) {
    this.map.lock();
    try {
      if (this.parent != null) {
        int thatIndex = this.parent.children.indexOf(topic);
        int thisIndex = this.parent.children.indexOf(this);

        if (thatIndex > thisIndex) {
          thatIndex--;
        }

        if (thatIndex >= 0 && thisIndex >= 0) {
          this.parent.children.remove(this);
          this.parent.children.add(thatIndex + 1, this);
        }
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public void write(final Writer out) throws IOException {
    this.map.lock();
    try {
      write(1, out);
    }
    finally {
      this.map.unlock();
    }
  }

  private void write(final int level, final Writer out) throws IOException {
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
  public boolean equals(final Object topic) {
    if (topic == null) {
      return false;
    }
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
    return "MindMapTopic('" + this.text + "')"; //NOI18N
  }

  public long getLocalUid() {
    return this.localUID;
  }

  public boolean hasChildren() {
    this.map.lock();
    try {
      return !this.children.isEmpty();
    }
    finally {
      this.map.unlock();
    }
  }

  boolean removeAllLinksTo(final Topic topic) {
    boolean result = false;

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

    return result;
  }

  boolean removeTopic(final Topic topic) {
    final Iterator<Topic> iterator = this.children.iterator();
    while (iterator.hasNext()) {
      final Topic t = iterator.next();
      if (t == topic) {
        iterator.remove();
        return true;
      }
      else {
        if (t.removeTopic(topic)) {
          return true;
        }
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
      if (newParent == null || this == newParent || this.getParent() == newParent || this.children.contains(newParent)) {
        return false;
      }

      this.parent.children.remove(this);
      newParent.children.add(this);
      this.parent = newParent;

      return true;
    }
    finally {
      this.map.unlock();
    }
  }

  public Topic makeChild(final String text, final Topic afterTheTopic) {
    this.map.lock();
    try {
      final int index = afterTheTopic == null ? -1 : this.children.indexOf(afterTheTopic);
      Topic result = new Topic(this.map, this, text == null ? "" : text); //NOI18N
      if (index >= 0) {
        result.moveAfter(afterTheTopic);
      }
      return result;
    }
    finally {
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
            }
            else if (checker.check(current.children.get(i))) {
              result = current.children.get(i);
              break;
            }
          }
        }
      }

      return result;
    }
    finally {
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
            if (checker == null) {
              result = current.children.get(i);
              break;
            }
            else if (checker.check(current.children.get(i))) {
              result = current.children.get(i);
              break;
            }
          }
        }
      }

      return result;
    }
    finally {
      this.map.unlock();
    }
  }

  public void removeExtras(final Extra<?>... extras) {
    this.map.lock();
    try {
      if (extras == null || extras.length == 0) {
        this.extras.clear();
      }
      else {
        for (final Extra<?> e : extras) {
          if (e != null) {
            this.extras.remove(e.getType());
          }
        }
      }
    }
    finally {
      this.map.unlock();
    }
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
    return list.toArray(new Topic[list.size()]);
  }

  Topic makeCopy(final MindMap newMindMap, final Topic parent) {
    this.map.lock();
    try {
      final Topic result = new Topic(newMindMap, parent, this.text, this.extras.values().toArray(new Extra<?>[this.extras.values().size()]));
      for (final Topic c : this.children) {
        c.makeCopy(newMindMap, result);
      }
      result.attributes.putAll(this.attributes);

      return result;
    }
    finally {
      this.map.unlock();
    }
  }

  public boolean removeExtraFromSubtree(final Extra.ExtraType... type) {
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
    }
    finally {
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
    }
    finally {
      this.map.unlock();
    }
  }

}
