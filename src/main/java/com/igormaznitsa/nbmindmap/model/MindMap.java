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

import com.igormaznitsa.nbmindmap.utils.Logger;
import com.igormaznitsa.nbmindmap.utils.Utils;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
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

public final class MindMap implements Serializable, Constants, TreeModel {

  private static final long serialVersionUID = 5929181596778047354L;

  private final Topic root;
  private final Lock locker = new ReentrantLock();
  private final Map<String, String> attributes = new HashMap<String, String>();
  private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s*(.+)$");
  private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*\\\"(.*?)\\\"");

  private final List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
  
  public MindMap() {
    this.root = new Topic(this, null, "");
  }

  public MindMap(final Reader reader) throws IOException {
    final StringBuilder lineBuffer = new StringBuilder();
    while (true) {
      final int chr = reader.read();
      if (chr < 0) {
        throw new IllegalArgumentException("It is not Mind Map");
      }
      if (chr == '\n') {
        final String line = lineBuffer.toString().trim();
        if (Utils.onlyFromChar(line, '-')) {
          break;
        }
        if (line.startsWith(">")) {
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
  }
  
  private void fireModelChanged(){
    final TreeModelEvent evt = new TreeModelEvent(this, this.root == null ?  null : this.root.getPath());
    for(final TreeModelListener l : this.treeListeners){
      l.treeStructureChanged(evt);
    }
  }
  
  private void fireTopicChanged(final Topic topic){
    final TreeModelEvent evt = new TreeModelEvent(this, topic == null ? null : topic.getPath());
    for(final TreeModelListener l : this.treeListeners){
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
        map.put(attrParser.group(1), Utils.unescapeStr(attrParser.group(2)));
      }
      return true;
    }
    return false;
  }

  static String allAttributesAsString(final Map<String, String> map) {
    final StringBuilder buffer = new StringBuilder();

    boolean nonfirst = false;
    for (final Map.Entry<String, String> e : map.entrySet()) {
      if (nonfirst) {
        buffer.append(',');
      }
      else {
        nonfirst = true;
      }
      buffer.append(e.getKey()).append("=\"").append(Utils.escapeStr(e.getValue())).append('\"');
    }

    return buffer.toString();
  }

  public void write(final Writer out) throws IOException {
    this.locker.lock();
    try {
      out.append("Mind Map generated by NB MindMap plugin").append(NEXT_PARAGRAPH);
      if (!this.attributes.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(this.attributes)).append(NEXT_LINE);
      }
      out.append("---").append(NEXT_LINE);
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

  public void removeTopic(final Topic topic) {
    if (this.root == topic){
      this.root.setText("");
      this.root.removeExtras();
      this.root.setPayload(null);
      this.root.removeAllChildren();
    }else{
      this.root.removeTopic(topic);
    }
  }

  public Topic findTopicForUID(final String topicUID) {
    return this.root.findForAttribute("uid",topicUID);
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
    if (newValue instanceof String){
      ((Topic)path.getLastPathComponent()).setText((String)newValue);
      fireTopicChanged((Topic) path.getLastPathComponent());
    }else {
      Logger.warn("Attempt to set non string value to path : "+path);
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
}
