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
package com.igormaznitsa.nbmindmap.model;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MindMapTopic implements Serializable, Constants {

  private static final long serialVersionUID = -4512887489914466613L;

  private static final Logger LOGGER = Logger.getLogger(MindMapTopic.class.getName());

  private static final AtomicLong LOCALUID_GENERATOR = new AtomicLong();

  private final MindMapTopic parent;
  private final EnumMap<Extra.ExtraType, Extra<?>> extras = new EnumMap<Extra.ExtraType, Extra<?>>(Extra.ExtraType.class);
  private final Map<String,String> attributes = new HashMap<String, String>();
  private volatile String text;
  private final List<MindMapTopic> children = new ArrayList<MindMapTopic>();
  private transient Object payload;

  private final transient long localUID = LOCALUID_GENERATOR.getAndIncrement();

  private static final Pattern PATTERN_TOPIC_HEADER = Pattern.compile("^\\s*(\\#+)\\s*(.*)$");
  private static final Pattern PATTERN_EXTRA = Pattern.compile("^\\s*\\-\\s*([^\\s]+)\\s*$");
  private static final Pattern PATTERN_MARKDOWN_FORMAT = Pattern.compile("(?ms)^\\s*(#+\\s*.*?)$|^\\s*(-\\s.*?)$|^\\s*(\\>.*?)$|(\\`+)(.+?)\\4|^(.*)$");
  private static final Pattern PATTERN_LINK = Pattern.compile("^\\s*\\!?\\[.*\\]\\((.*)\\)$");
  private static final int MD_GROUP_HEAD = 1;
  private static final int MD_GROUP_ITEM = 2;
  private static final int MD_GROUP_BLOCKQUOTE = 3;
  private static final int MD_GROUP_CODE = 5;
  private static final int MD_GROUP_OTHER_LINE = 6;

  private final MindMap map;

  private MindMapTopic(final MindMapTopic base){
    this(base.map, base.parent, base.text);
    this.attributes.putAll(base.attributes);
    this.extras.putAll(base.extras);
  }
  
  public MindMapTopic(final MindMap map, final MindMapTopic parent, final String text, final Extra<?>... extras) {
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

  public Object getPayload(){
    return this.payload;
  }
  
  public void setPayload(final Object value){
    this.payload = value;
  }
  
  private Object readResolve(){
    return new MindMapTopic(this);
  }
  
  public MindMap getMap() {
    return this.map;
  }

  public int getTopicLevel(){
    MindMapTopic topic = this.parent;
    int result = 0;
    while(topic!=null){
      topic = topic.parent;
      result ++;
    }
    return result;
  }
  
  public MindMapTopic findParentForDepth(int depth) {
    this.map.lock();
    try {
      MindMapTopic result = this.parent;
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

  public MindMapTopic getRoot() {
    this.map.lock();
    try {
      MindMapTopic result = this;
      while (result.getParent() != null) {
        result = result.parent;
      }
      return result;
    }
    finally {
      this.map.unlock();
    }
  }

  public static MindMapTopic parse(final MindMap map, final String text) throws IOException {
    map.lock();
    try {
      final Matcher matcher = PATTERN_MARKDOWN_FORMAT.matcher(text);

      MindMapTopic topic = null;
      int depth = 0;

      Extra.ExtraType extraType = null;

      while (matcher.find()) {
        if (matcher.group(MD_GROUP_HEAD) != null) {
          extraType = null;

          final Matcher topicMatcher = PATTERN_TOPIC_HEADER.matcher(matcher.group(MD_GROUP_HEAD));
          if (topicMatcher.find()) {
            final int newDepth = topicMatcher.group(1).length();
            final String newTopicText = Utils.unescapeStr(topicMatcher.group(2));

            if (newDepth == depth + 1) {
              depth = newDepth;
              topic = new MindMapTopic(map, topic, newTopicText);
            }
            else if (newDepth == depth) {
              topic = new MindMapTopic(map, topic == null ? null : topic.getParent(), newTopicText);
            }
            else if (newDepth < depth) {
              if (topic != null) {
                topic = topic.findParentForDepth(depth - newDepth);
                topic = new MindMapTopic(map, topic, newTopicText);
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
        else if (matcher.group(MD_GROUP_CODE) != null) {
          if (topic != null && extraType != null) {
            try {
              topic.setExtra(extraType.make(matcher.group(MD_GROUP_CODE)));
            }
            catch (Exception ex) {
            }
            finally {
              extraType = null;
            }
          }
        }
        else if (matcher.group(MD_GROUP_OTHER_LINE) != null) {
          if (topic != null && extraType != null) {
            final Matcher linkMatcher = PATTERN_LINK.matcher(matcher.group(MD_GROUP_OTHER_LINE));
            if (linkMatcher.find()) {
              try {
                topic.setExtra(extraType.make(linkMatcher.group(1)));
              }
              catch (Exception ex) {
              }
            }
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

  public List<MindMapTopic> getChildren() {
    return Collections.unmodifiableList(this.children);
  }

  public Map<Extra.ExtraType, Extra<?>> getExtras() {
    return Collections.unmodifiableMap(this.extras);
  }

  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(this.attributes);
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

  public MindMapTopic getParent() {
    return this.parent;
  }

  public String getText() {
    return this.text;
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

  public void removeExtra(final Extra.ExtraType... types) {
    this.map.lock();
    try {
      for (final Extra.ExtraType e : types) {
        if (e != null) {
          this.extras.remove(e);
        }
      }
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

  public void attachTo(final MindMapTopic topic) {
    this.map.lock();
    try {
      delete();
      if (topic != null) {
        topic.children.add(this);
      }
    }
    finally {
      this.map.unlock();
    }
  }

  public void placeBefore(final MindMapTopic topic) {
    this.map.lock();
    try {
      if (this.parent != null) {
        final int thatIndex = this.parent.children.indexOf(topic);
        final int thisIndex = this.parent.children.indexOf(this);
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

  public void placeAfter(final MindMapTopic topic) {
    this.map.lock();
    try {
      if (this.parent != null) {
        final int thatIndex = this.parent.children.indexOf(topic);
        final int thisIndex = this.parent.children.indexOf(this);
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
    Utils.writeChar(out, '#', level);
    out.append(' ').append(Utils.escapeStr(this.text)).append(NEXT_LINE);
    if (!this.attributes.isEmpty()) {
      out.append("> ").append(MindMap.allAttributesAsString(this.attributes)).append(NEXT_LINE);
    }
    for (final Map.Entry<Extra.ExtraType, Extra<?>> e : this.extras.entrySet()) {
      e.getValue().write(out);
      out.append(NEXT_LINE);
    }
    for (final MindMapTopic t : this.children) {
      t.write(level + 1, out);
    }
  }

  @Override
  public int hashCode() {
    return (int)((this.localUID >>> 32) ^ (this.localUID & 0xFFFFFFFFL));
  }

  @Override
  public boolean equals(final Object topic) {
    if (topic == null) {
      return false;
    }
    if (this == topic) {
      return true;
    }
    if (topic instanceof MindMapTopic) {
      return this.localUID == ((MindMapTopic)topic).localUID;
    }
    return false;
  }

  @Override
  public String toString() {
    return this.text;
  }

  public long getLocalUid() {
    return this.localUID;
  }

  public boolean hasChildren() {
    this.map.lock();
    try{
      return !this.children.isEmpty();
    }finally{
      this.map.unlock();
    }
  }

}
