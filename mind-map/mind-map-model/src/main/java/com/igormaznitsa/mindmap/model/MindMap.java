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
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MindMap implements Serializable, Constants, TreeModel {

  private static final long serialVersionUID = 5929181596778047354L;

  private static final Logger logger = LoggerFactory.getLogger(MindMap.class);

  private final Topic root;
  private final Lock locker = new ReentrantLock();
  private final Map<String, String> attributes = new HashMap<String, String>();
  private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s(.+)$"); //NOI18N
  private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*(\\`+)(.*?)\\2"); //NOI18N

  private static final String GENERATOR_VERSION_NAME = "__version__"; //NOI18N
  private static final String GENERATOR_VERSION = "1.0"; //NOI18N

  private final transient List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();

  private final MindMapController controller;
  
  public MindMap(final MindMapController nullableController) {
    this.root = new Topic(this, null, ""); //NOI18N
    this.controller = nullableController;
  }

  public MindMap(final MindMap map,final MindMapController nullableController) {
    this.attributes.putAll(map.attributes);
    this.root = map.getRoot() == null ? null : map.getRoot().makeCopy(this, null);
    this.controller = nullableController;
  }

  public MindMap(final MindMapController nullableController, final Reader reader) throws IOException {
    this.controller = nullableController;
    final StringBuilder lineBuffer = new StringBuilder();
    while (true) {
      final int chr = reader.read();
      if (chr < 0) {
        throw new IllegalArgumentException("It is not Mind Map"); //NOI18N
      }
      if (chr == '\n') {
        final String line = lineBuffer.toString().trim();
        if (ModelUtils.onlyFromChar(line, '-')) {
          break;
        }
        if (line.startsWith(">")) { //NOI18N
          fillMapByAttributes(line, this.attributes);
        }
        lineBuffer.setLength(0);
      }
      else {
        lineBuffer.append((char) chr);
      }
    }
    lineBuffer.trimToSize();

    final String str = IOUtils.toString(reader);
    this.root = Topic.parse(this, str);

    this.attributes.put(GENERATOR_VERSION_NAME, GENERATOR_VERSION);
  }

  public MindMapController getController(){
    return this.controller;
  }
  
  private void fireModelChanged() {
    final TreeModelEvent evt = new TreeModelEvent(this, this.root == null ? null : this.root.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeStructureChanged(evt);
    }
  }

  private void fireTopicChanged(final Topic topic) {
    final TreeModelEvent evt = new TreeModelEvent(this, topic == null ? null : topic.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeNodesChanged(evt);
    }
  }

  public String getAttribute(final String name) {
    return this.attributes.get(name);
  }

  public void setAttribute(final String name, final String value) {
    this.locker.lock();
    try {
      if (value == null) {
        this.attributes.remove(name);
      }
      else {
        this.attributes.put(name, value);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  public void resetPayload() {
    this.locker.lock();
    try {
      if (this.root != null) {
        resetPayload(this.root);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  private void resetPayload(final Topic t) {
    t.setPayload(null);
    for (final Topic m : t.getChildren()) {
      resetPayload(m);
    }
  }

  public Topic findForPositionPath(final int[] positions) {
    if (positions == null || positions.length == 0) {
      return null;
    }
    if (positions.length == 1) {
      return this.root;
    }

    Topic result = this.root;
    int index = 1;
    while (result != null && index < positions.length) {
      final int elementPosition = positions[index++];
      if (elementPosition < 0 || result.getChildren().size() <= elementPosition) {
        result = null;
        break;
      }
      result = result.getChildren().get(elementPosition);
    }
    return result;
  }

  public List<Topic> removeNonExistingTopics(final List<Topic> origList) {
    final List<Topic> result = new ArrayList<Topic>();
    this.locker.lock();
    try {
      if (this.root != null) {
        for (final Topic t : origList) {
          if (this.root.containTopic(t)) {
            result.add(t);
          }
        }
      }
    }
    finally {
      this.locker.unlock();
    }
    return result;
  }

  @Override
  public Topic getRoot() {
    this.locker.lock();
    try {
      return this.root;
    }
    finally {
      this.locker.unlock();
    }
  }

  static boolean fillMapByAttributes(final String line, final Map<String, String> map) {
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

    boolean nonfirst = false;
    for (final Map.Entry<String, String> e : map.entrySet()) {
      if (nonfirst) {
        buffer.append(',');
      }
      else {
        nonfirst = true;
      }
      buffer.append(e.getKey()).append('=').append(ModelUtils.makeMDCodeBlock(e.getValue()));
    }

    return buffer.toString();
  }

  public String packToString() {
    final StringWriter writer = new StringWriter(16384);
    try {
      write(writer);
    }
    catch (IOException ex) {
      throw new Error("Unexpected exception", ex);
    }
    return writer.toString();
  }

  public void write(final Writer out) throws IOException {
    this.locker.lock();
    try {
      out.append("Mind Map generated by NB MindMap plugin").append(NEXT_PARAGRAPH); //NOI18N
      this.attributes.put(GENERATOR_VERSION_NAME, GENERATOR_VERSION);
      if (!this.attributes.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(this.attributes)).append(NEXT_LINE); //NOI18N
      }
      out.append("---").append(NEXT_LINE); //NOI18N
      this.root.write(out);
    }
    finally {
      this.locker.unlock();
    }
  }

  public void lock() {
    this.locker.lock();
  }

  public void unlock() {
    this.locker.unlock();
  }

  public Topic cloneTopic(final Topic topic, final boolean cloneFullTree) {
    this.locker.lock();
    try {
      if (topic == null || topic == this.root) {
        return null;
      }

      final Topic clonedtopic = topic.makeCopy(this, topic.getParent());
      if (!cloneFullTree) {
        clonedtopic.removeAllChildren();
      }

      clonedtopic.removeAttributeFromSubtree(ExtraTopic.TOPIC_UID_ATTR);

      fireModelChanged();

      return clonedtopic;
    }
    finally {
      this.locker.unlock();
    }
  }

  public boolean removeTopic(final Topic topic) {
    this.locker.lock();
    try {
      final boolean result;
      if (this.root == topic) {
        this.root.setText(""); //NOI18N
        this.root.removeExtras();
        this.root.setPayload(null);
        this.root.removeAllChildren();
        result = true;
      }
      else {
        this.root.removeTopic(topic);
        result = this.root.removeAllLinksTo(topic);
      }

      if (result) {
        fireModelChanged();
      }

      return result;
    }
    finally {
      this.locker.unlock();
    }
  }

  public Topic findTopicForLink(final ExtraTopic link) {
    if (link == null) {
      return null;
    }
    this.locker.lock();
    try {
      return this.root.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, link.getValue());
    }
    finally {
      this.locker.unlock();
    }
  }

  public List<Topic> findAllTopicsForExtraType(final Extra.ExtraType type) {
    final List<Topic> result = new ArrayList<Topic>();
    this.locker.lock();
    try {
      if (this.root != null) {
        _findAllTopicsForExtraType(this.root, type, result);
      }
    }
    finally {
      this.locker.unlock();
    }
    return result;
  }

  private void _findAllTopicsForExtraType(final Topic topic, final Extra.ExtraType type, final List<Topic> result) {
    if (topic.getExtras().containsKey(type)) {
      result.add(topic);
    }
    for (final Topic c : topic.getChildren()) {
      _findAllTopicsForExtraType(c, type, result);
    }
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    return ((Topic) parent).getChildren().get(index);
  }

  @Override
  public int getChildCount(Object parent) {
    return ((Topic) parent).getChildren().size();
  }

  @Override
  public boolean isLeaf(final Object node) {
    return !((Topic) node).hasChildren();
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    if (newValue instanceof String) {
      ((Topic) path.getLastPathComponent()).setText((String) newValue);
      fireTopicChanged((Topic) path.getLastPathComponent());
    }
    else {
      logger.warn("Attempt to set non string value to path : " + path); //NOI18N
    }
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return ((Topic) parent).getChildren().indexOf(child);
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    this.treeListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    this.treeListeners.remove(l);
  }

  public boolean doesContainFileLink(final File baseFolder, final MMapURI file) {
    this.locker.lock();
    try {
      if (this.root == null) {
        return false;
      }
      else {
        return this.root.doesContainFileLink(baseFolder, file);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  public boolean deleteAllLinksToFile(final File baseFolder, final MMapURI file) {
    boolean changed = false;
    this.locker.lock();
    try {
      if (this.root != null) {
        changed = this.root.deleteLinkToFileIfPresented(baseFolder, file);
      }
    }
    finally {
      this.locker.unlock();
    }
    if (changed) {
      fireModelChanged();
    }
    return changed;
  }

  public boolean replaceAllLinksToFile(final File baseFolder, final MMapURI oldFile, final MMapURI newFile) {
    boolean changed = false;
    this.locker.lock();
    try {
      if (this.root != null) {
        changed = this.root.replaceLinkToFileIfPresented(baseFolder, oldFile, newFile);
      }
    }
    finally {
      this.locker.unlock();
    }
    if (changed) {
      fireModelChanged();
    }
    return changed;
  }

}
