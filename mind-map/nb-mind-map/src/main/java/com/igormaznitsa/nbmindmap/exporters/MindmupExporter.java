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
package com.igormaznitsa.nbmindmap.exporters;

import com.igormaznitsa.nbmindmap.mmgui.AbstractCollapsableElement;
import com.igormaznitsa.nbmindmap.mmgui.Configuration;
import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.openide.filesystems.FileChooserBuilder;

public class MindmupExporter extends AbstractMindMapExporter {

  private static int idCounter = 1;

  private static class TopicData {

    private final int uid;
    private final int id;
    private final Topic topic;

    public TopicData(final int uid, final int id, final Topic topic) {
      this.uid = uid;
      this.id = id;
      this.topic = topic;
    }

    public int getUID() {
      return this.uid;
    }

    public int getID() {
      return id;
    }

    public Topic getTopic() {
      return this.topic;
    }
  }

  private static class State {

    private final JSONObject main = new JSONObject();
    private final Stack<Object> stack = new Stack<Object>();
    private final Map<String, TopicData> topicsWithId = new HashMap<String, TopicData>();
    private final List<TopicData> topicsContainsJump = new ArrayList<TopicData>();

    public State() {
      stack.push(main);
    }

    public void processTopic(final int uid, final int id, final Topic topic) {
      final String topicUID = getTopicUid(topic);
      if (topicUID != null) {
        topicsWithId.put(topicUID, new TopicData(uid, id, topic));
      }

      final ExtraTopic linkto = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);
      if (linkto != null) {
        this.topicsContainsJump.add(new TopicData(uid, id, topic));
      }
    }

    public List<TopicData> getTopicsContainingJump() {
      return this.topicsContainsJump;
    }

    public TopicData findTopic(final ExtraTopic link) {
      return topicsWithId.get(link.getValue());
    }

    public State start(final String key) {
      final JSONObject newObject = new JSONObject();

      final Object top = stack.peek();
      if (top instanceof JSONObject) {
        ((JSONObject) stack.peek()).put(key, newObject);
      }
      else if (top instanceof JSONArray) {
        ((JSONArray) stack.peek()).add(newObject);
      }
      stack.push(newObject);
      return this;
    }

    public State startArray(final String key) {
      final JSONArray newArray = new JSONArray();
      ((JSONObject) stack.peek()).put(key, newArray);
      stack.push(newArray);
      return this;
    }

    public State set(final String key, final String value) {
      ((JSONObject) stack.peek()).put(key, value);
      return this;
    }

    public State set(final String key, final int value) {
      ((JSONObject) stack.peek()).put(key, value);
      return this;
    }

    public State end() {
      stack.pop();
      return this;
    }

    @Override
    public String toString() {
      return main.toJSONString();
    }
  }

  private static String getTopicUid(final Topic topic) {
    return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
  }

  private int writeTopic(final State state, int id, final Configuration cfg, final Topic topic) {
    state.start(Integer.toString(idCounter));

    state.processTopic(idCounter, id, topic);

    idCounter++;

    final int level = topic.getTopicLevel();

    state.set("title", topic.getText()); //NOI18N
    state.set("id", id); //NOI18N

    id = Math.abs(id);

    state.start("ideas"); //NOI18N
    for (final Topic t : topic.getChildren()) {
      id = writeTopic(state, id + 1, cfg, t);
    }
    state.end();

    state.start("attr"); //NOI18N
    state.start("style").set("background", Utils.color2html(level == 1 ? cfg.getFirstLevelBackgroundColor() : cfg.getOtherLevelBackgroundColor())).end(); //NOI18N

    final String attachment = makeHtmlFromExtras(topic);
    if (attachment != null) {
      state.start("attachment"); //NOI18N
      state.set("contentType", "text/html"); //NOI18N
      state.set("content", attachment); //NOI18N
      state.end();
    }

    state.end();
    state.end();

    return id;
  }

  private static String makeHtmlFromExtras(final Topic topic) {
    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);

    if (file == null && link == null && note == null) {
      return null;
    }

    final StringBuilder result = new StringBuilder();

    if (file != null) {
      result.append("FILE: <a href=\"").append(file.getValue().toASCIIString()).append("\">").append(file.getValue().toASCIIString()).append("</a><br>"); //NOI18N
    }
    if (link != null) {
      result.append("LINK: <a href=\"").append(link.getValue().toASCIIString()).append("\">").append(link.getValue().toASCIIString()).append("</a><br>"); //NOI18N
    }
    if (note != null) {
      if (file != null || link != null) {
        result.append("<br>"); //NOI18N
      }
      result.append("<pre>").append(StringEscapeUtils.escapeHtml(note.getValue())).append("</pre>"); //NOI18N
    }
    return result.toString();
  }

  private void writeRoot(final State state, final Configuration cfg, final Topic root) {
    if (root == null) {
      state.set("title", ""); //NOI18N
    }
    else {
      state.set("title", root.getText()); //NOI18N
    }
    state.set("id", 1); //NOI18N
    state.set("formatVersion", 2); //NOI18N

    final List<Topic> leftChildren = new ArrayList<Topic>();
    final List<Topic> rightChildren = new ArrayList<Topic>();

    if (root != null) {
      for (final Topic t : root.getChildren()) {
        if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
          leftChildren.add(t);
        }
        else {
          rightChildren.add(t);
        }
      }
    }
    state.start("ideas"); //NOI18N

    if (root != null) {
      state.processTopic(0, 1, root);
    }

    int id = 2;
    for (final Topic right : rightChildren) {
      id = writeTopic(state, id + 1, cfg, right);
    }

    for (final Topic left : leftChildren) {
      id = writeTopic(state, -(id + 1), cfg, left);
    }

    state.end();

    state.start("attr"); //NOI18N
    state.start("style").set("background", Utils.color2html(cfg.getRootBackgroundColor())).end(); //NOI18N

    final String attachment = makeHtmlFromExtras(root);
    if (attachment != null) {
      state.start("attachment"); //NOI18N
      state.set("contentType", "text/html"); //NOI18N
      state.set("content", attachment); //NOI18N
      state.end();
    }

    state.end();

    final List<TopicData> topicsWithJumps = state.getTopicsContainingJump();
    if (!topicsWithJumps.isEmpty()) {
      state.startArray("links"); //NOI18N
      for (final TopicData src : topicsWithJumps) {
        final TopicData dest = state.findTopic((ExtraTopic) src.getTopic().getExtras().get(Extra.ExtraType.TOPIC));
        if (dest != null) {
          state.start(""); //NOI18N
          state.set("ideaIdFrom", src.getID()); //NOI18N
          state.set("ideaIdTo", dest.getID()); //NOI18N
          state.start("attr").start("style").set("color", "#FF0000").set("lineStyle", "dashed").end().end(); //NOI18N
          state.end();
        }
      }

      state.end();
    }

    state.end();
  }

  @Override
  public void doExport(final MindMapPanel currentPanel) throws IOException {
    final State state = new State();
    writeRoot(state, currentPanel.getConfiguration(), currentPanel.getModel().getRoot());

    final String text = state.toString();

    final File home = new File(System.getProperty("user.home"));//NOI18N
    File fileToSaveImage = new FileChooserBuilder("user-dir")//NOI18N
            .setTitle(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MindmupExporter.SaveDialog.title")).setDefaultWorkingDirectory(home).setFilesOnly(true).setFileFilter(new FileFilter() {

              @Override
              public boolean accept(File f) {
                return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".mup"));//NOI18N
              }

              @Override
              public String getDescription() {
                return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MindmupExporter.fileFilterDescription");
              }
            }).setApproveText(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MindmupExporter.saveDialog.approveButton")).showSaveDialog();

    fileToSaveImage = checkFile(fileToSaveImage, ".mup");//NOI18N

    if (fileToSaveImage != null) {
      FileUtils.writeStringToFile(fileToSaveImage, text, "UTF-8");//NOI18N
    }
  }

  @Override
  public String getName() {
    return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MindmupExporter.exporterName");
  }

  @Override
  public String getReference() {
    return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MindmupExporter.exporterReference");
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.MINDMUP.getIcon();
  }

}
